package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class BeanDeserializerTest {

    private final ObjectMapper MAPPER = new ObjectMapper();

    // =======================================================================
    // Helper POJOs
    // =======================================================================

    static class SimpleBean {
        public String name;
        public int age;

        public SimpleBean() {}

        public SimpleBean(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    static class CreatorBean {
        final String name;
        final int age;
        public String extra;

        @JsonCreator
        public CreatorBean(@JsonProperty("name") String name, @JsonProperty("age") int age) {
            this.name = name;
            this.age = age;
        }
    }

    static class AnySetterBean {
        public String name;
        private final Map<String, Object> any = new HashMap<String, Object>();

        @JsonAnySetter
        public void setAny(String key, Object value) {
            any.put(key, value);
        }

        public Map<String, Object> getAny() {
            return any;
        }
    }

    @JsonIgnoreProperties({"ignored1", "ignored2"})
    static class IgnorableBean {
        public String name;
    }

    static class Views {
        static class Public {}
        static class Internal extends Public {}
    }

    static class ViewBean {
        @JsonView(Views.Public.class)
        public String pub;

        @JsonView(Views.Internal.class)
        public String priv;
    }

    static class UnwrappedChild {
        public String street;
        public String city;
    }

    static class UnwrappedParent {
        public String name;
        @JsonUnwrapped
        public UnwrappedChild address;
    }

    static class UnwrappedCreatorParent {
        public String name;
        @JsonUnwrapped
        public UnwrappedChild address;

        @JsonCreator
        public UnwrappedCreatorParent(@JsonProperty("name") String name, @JsonProperty("address") UnwrappedChild address) {
            this.name = name;
            this.address = address;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
    static class IdentifiedBean {
        public String name;
        public IdentifiedBean next;
    }

    static class ExternalBase {
        public int id;
    }

    static class ExternalSub extends ExternalBase {
        public String value;
    }

    static class ExternalValueHolder {
        public int id;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "extType")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = ExternalSub.class, name = "sub")
        })
        public ExternalBase value;
        public String extType;

        @JsonCreator
        public ExternalValueHolder(@JsonProperty("id") int id,
                                   @JsonProperty("value") ExternalBase value,
                                   @JsonProperty("extType") String extType) {
            this.id = id;
            this.value = value;
            this.extType = extType;
        }
    }

    static class ScalarCreatorBean {
        String value;

        @JsonCreator
        public ScalarCreatorBean(String v) {
            this.value = "str:" + v;
        }

        @JsonCreator
        public ScalarCreatorBean(int v) {
            this.value = "int:" + v;
        }

        @JsonCreator
        public ScalarCreatorBean(double v) {
            this.value = "double:" + v;
        }

        @JsonCreator
        public ScalarCreatorBean(boolean v) {
            this.value = "bool:" + v;
        }
    }

    // =======================================================================
    // Test Cases
    // =======================================================================

    // Tests vanilla deserialization from standard JSON Object
    @Test
    public void testDeserialize_standardBean_returnsCorrectInstance() throws Exception {
        String json = "{\"name\":\"Bob\",\"age\":25}";
        SimpleBean result = MAPPER.readValue(json, SimpleBean.class);

        assertNotNull(result);
        assertEquals("Bob", result.name);
        assertEquals(25, result.age);
    }

    // Tests updating an existing bean instance (deserialize with bean argument)
    @Test
    public void testDeserialize_updateExistingBean_mutatesTargetInstance() throws Exception {
        SimpleBean bean = new SimpleBean("Alice", 20);
        String json = "{\"age\":30}";
        SimpleBean result = MAPPER.readerForUpdating(bean).readValue(json);

        assertSame(bean, result);
        assertEquals("Alice", result.name);
        assertEquals(30, result.age);
    }

    // Tests property-based creator deserialization
    @Test
    public void testDeserializeUsingPropertyBased_validCreatorFields_buildsBeanCorrectly() throws Exception {
        String json = "{\"extra\":\"bonus\",\"name\":\"Charlie\",\"age\":40}";
        CreatorBean result = MAPPER.readValue(json, CreatorBean.class);

        assertNotNull(result);
        assertEquals("Charlie", result.name);
        assertEquals(40, result.age);
        assertEquals("bonus", result.extra);
    }

    // Tests handling of @JsonIgnoreProperties in bean deserialization
    @Test
    public void testDeserialize_ignorableProperties_skipsIgnoredFields() throws Exception {
        String json = "{\"name\":\"David\",\"ignored1\":\"skipMe\",\"ignored2\":12345}";
        IgnorableBean result = MAPPER.readValue(json, IgnorableBean.class);

        assertNotNull(result);
        assertEquals("David", result.name);
    }

    // Tests @JsonAnySetter handling for unexpected/extra fields
    @Test
    public void testDeserialize_anySetter_capturesUnknownProperties() throws Exception {
        String json = "{\"name\":\"Eve\",\"customField1\":\"val1\",\"customField2\":999}";
        AnySetterBean result = MAPPER.readValue(json, AnySetterBean.class);

        assertNotNull(result);
        assertEquals("Eve", result.name);
        assertEquals("val1", result.getAny().get("customField1"));
        assertEquals(999, result.getAny().get("customField2"));
    }

    // Tests deserialization with active @JsonView
    @Test
    public void testDeserializeWithView_publicView_excludesPrivateProperty() throws Exception {
        String json = "{\"pub\":\"visible\",\"priv\":\"hidden\"}";
        ViewBean result = MAPPER.readerWithView(Views.Public.class)
                .forType(ViewBean.class)
                .readValue(json);

        assertNotNull(result);
        assertEquals("visible", result.pub);
        assertNull(result.priv);
    }

    // Tests deserialization with active @JsonView including internal views
    @Test
    public void testDeserializeWithView_internalView_includesAllProperties() throws Exception {
        String json = "{\"pub\":\"visible\",\"priv\":\"secret\"}";
        ViewBean result = MAPPER.readerWithView(Views.Internal.class)
                .forType(ViewBean.class)
                .readValue(json);

        assertNotNull(result);
        assertEquals("visible", result.pub);
        assertEquals("secret", result.priv);
    }

    // Tests unwrapped property deserialization
    @Test
    public void testDeserializeWithUnwrapped_flatJson_populatesNestedBean() throws Exception {
        String json = "{\"name\":\"Frank\",\"street\":\"1st Main\",\"city\":\"Metro\"}";
        UnwrappedParent result = MAPPER.readValue(json, UnwrappedParent.class);

        assertNotNull(result);
        assertEquals("Frank", result.name);
        assertNotNull(result.address);
        assertEquals("1st Main", result.address.street);
        assertEquals("Metro", result.address.city);
    }

    // Tests unwrapped property deserialization combined with @JsonCreator
    @Test
    public void testDeserializeUsingPropertyBasedWithUnwrapped_flatJson_buildsBean() throws Exception {
        String json = "{\"name\":\"Grace\",\"street\":\"2nd Ave\",\"city\":\"Gotham\"}";
        UnwrappedCreatorParent result = MAPPER.readValue(json, UnwrappedCreatorParent.class);

        assertNotNull(result);
        assertEquals("Grace", result.name);
        assertNotNull(result.address);
        assertEquals("2nd Ave", result.address.street);
        assertEquals("Gotham", result.address.city);
    }

    // Tests Object Id resolution (@JsonIdentityInfo)
    @Test
    public void testDeserializeWithObjectId_cyclicalOrSelfReference_resolvesId() throws Exception {
        String json = "{\"id\":1,\"name\":\"Node1\",\"next\":1}";
        IdentifiedBean result = MAPPER.readValue(json, IdentifiedBean.class);

        assertNotNull(result);
        assertEquals("Node1", result.name);
        assertSame(result, result.next);
    }

    // Tests Property-based creator with External Type Id (Defects4J Bug 27 target branch)
    @Test
    public void testDeserializeUsingPropertyBasedWithExternalTypeId_typeFirst_deserializesSubtype() throws Exception {
        String json = "{\"extType\":\"sub\",\"id\":42,\"value\":{\"id\":100,\"value\":\"childData\"}}";
        ExternalValueHolder result = MAPPER.readValue(json, ExternalValueHolder.class);

        assertNotNull(result);
        assertEquals(42, result.id);
        assertEquals("sub", result.extType);
        assertTrue(result.value instanceof ExternalSub);
        assertEquals("childData", ((ExternalSub) result.value).value);
    }

    // Tests Property-based creator with External Type Id when type property appears after value
    @Test
    public void testDeserializeUsingPropertyBasedWithExternalTypeId_valueFirst_deserializesSubtype() throws Exception {
        String json = "{\"id\":42,\"value\":{\"id\":100,\"value\":\"childData\"},\"extType\":\"sub\"}";
        ExternalValueHolder result = MAPPER.readValue(json, ExternalValueHolder.class);

        assertNotNull(result);
        assertEquals(42, result.id);
        assertEquals("sub", result.extType);
        assertTrue(result.value instanceof ExternalSub);
        assertEquals("childData", ((ExternalSub) result.value).value);
    }

    // Tests deserialization from String scalar token via @JsonCreator
    @Test
    public void testDeserializeFromString_stringToken_delegatesToCreator() throws Exception {
        ScalarCreatorBean result = MAPPER.readValue("\"hello\"", ScalarCreatorBean.class);

        assertNotNull(result);
        assertEquals("str:hello", result.value);
    }

    // Tests deserialization from Integer scalar token via @JsonCreator
    @Test
    public void testDeserializeFromNumber_intToken_delegatesToCreator() throws Exception {
        ScalarCreatorBean result = MAPPER.readValue("123", ScalarCreatorBean.class);

        assertNotNull(result);
        assertEquals("int:123", result.value);
    }

    // Tests deserialization from Double scalar token via @JsonCreator
    @Test
    public void testDeserializeFromDouble_floatToken_delegatesToCreator() throws Exception {
        ScalarCreatorBean result = MAPPER.readValue("12.5", ScalarCreatorBean.class);

        assertNotNull(result);
        assertEquals("double:12.5", result.value);
    }

    // Tests deserialization from Boolean scalar token via @JsonCreator
    @Test
    public void testDeserializeFromBoolean_booleanToken_delegatesToCreator() throws Exception {
        ScalarCreatorBean result = MAPPER.readValue("true", ScalarCreatorBean.class);

        assertNotNull(result);
        assertEquals("bool:true", result.value);
    }

    // Tests unknown property throwing exception on default settings
    @Test(expected = UnrecognizedPropertyException.class)
    public void testDeserialize_unknownProperty_throwsUnrecognizedPropertyException() throws Exception {
        String json = "{\"name\":\"Bob\",\"unknownProp\":\"xyz\"}";
        MAPPER.readValue(json, SimpleBean.class);
    }
}