package com.fasterxml.jackson.databind.introspect;

import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.cfg.MapperConfig;

import org.junit.Test;
import static org.junit.Assert.*;

public class POJOPropertiesCollectorTest
{
    // POJOs for testing various property combinations

    static class TwoAnySetterFields {
        @JsonAnySetter
        public Map<String, Object> extra1;

        @JsonAnySetter
        public Map<String, Object> extra2;
    }

    static class SingleAnySetterField {
        @JsonAnySetter
        public Map<String, Object> extra;
    }

    static class TwoAnySetterMethods {
        @JsonAnySetter
        public void setAny1(String name, Object value) { }

        @JsonAnySetter
        public void setAny2(String name, Object value) { }
    }

    static class SingleAnySetterMethod {
        @JsonAnySetter
        public void setAny(String name, Object value) { }
    }

    static class TwoAnyGetters {
        @JsonAnyGetter
        public Map<String, Object> getAny1() { return null; }

        @JsonAnyGetter
        public Map<String, Object> getAny2() { return null; }
    }

    static class SingleAnyGetter {
        @JsonAnyGetter
        public Map<String, Object> getAny() { return null; }
    }

    static class TwoJsonValues {
        @JsonValue
        public String value1() { return "v1"; }

        @JsonValue
        public String value2() { return "v2"; }
    }

    static class SingleJsonValue {
        @JsonValue
        public String value() { return "v"; }
    }

    static class SimpleBean {
        private String name;
        private boolean active;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    static class IgnoredPropertiesBean {
        @JsonIgnore
        public int ignoredField;

        public int normalField;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdentifiedBean {
        public int id;
    }

    @JsonDeserialize(builder = SimpleBuilder.class)
    static class BuiltPOJO {
    }

    static class SimpleBuilder {
    }

    static class CreatorBean {
        private final String name;
        private final int age;

        @JsonCreator
        public CreatorBean(@JsonProperty("name") String name, @JsonProperty("age") int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public int getAge() { return age; }
    }

    static class DuplicateInjectablesBean {
        @JacksonInject("duplicateId")
        public String field1;

        @JacksonInject("duplicateId")
        public String field2;
    }

    // Helper method to instantiate POJOPropertiesCollector
    private POJOPropertiesCollector collector(Class<?> cls, boolean forSerialization) {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(cls);
        MapperConfig<?> config = forSerialization ? mapper.getSerializationConfig() : mapper.getDeserializationConfig();
        AnnotatedClass ac = AnnotatedClass.construct(type, config);
        return new POJOPropertiesCollector(config, forSerialization, type, ac, "set");
    }

    // Tests defect where multiple @JsonAnySetter fields should report problem and not throw NPE
    @Test
    public void testGetAnySetterField_multipleFields_throwsIllegalArgumentException() {
        POJOPropertiesCollector coll = collector(TwoAnySetterFields.class, false);
        try {
            coll.getAnySetterField();
            fail("Expected IllegalArgumentException for multiple any-setter fields");
        } catch (IllegalArgumentException e) {
            assertTrue("Message should mention multiple any-setters, got: " + e.getMessage(),
                    e.getMessage().contains("Multiple 'any-Setters' defined"));
        }
    }

    // Tests single @JsonAnySetter field accessor
    @Test
    public void testGetAnySetterField_singleField_returnsAnnotatedMember() {
        POJOPropertiesCollector coll = collector(SingleAnySetterField.class, false);
        AnnotatedMember member = coll.getAnySetterField();
        assertNotNull(member);
        assertEquals("extra", member.getName());
    }

    // Tests getAnySetterField when no any-setter field is present
    @Test
    public void testGetAnySetterField_none_returnsNull() {
        POJOPropertiesCollector coll = collector(SimpleBean.class, false);
        assertNull(coll.getAnySetterField());
    }

    // Tests multiple @JsonAnySetter methods report problem
    @Test(expected = IllegalArgumentException.class)
    public void testGetAnySetterMethod_multipleMethods_throwsIllegalArgumentException() {
        POJOPropertiesCollector coll = collector(TwoAnySetterMethods.class, false);
        coll.getAnySetterMethod();
    }

    // Tests single @JsonAnySetter method
    @Test
    public void testGetAnySetterMethod_singleMethod_returnsAnnotatedMethod() {
        POJOPropertiesCollector coll = collector(SingleAnySetterMethod.class, false);
        AnnotatedMethod method = coll.getAnySetterMethod();
        assertNotNull(method);
        assertEquals("setAny", method.getName());
    }

    // Tests multiple @JsonAnyGetter methods report problem
    @Test(expected = IllegalArgumentException.class)
    public void testGetAnyGetter_multipleGetters_throwsIllegalArgumentException() {
        POJOPropertiesCollector coll = collector(TwoAnyGetters.class, true);
        coll.getAnyGetter();
    }

    // Tests single @JsonAnyGetter method
    @Test
    public void testGetAnyGetter_singleGetter_returnsAnnotatedMember() {
        POJOPropertiesCollector coll = collector(SingleAnyGetter.class, true);
        AnnotatedMember member = coll.getAnyGetter();
        assertNotNull(member);
        assertEquals("getAny", member.getName());
    }

    // Tests multiple @JsonValue methods report problem
    @Test(expected = IllegalArgumentException.class)
    public void testGetJsonValueMethod_multipleValues_throwsIllegalArgumentException() {
        POJOPropertiesCollector coll = collector(TwoJsonValues.class, true);
        coll.getJsonValueMethod();
    }

    // Tests single @JsonValue method
    @Test
    public void testGetJsonValueMethod_singleValue_returnsAnnotatedMethod() {
        POJOPropertiesCollector coll = collector(SingleJsonValue.class, true);
        AnnotatedMethod method = coll.getJsonValueMethod();
        assertNotNull(method);
        assertEquals("value", method.getName());
    }

    // Tests normal property collection for standard getters and setters
    @Test
    public void testGetProperties_simpleBean_collectsProperties() {
        POJOPropertiesCollector coll = collector(SimpleBean.class, false);
        List<BeanPropertyDefinition> props = coll.getProperties();
        assertNotNull(props);
        assertEquals(2, props.size());

        Set<String> propNames = new HashSet<String>();
        for (BeanPropertyDefinition prop : props) {
            propNames.add(prop.getName());
        }
        assertTrue(propNames.contains("name"));
        assertTrue(propNames.contains("active"));
    }

    // Tests ignored property collection
    @Test
    public void testGetIgnoredPropertyNames_ignoredField_containsIgnoredName() {
        POJOPropertiesCollector coll = collector(IgnoredPropertiesBean.class, false);
        coll.getProperties(); // trigger collection
        Set<String> ignored = coll.getIgnoredPropertyNames();
        assertNotNull(ignored);
        assertTrue(ignored.contains("ignoredField"));
        assertFalse(ignored.contains("normalField"));
    }

    // Tests object id info retrieval
    @Test
    public void testGetObjectIdInfo_annotatedBean_returnsObjectIdInfo() {
        POJOPropertiesCollector coll = collector(IdentifiedBean.class, false);
        ObjectIdInfo info = coll.getObjectIdInfo();
        assertNotNull(info);
        assertEquals("id", info.getPropertyName().getSimpleName());
    }

    // Tests finding POJO builder class
    @Test
    public void testFindPOJOBuilderClass_builderAnnotated_returnsBuilderClass() {
        POJOPropertiesCollector coll = collector(BuiltPOJO.class, false);
        Class<?> builderClass = coll.findPOJOBuilderClass();
        assertEquals(SimpleBuilder.class, builderClass);
    }

    // Tests creator property collection
    @Test
    public void testGetProperties_creatorBean_collectsCreatorProperties() {
        POJOPropertiesCollector coll = collector(CreatorBean.class, false);
        List<BeanPropertyDefinition> props = coll.getProperties();
        assertNotNull(props);
        assertEquals(2, props.size());

        Set<String> names = new HashSet<String>();
        for (BeanPropertyDefinition prop : props) {
            names.add(prop.getName());
            assertTrue(prop.hasConstructorParameter());
        }
        assertTrue(names.contains("name"));
        assertTrue(names.contains("age"));
    }

    // Tests duplicate injectable values throw exception
    @Test(expected = IllegalArgumentException.class)
    public void testGetInjectables_duplicateId_throwsIllegalArgumentException() {
        POJOPropertiesCollector coll = collector(DuplicateInjectablesBean.class, false);
        coll.getInjectables();
    }

    // Tests basic accessors: config, type, classDef, and annotationIntrospector
    @Test
    public void testAccessors_validCollector_returnNonNullValues() {
        POJOPropertiesCollector coll = collector(SimpleBean.class, true);
        assertNotNull(coll.getConfig());
        assertNotNull(coll.getType());
        assertNotNull(coll.getClassDef());
        assertNotNull(coll.getAnnotationIntrospector());
    }
}