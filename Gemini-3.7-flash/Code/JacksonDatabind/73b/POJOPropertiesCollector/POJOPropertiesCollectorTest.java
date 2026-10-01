package com.fasterxml.jackson.databind.introspect;

import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.MapperConfig;

public class POJOPropertiesCollectorTest {

    // Helper classes for testing
    static class SimpleBean {
        public int x;
        private int y;

        public int getY() { return y; }
        public void setY(int value) { this.y = value; }
    }

    static class IgnoredBean {
        @JsonIgnore
        public int ignoredField;
        public int visibleField;

        @JsonIgnore
        public int getIgnoredMethod() { return 0; }
        public void setIgnoredMethod(int v) {}
    }

    static class RenamedBean {
        @JsonProperty("customName")
        public int originalName;
    }

    static class MultipleJsonValueBean {
        @JsonValue
        public int value1() { return 1; }

        @JsonValue
        public int value2() { return 2; }
    }

    static class SingleJsonValueBean {
        @JsonValue
        public int value() { return 1; }
    }

    static class MultipleAnyGetterBean {
        @JsonAnyGetter
        public Map<String, Object> any1() { return Collections.emptyMap(); }

        @JsonAnyGetter
        public Map<String, Object> any2() { return Collections.emptyMap(); }
    }

    static class SingleAnyGetterBean {
        @JsonAnyGetter
        public Map<String, Object> any() { return Collections.emptyMap(); }
    }

    static class MultipleAnySetterMethodBean {
        @JsonAnySetter
        public void setAny1(String key, Object value) {}

        @JsonAnySetter
        public void setAny2(String key, Object value) {}
    }

    static class SingleAnySetterMethodBean {
        @JsonAnySetter
        public void setAny(String key, Object value) {}
    }

    static class MultipleAnySetterFieldBean {
        @JsonAnySetter
        public Map<String, Object> anyField1;

        @JsonAnySetter
        public Map<String, Object> anyField2;
    }

    static class SingleAnySetterFieldBean {
        @JsonAnySetter
        public Map<String, Object> anyField;
    }

    static class InjectableBean {
        @JacksonInject("id1")
        public String injectedField;

        @JacksonInject("id2")
        public void setInjectedMethod(String val) {}
    }

    static class DuplicateInjectableBean {
        @JacksonInject("sameId")
        public String field1;

        @JacksonInject("sameId")
        public String field2;
    }

    @JsonPropertyOrder({ "b", "a", "c" })
    static class OrderedBean {
        public int c;
        public int a;
        public int b;
    }

    static class CreatorBean {
        public int x;
        public int y;

        public CreatorBean(@JsonProperty("x") int x, @JsonProperty("y") int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class FieldJsonValueBean {
        @JsonValue
        public int valueField = 10;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdentityBean {
        public int id;
    }

    @JsonPropertyOrder(alphabetic = true)
    static class AlphabeticOrderedBean {
        public int z;
        public int a;
        public int m;
    }

    static class InjectedCreatorParamBean {
        public int a;

        public InjectedCreatorParamBean(@JsonProperty("a") int a, @JacksonInject("injectedParam") String b) {
            this.a = a;
        }
    }

    static class CustomGetterSetterBean {
        private int val;

        @JsonGetter("customProp")
        public int getMyVal() { return val; }

        @JsonSetter("customProp")
        public void setMyVal(int v) { this.val = v; }
    }

    static class CamelCaseBean {
        public int myPropertyField;
    }

    static class BooleanBean {
        private boolean active;

        public boolean isActive() { return active; }
        public void setActive(boolean a) { this.active = a; }
    }

    static class PrefixBean {
        private int x;

        public int getX() { return x; }
        public void setX(int v) { this.x = v; }
    }

    private POJOPropertiesCollector createCollector(Class<?> cls, boolean forSerialization) {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(cls);
        MapperConfig<?> config = forSerialization ? mapper.getSerializationConfig() : mapper.getDeserializationConfig();
        AnnotatedClass ac = AnnotatedClass.construct(type, config);
        return new POJOPropertiesCollector(config, forSerialization, type, ac, null);
    }

    // Tests collecting normal properties for serialization and deserialization
    @Test
    public void testGetProperties_simpleBean_returnsProperties() {
        POJOPropertiesCollector collector = createCollector(SimpleBean.class, true);
        List<BeanPropertyDefinition> props = collector.getProperties();
        assertNotNull(props);
        assertEquals(2, props.size());

        Set<String> names = new HashSet<String>();
        for (BeanPropertyDefinition prop : props) {
            names.add(prop.getName());
        }
        assertTrue(names.contains("x"));
        assertTrue(names.contains("y"));
    }

    // Tests configuration, type, classDef, and introspector accessors
    @Test
    public void testAccessors_initializedCorrectly_returnsNonNull() {
        POJOPropertiesCollector collector = createCollector(SimpleBean.class, false);
        assertNotNull(collector.getConfig());
        assertNotNull(collector.getType());
        assertNotNull(collector.getClassDef());
        assertNotNull(collector.getAnnotationIntrospector());
    }

    // Tests explicitly ignored properties collection
    @Test
    public void testGetIgnoredPropertyNames_deserialization_containsIgnoredNames() {
        POJOPropertiesCollector collector = createCollector(IgnoredBean.class, false);
        List<BeanPropertyDefinition> props = collector.getProperties();
        assertNotNull(props);
        assertEquals(1, props.size());
        assertEquals("visibleField", props.get(0).getName());

        Set<String> ignored = collector.getIgnoredPropertyNames();
        assertNotNull(ignored);
        assertTrue(ignored.contains("ignoredField"));
    }

    // Tests renamed properties via @JsonProperty
    @Test
    public void testRenameProperties_customName_returnsRenamedProperty() {
        POJOPropertiesCollector collector = createCollector(RenamedBean.class, true);
        List<BeanPropertyDefinition> props = collector.getProperties();
        assertEquals(1, props.size());
        assertEquals("customName", props.get(0).getName());
    }

    // Tests single @JsonValue method detection
    @Test
    public void testGetJsonValueMethod_singleAnnotation_returnsMethod() {
        POJOPropertiesCollector collector = createCollector(SingleJsonValueBean.class, true);
        AnnotatedMethod method = collector.getJsonValueMethod();
        assertNotNull(method);
        assertEquals("value", method.getName());
    }

    // Tests multiple @JsonValue methods reporting problem
    @Test(expected = IllegalArgumentException.class)
    public void testGetJsonValueMethod_multipleAnnotations_throwsException() {
        POJOPropertiesCollector collector = createCollector(MultipleJsonValueBean.class, true);
        collector.getJsonValueMethod();
    }

    // Tests single @JsonAnyGetter annotation
    @Test
    public void testGetAnyGetter_singleAnnotation_returnsMember() {
        POJOPropertiesCollector collector = createCollector(SingleAnyGetterBean.class, true);
        AnnotatedMember member = collector.getAnyGetter();
        assertNotNull(member);
        assertEquals("any", member.getName());
    }

    // Tests multiple @JsonAnyGetter annotations reporting problem
    @Test(expected = IllegalArgumentException.class)
    public void testGetAnyGetter_multipleAnnotations_throwsException() {
        POJOPropertiesCollector collector = createCollector(MultipleAnyGetterBean.class, true);
        collector.getAnyGetter();
    }

    // Tests single @JsonAnySetter method annotation
    @Test
    public void testGetAnySetterMethod_singleAnnotation_returnsMethod() {
        POJOPropertiesCollector collector = createCollector(SingleAnySetterMethodBean.class, false);
        AnnotatedMethod method = collector.getAnySetterMethod();
        assertNotNull(method);
        assertEquals("setAny", method.getName());
    }

    // Tests multiple @JsonAnySetter method annotations reporting problem
    @Test(expected = IllegalArgumentException.class)
    public void testGetAnySetterMethod_multipleAnnotations_throwsException() {
        POJOPropertiesCollector collector = createCollector(MultipleAnySetterMethodBean.class, false);
        collector.getAnySetterMethod();
    }

    // Tests single @JsonAnySetter field annotation
    @Test
    public void testGetAnySetterField_singleAnnotation_returnsField() {
        POJOPropertiesCollector collector = createCollector(SingleAnySetterFieldBean.class, false);
        AnnotatedMember field = collector.getAnySetterField();
        assertNotNull(field);
        assertEquals("anyField", field.getName());
    }

    // Tests multiple @JsonAnySetter field annotations reporting problem
    @Test(expected = IllegalArgumentException.class)
    public void testGetAnySetterField_multipleAnnotations_throwsException() {
        POJOPropertiesCollector collector = createCollector(MultipleAnySetterFieldBean.class, false);
        collector.getAnySetterField();
    }

    // Tests collection of @JacksonInject annotations
    @Test
    public void testGetInjectables_validAnnotations_returnsInjectablesMap() {
        POJOPropertiesCollector collector = createCollector(InjectableBean.class, false);
        Map<Object, AnnotatedMember> injectables = collector.getInjectables();
        assertNotNull(injectables);
        assertEquals(2, injectables.size());
        assertTrue(injectables.containsKey("id1"));
        assertTrue(injectables.containsKey("id2"));
    }

    // Tests duplicate injectable id throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetInjectables_duplicateId_throwsException() {
        POJOPropertiesCollector collector = createCollector(DuplicateInjectableBean.class, false);
        collector.getInjectables();
    }

    // Tests property sorting by @JsonPropertyOrder
    @Test
    public void testSortProperties_orderedBean_sortsPropertiesCorrectly() {
        POJOPropertiesCollector collector = createCollector(OrderedBean.class, true);
        List<BeanPropertyDefinition> props = collector.getProperties();
        assertEquals(3, props.size());
        assertEquals("b", props.get(0).getName());
        assertEquals("a", props.get(1).getName());
        assertEquals("c", props.get(2).getName());
    }

    // Tests creator properties collection
    @Test
    public void testCreators_constructorParams_propertiesCollected() {
        POJOPropertiesCollector collector = createCollector(CreatorBean.class, false);
        List<BeanPropertyDefinition> props = collector.getProperties();
        assertEquals(2, props.size());
        assertTrue(props.get(0).hasConstructorParameter() || props.get(1).hasConstructorParameter());
    }

    // Tests deprecated collect method returns this
    @Test
    @SuppressWarnings("deprecation")
    public void testCollect_deprecatedMethod_returnsSelf() {
        POJOPropertiesCollector collector = createCollector(SimpleBean.class, true);
        assertSame(collector, collector.collect());
    }

    // Tests single @JsonValue on field via getJsonValueAccessor
    @Test
    public void testGetJsonValueAccessor_fieldAnnotation_returnsMember() {
        POJOPropertiesCollector collector = createCollector(FieldJsonValueBean.class, true);
        AnnotatedMember member = collector.getJsonValueAccessor();
        assertNotNull(member);
        assertEquals("valueField", member.getName());
    }

    // Tests @JsonIdentityInfo extraction
    @Test
    public void testGetObjectIdInfo_identityInfoPresent_returnsObjectIdInfo() {
        POJOPropertiesCollector collector = createCollector(IdentityBean.class, true);
        assertNotNull(collector.getObjectIdInfo());
        assertEquals("id", collector.getObjectIdInfo().getPropertyName().getSimpleName());
    }

    // Tests alphabetic sorting via @JsonPropertyOrder(alphabetic = true)
    @Test
    public void testSortProperties_alphabeticOrder_sortsAlphabetically() {
        POJOPropertiesCollector collector = createCollector(AlphabeticOrderedBean.class, true);
        List<BeanPropertyDefinition> props = collector.getProperties();
        assertEquals(3, props.size());
        assertEquals("a", props.get(0).getName());
        assertEquals("m", props.get(1).getName());
        assertEquals("z", props.get(2).getName());
    }

    // Tests getPropertyMap accessor
    @Test
    public void testGetPropertyMap_simpleBean_returnsMap() {
        POJOPropertiesCollector collector = createCollector(SimpleBean.class, true);
        Map<String, POJOPropertyBuilder> map = collector.getPropertyMap();
        assertNotNull(map);
        assertTrue(map.containsKey("x"));
        assertTrue(map.containsKey("y"));
    }

    // Tests creator parameter injection extraction
    @Test
    public void testGetInjectables_creatorParameter_returnsInjectable() {
        POJOPropertiesCollector collector = createCollector(InjectedCreatorParamBean.class, false);
        Map<Object, AnnotatedMember> injectables = collector.getInjectables();
        assertNotNull(injectables);
        assertTrue(injectables.containsKey("injectedParam"));
    }

    // Tests matching @JsonGetter and @JsonSetter merged into one property definition
    @Test
    public void testGetterSetter_customNames_mergedIntoSingleProperty() {
        POJOPropertiesCollector collector = createCollector(CustomGetterSetterBean.class, true);
        List<BeanPropertyDefinition> props = collector.getProperties();
        assertEquals(1, props.size());
        assertEquals("customProp", props.get(0).getName());
    }

    // Tests property naming strategy applied during collection
    @Test
    public void testPropertyNamingStrategy_snakeCase_renamesProperties() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        JavaType type = mapper.constructType(CamelCaseBean.class);
        MapperConfig<?> config = mapper.getSerializationConfig();
        AnnotatedClass ac = AnnotatedClass.construct(type, config);
        POJOPropertiesCollector collector = new POJOPropertiesCollector(config, true, type, ac, null);
        List<BeanPropertyDefinition> props = collector.getProperties();
        assertEquals(1, props.size());
        assertEquals("my_property_field", props.get(0).getName());
    }

    // Tests collector behavior when annotations are disabled / no introspector
    @Test
    public void testNoAnnotationIntrospector_simpleBean_collectsProperties() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.USE_ANNOTATIONS);
        JavaType type = mapper.constructType(SimpleBean.class);
        MapperConfig<?> config = mapper.getSerializationConfig();
        AnnotatedClass ac = AnnotatedClass.construct(type, config);
        POJOPropertiesCollector collector = new POJOPropertiesCollector(config, true, type, ac, null);
        assertNull(collector.getAnnotationIntrospector());
        List<BeanPropertyDefinition> props = collector.getProperties();
        assertNotNull(props);
        assertFalse(props.isEmpty());
    }

    // Tests boolean 'is' getter method handling
    @Test
    public void testBooleanIsGetter_collectsProperty() {
        POJOPropertiesCollector collector = createCollector(BooleanBean.class, true);
        List<BeanPropertyDefinition> props = collector.getProperties();
        assertEquals(1, props.size());
        assertEquals("active", props.get(0).getName());
    }

    // Tests custom mutator prefix passed to POJOPropertiesCollector
    @Test
    public void testCustomMutatorPrefix_isSupported() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(PrefixBean.class);
        MapperConfig<?> config = mapper.getDeserializationConfig();
        AnnotatedClass ac = AnnotatedClass.construct(type, config);
        POJOPropertiesCollector collector = new POJOPropertiesCollector(config, false, type, ac, "set");
        List<BeanPropertyDefinition> props = collector.getProperties();
        assertNotNull(props);
        assertEquals(1, props.size());
        assertEquals("x", props.get(0).getName());
    }
}