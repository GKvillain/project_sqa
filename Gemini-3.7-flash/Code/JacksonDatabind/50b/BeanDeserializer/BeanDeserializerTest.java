package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class BeanDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // Helper Beans

    static class SimpleBean {
        public int id;
        public String name;

        public SimpleBean() {}

        public SimpleBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    static class CreatorBean {
        private final int id;
        private final String name;
        private String extra;

        @JsonCreator
        public CreatorBean(@JsonProperty("id") int id, @JsonProperty("name") String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getExtra() {
            return extra;
        }

        public void setExtra(String extra) {
            this.extra = extra;
        }
    }

    @JsonIgnoreProperties({"ignored"})
    static class IgnorableBean {
        public int id;
        public String name;

        @JsonCreator
        public IgnorableBean(@JsonProperty("id") int id, @JsonProperty("name") String name) {
            this.id = id;
            this.name = name;
        }
    }

    static class AnySetterBean {
        public int id;
        public Map<String, Object> any = new HashMap<String, Object>();

        @JsonCreator
        public AnySetterBean(@JsonProperty("id") int id) {
            this.id = id;
        }

        @JsonAnySetter
        public void setAny(String key, Object value) {
            any.put(key, value);
        }
    }

    static class Views {
        static class Public {}
        static class Internal extends Public {}
    }

    static class ViewBean {
        @JsonView(Views.Public.class)
        public String publicField;

        @JsonView(Views.Internal.class)
        public String internalField;
    }

    static class Location {
        public String city;
        public String country;
    }

    static class UnwrappedBean {
        public String name;

        @JsonUnwrapped
        public Location location;
    }

    static class UnwrappedCreatorBean {
        public String name;

        @JsonUnwrapped
        public Location location;

        @JsonCreator
        public UnwrappedCreatorBean(@JsonProperty("name") String name) {
            this.name = name;
        }
    }

    static class ExternalTypeContainer {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = SubTypeA.class, name = "typeA"),
            @JsonSubTypes.Type(value = SubTypeB.class, name = "typeB")
        })
        public BaseType value;
    }

    static class ExternalTypeCreatorContainer {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = SubTypeA.class, name = "typeA"),
            @JsonSubTypes.Type(value = SubTypeB.class, name = "typeB")
        })
        public BaseType value;

        @JsonCreator
        public ExternalTypeCreatorContainer(@JsonProperty("type") String type) {
            this.type = type;
        }
    }

    static abstract class BaseType {}

    static class SubTypeA extends BaseType {
        public int a;
    }

    static class SubTypeB extends BaseType {
        public String b;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdBean {
        public int id;
        public String name;
        public IdBean next;

        public IdBean() {}

        public IdBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    static class NullReturningCreatorBean {
        @JsonCreator
        public static NullReturningCreatorBean create(@JsonProperty("value") String val) {
            return null;
        }
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    static class AsArrayBean {
        public int id;
        public String name;
        public boolean active;

        public AsArrayBean() {}

        public AsArrayBean(int id, String name, boolean active) {
            this.id = id;
            this.name = name;
            this.active = active;
        }
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    static class AsArrayCreatorBean {
        public int id;
        public String name;

        @JsonCreator
        public AsArrayCreatorBean(@JsonProperty("id") int id, @JsonProperty("name") String name) {
            this.id = id;
            this.name = name;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class IgnoreUnknownBean {
        public int id;
        public String name;
    }

    static class StringDelegatingBean {
        private final String value;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public StringDelegatingBean(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    static class IntDelegatingBean {
        private final int value;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public IntDelegatingBean(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    static class BooleanDelegatingBean {
        private final boolean value;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public BooleanDelegatingBean(boolean value) {
            this.value = value;
        }

        public boolean isValue() {
            return value;
        }
    }

    static class DoubleDelegatingBean {
        private final double value;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public DoubleDelegatingBean(double value) {
            this.value = value;
        }

        public double getValue() {
            return value;
        }
    }

    @JsonDeserialize(builder = ValueObject.Builder.class)
    static class ValueObject {
        private final int id;
        private final String name;

        private ValueObject(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @JsonPOJOBuilder(withPrefix = "set")
        static class Builder {
            private int id;
            private String name;

            public Builder setId(int id) {
                this.id = id;
                return this;
            }

            public Builder setName(String name) {
                this.name = name;
                return this;
            }

            public ValueObject build() {
                return new ValueObject(id, name);
            }
        }
    }

    static class ParentRefBean {
        public int id;
        @JsonManagedReference
        public List<ChildRefBean> children = new ArrayList<ChildRefBean>();
    }

    static class ChildRefBean {
        public String name;
        @JsonBackReference
        public ParentRefBean parent;
    }

    static class AccessControlledBean {
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        public String writeOnly;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        public String readOnly;
    }

    static class PrimitiveBean {
        public int num;
        public boolean flag;
    }

    // Tests

    // Tests vanilla deserialization for simple POJO
    @Test
    public void testDeserialize_vanillaProcessing_success() throws Exception {
        String json = "{\"id\":101,\"name\":\"John\"}";
        SimpleBean result = mapper.readValue(json, SimpleBean.class);

        assertNotNull(result);
        assertEquals(101, result.id);
        assertEquals("John", result.name);
    }

    // Tests property-based creator deserialization
    @Test
    public void testDeserialize_propertyBasedCreator_success() throws Exception {
        String json = "{\"id\":202,\"name\":\"Jane\"}";
        CreatorBean result = mapper.readValue(json, CreatorBean.class);

        assertNotNull(result);
        assertEquals(202, result.getId());
        assertEquals("Jane", result.getName());
    }

    // Tests property-based creator with regular setter properties
    @Test
    public void testDeserialize_propertyBasedCreatorWithExtraProperty_success() throws Exception {
        String json = "{\"extra\":\"ExtraValue\",\"name\":\"Bob\",\"id\":303}";
        CreatorBean result = mapper.readValue(json, CreatorBean.class);

        assertNotNull(result);
        assertEquals(303, result.getId());
        assertEquals("Bob", result.getName());
        assertEquals("ExtraValue", result.getExtra());
    }

    // Tests property-based creator with ignorable property
    @Test
    public void testDeserialize_propertyBasedCreatorWithIgnoredProperty_success() throws Exception {
        String json = "{\"ignored\":\"shouldBeSkipped\",\"id\":404,\"name\":\"Alice\"}";
        IgnorableBean result = mapper.readValue(json, IgnorableBean.class);

        assertNotNull(result);
        assertEquals(404, result.id);
        assertEquals("Alice", result.name);
    }

    // Tests property-based creator with any-setter
    @Test
    public void testDeserialize_propertyBasedCreatorWithAnySetter_capturesAnyProperties() throws Exception {
        String json = "{\"id\":505,\"customField\":\"customVal\",\"otherField\":123}";
        AnySetterBean result = mapper.readValue(json, AnySetterBean.class);

        assertNotNull(result);
        assertEquals(505, result.id);
        assertEquals("customVal", result.any.get("customField"));
        assertEquals(123, result.any.get("otherField"));
    }

    // Tests deserialization into an existing object instance
    @Test
    public void testDeserialize_updatingExistingBean_updatesFields() throws Exception {
        SimpleBean existing = new SimpleBean(1, "Original");
        String json = "{\"name\":\"Updated\"}";
        SimpleBean result = mapper.readerForUpdating(existing).readValue(json);

        assertSame(existing, result);
        assertEquals(1, result.id);
        assertEquals("Updated", result.name);
    }

    // Tests deserialization with JsonView active
    @Test
    public void testDeserialize_withActiveView_filtersProperties() throws Exception {
        String json = "{\"publicField\":\"pubVal\",\"internalField\":\"intVal\"}";
        ViewBean result = mapper.readerWithView(Views.Public.class)
                .forType(ViewBean.class)
                .readValue(json);

        assertNotNull(result);
        assertEquals("pubVal", result.publicField);
        assertNull(result.internalField);
    }

    // Tests deserialization with unwrapped properties on standard bean
    @Test
    public void testDeserialize_withUnwrappedProperties_success() throws Exception {
        String json = "{\"name\":\"Office\",\"city\":\"Bangkok\",\"country\":\"Thailand\"}";
        UnwrappedBean result = mapper.readValue(json, UnwrappedBean.class);

        assertNotNull(result);
        assertEquals("Office", result.name);
        assertNotNull(result.location);
        assertEquals("Bangkok", result.location.city);
        assertEquals("Thailand", result.location.country);
    }

    // Tests deserialization with unwrapped properties on property-based creator bean
    @Test
    public void testDeserialize_withUnwrappedPropertyBasedCreator_success() throws Exception {
        String json = "{\"city\":\"Tokyo\",\"name\":\"Headquarter\",\"country\":\"Japan\"}";
        UnwrappedCreatorBean result = mapper.readValue(json, UnwrappedCreatorBean.class);

        assertNotNull(result);
        assertEquals("Headquarter", result.name);
        assertNotNull(result.location);
        assertEquals("Tokyo", result.location.city);
        assertEquals("Japan", result.location.country);
    }

    // Tests deserialization with external type id
    @Test
    public void testDeserialize_withExternalTypeId_success() throws Exception {
        String json = "{\"type\":\"typeA\",\"value\":{\"a\":42}}";
        ExternalTypeContainer result = mapper.readValue(json, ExternalTypeContainer.class);

        assertNotNull(result);
        assertEquals("typeA", result.type);
        assertTrue(result.value instanceof SubTypeA);
        assertEquals(42, ((SubTypeA) result.value).a);
    }

    // Tests deserialization with external type id and property-based creator
    @Test
    public void testDeserialize_withExternalTypeIdAndPropertyBasedCreator_success() throws Exception {
        String json = "{\"value\":{\"b\":\"testStr\"},\"type\":\"typeB\"}";
        ExternalTypeCreatorContainer result = mapper.readValue(json, ExternalTypeCreatorContainer.class);

        assertNotNull(result);
        assertEquals("typeB", result.type);
        assertTrue(result.value instanceof SubTypeB);
        assertEquals("testStr", ((SubTypeB) result.value).b);
    }

    // Tests deserialization with Object Id references
    @Test
    public void testDeserialize_withObjectIdReference_resolvesReferences() throws Exception {
        String json = "{\"id\":1,\"name\":\"Parent\",\"next\":{\"id\":2,\"name\":\"Child\",\"next\":1}}";
        IdBean parent = mapper.readValue(json, IdBean.class);

        assertNotNull(parent);
        assertEquals(1, parent.id);
        assertEquals("Parent", parent.name);
        assertNotNull(parent.next);
        assertEquals(2, parent.next.id);
        assertEquals("Child", parent.next.name);
        assertSame(parent, parent.next.next);
    }

    // Tests unknown property with default config throws exception
    @Test(expected = UnrecognizedPropertyException.class)
    public void testDeserialize_unknownProperty_throwsUnrecognizedPropertyException() throws Exception {
        String json = "{\"id\":1,\"unknownField\":true}";
        mapper.readValue(json, SimpleBean.class);
    }

    // Tests unexpected token handling (e.g. integer when object expected)
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_unexpectedToken_throwsJsonMappingException() throws Exception {
        String json = "12345";
        mapper.readValue(json, SimpleBean.class);
    }

    // Tests creator returning null throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_creatorReturnsNull_throwsJsonMappingException() throws Exception {
        String json = "{\"value\":\"someInput\"}";
        mapper.readValue(json, NullReturningCreatorBean.class);
    }

    // Tests empty JSON object deserialization
    @Test
    public void testDeserialize_emptyObject_returnsDefaultInstance() throws Exception {
        String json = "{}";
        SimpleBean result = mapper.readValue(json, SimpleBean.class);

        assertNotNull(result);
        assertEquals(0, result.id);
        assertNull(result.name);
    }

    // --- Newly Added Tests ---

    // Tests shape = Shape.ARRAY deserialization for standard bean
    @Test
    public void testDeserialize_asArrayFormat_success() throws Exception {
        String json = "[10,\"TestArray\",true]";
        AsArrayBean result = mapper.readValue(json, AsArrayBean.class);

        assertNotNull(result);
        assertEquals(10, result.id);
        assertEquals("TestArray", result.name);
        assertTrue(result.active);
    }

    // Tests shape = Shape.ARRAY deserialization with property-based creator
    @Test
    public void testDeserialize_asArrayFormatCreator_success() throws Exception {
        String json = "[20,\"CreatorArray\"]";
        AsArrayCreatorBean result = mapper.readValue(json, AsArrayCreatorBean.class);

        assertNotNull(result);
        assertEquals(20, result.id);
        assertEquals("CreatorArray", result.name);
    }

    // Tests ignoring unknown properties at class-level with @JsonIgnoreProperties(ignoreUnknown = true)
    @Test
    public void testDeserialize_ignoreUnknownClassAnnotation_ignoresUnknownProperties() throws Exception {
        String json = "{\"id\":77,\"extraUnknown\":\"ignoreMe\",\"name\":\"Valid\"}";
        IgnoreUnknownBean result = mapper.readValue(json, IgnoreUnknownBean.class);

        assertNotNull(result);
        assertEquals(77, result.id);
        assertEquals("Valid", result.name);
    }

    // Tests ignoring unknown properties globally via DeserializationFeature
    @Test
    public void testDeserialize_globallyDisableFailOnUnknownProperties_ignoresUnknownProperties() throws Exception {
        ObjectMapper lenientMapper = new ObjectMapper();
        lenientMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        String json = "{\"id\":88,\"name\":\"Lenient\",\"foo\":\"bar\"}";
        SimpleBean result = lenientMapper.readValue(json, SimpleBean.class);

        assertNotNull(result);
        assertEquals(88, result.id);
        assertEquals("Lenient", result.name);
    }

    // Tests delegating string creator
    @Test
    public void testDeserialize_stringDelegatingCreator_success() throws Exception {
        String json = "\"delegatedString\"";
        StringDelegatingBean result = mapper.readValue(json, StringDelegatingBean.class);

        assertNotNull(result);
        assertEquals("delegatedString", result.getValue());
    }

    // Tests delegating int creator
    @Test
    public void testDeserialize_intDelegatingCreator_success() throws Exception {
        String json = "1234";
        IntDelegatingBean result = mapper.readValue(json, IntDelegatingBean.class);

        assertNotNull(result);
        assertEquals(1234, result.getValue());
    }

    // Tests delegating boolean creator
    @Test
    public void testDeserialize_booleanDelegatingCreator_success() throws Exception {
        String json = "true";
        BooleanDelegatingBean result = mapper.readValue(json, BooleanDelegatingBean.class);

        assertNotNull(result);
        assertTrue(result.isValue());
    }

    // Tests delegating double creator
    @Test
    public void testDeserialize_doubleDelegatingCreator_success() throws Exception {
        String json = "12.34";
        DoubleDelegatingBean result = mapper.readValue(json, DoubleDelegatingBean.class);

        assertNotNull(result);
        assertEquals(12.34, result.getValue(), 0.0001);
    }

    // Tests builder-based deserialization (@JsonDeserialize(builder = ...))
    @Test
    public void testDeserialize_builderPattern_success() throws Exception {
        String json = "{\"id\":555,\"name\":\"BuilderConstructed\"}";
        ValueObject result = mapper.readValue(json, ValueObject.class);

        assertNotNull(result);
        assertEquals(555, result.getId());
        assertEquals("BuilderConstructed", result.getName());
    }

    // Tests managed/back reference binding
    @Test
    public void testDeserialize_managedAndBackReference_success() throws Exception {
        String json = "{\"id\":10,\"children\":[{\"name\":\"FirstChild\"},{\"name\":\"SecondChild\"}]}";
        ParentRefBean parent = mapper.readValue(json, ParentRefBean.class);

        assertNotNull(parent);
        assertEquals(10, parent.id);
        assertEquals(2, parent.children.size());
        assertSame(parent, parent.children.get(0).parent);
        assertSame(parent, parent.children.get(1).parent);
        assertEquals("FirstChild", parent.children.get(0).name);
        assertEquals("SecondChild", parent.children.get(1).name);
    }

    // Tests access control annotations (WRITE_ONLY / READ_ONLY)
    @Test
    public void testDeserialize_writeOnlyAndReadOnlyProperties_honorsAccess() throws Exception {
        String json = "{\"writeOnly\":\"writeVal\",\"readOnly\":\"readVal\"}";
        AccessControlledBean result = mapper.readValue(json, AccessControlledBean.class);

        assertNotNull(result);
        assertEquals("writeVal", result.writeOnly);
        assertNull(result.readOnly);
    }

    // Tests ACCEPT_EMPTY_STRING_AS_NULL_OBJECT feature
    @Test
    public void testDeserialize_emptyStringAsNullObject_returnsNull() throws Exception {
        ObjectMapper emptyStringMapper = new ObjectMapper();
        emptyStringMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        SimpleBean result = emptyStringMapper.readValue("\"\"", SimpleBean.class);
        assertNull(result);
    }

    // Tests FAIL_ON_NULL_FOR_PRIMITIVES feature
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_failOnNullForPrimitives_throwsException() throws Exception {
        ObjectMapper strictMapper = new ObjectMapper();
        strictMapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);

        String json = "{\"num\":null}";
        strictMapper.readValue(json, PrimitiveBean.class);
    }

    // Tests custom DeserializationProblemHandler handling unknown property
    @Test
    public void testDeserialize_problemHandlerHandlingUnknownProperty_recovers() throws Exception {
        ObjectMapper handlerMapper = new ObjectMapper();
        handlerMapper.addHandler(new com.fasterxml.jackson.databind.deser.DeserializationProblemHandler() {
            @Override
            public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p,
                                                 JsonDeserializer<?> deserializer, Object beanOrClass,
                                                 String propertyName) throws IOException {
                p.skipChildren();
                return true;
            }
        });

        String json = "{\"id\":99,\"unknownObj\":{\"nested\":123},\"name\":\"Handled\"}";
        SimpleBean result = handlerMapper.readValue(json, SimpleBean.class);

        assertNotNull(result);
        assertEquals(99, result.id);
        assertEquals("Handled", result.name);
    }

    // Tests null JSON string deserialization returns null
    @Test
    public void testDeserialize_nullLiteral_returnsNull() throws Exception {
        SimpleBean result = mapper.readValue("null", SimpleBean.class);
        assertNull(result);
    }
}