package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

public class BeanDeserializerTest {

    private final ObjectMapper MAPPER = new ObjectMapper();

    // Helper classes for testing various deserialization paths

    static class SimpleBean {
        public String name;
        public int age;

        public SimpleBean() { }
        public SimpleBean(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    static class CreatorBean {
        final String a;
        final int b;
        String c;

        @JsonCreator
        public CreatorBean(@JsonProperty("a") String a, @JsonProperty("b") int b) {
            this.a = a;
            this.b = b;
        }

        public void setC(String c) {
            this.c = c;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    static class CreatorWithUnwrappedAndTrailing {
        final int id;
        final String name;
        String extra;

        @JsonCreator
        public CreatorWithUnwrappedAndTrailing(@JsonProperty("id") int id, @JsonProperty("name") String name) {
            this.id = id;
            this.name = name;
        }

        public void setExtra(String extra) {
            this.extra = extra;
        }
    }

    static class IgnorablePropsBean {
        @JsonProperty("id")
        public int id;
        public String name;
    }

    @JsonIgnoreProperties({ "ignored1", "ignored2" })
    static class IgnoredPropertiesCreatorBean {
        final int x;
        final int y;

        @JsonCreator
        public IgnoredPropertiesCreatorBean(@JsonProperty("x") int x, @JsonProperty("y") int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class AnySetterBean {
        public int id;
        private Map<String, Object> other = new HashMap<String, Object>();

        @JsonAnySetter
        public void setOther(String name, Object value) {
            other.put(name, value);
        }

        public Map<String, Object> getOther() {
            return other;
        }
    }

    static class CreatorWithAnySetterBean {
        final int id;
        private Map<String, Object> other = new HashMap<String, Object>();

        @JsonCreator
        public CreatorWithAnySetterBean(@JsonProperty("id") int id) {
            this.id = id;
        }

        @JsonAnySetter
        public void setOther(String name, Object value) {
            other.put(name, value);
        }

        public Map<String, Object> getOther() {
            return other;
        }
    }

    static class Views {
        static class Public { }
        static class Internal extends Public { }
    }

    static class ViewBean {
        @JsonView(Views.Public.class)
        public String publicField;

        @JsonView(Views.Internal.class)
        public String internalField;
    }

    static class ViewCreatorBean {
        @JsonView(Views.Public.class)
        public final String publicField;

        @JsonView(Views.Internal.class)
        public final String internalField;

        @JsonCreator
        public ViewCreatorBean(@JsonProperty("publicField") String pub,
                               @JsonProperty("internalField") String internal) {
            this.publicField = pub;
            this.internalField = internal;
        }
    }

    static class UnwrappedChild {
        public String city;
        public String street;
    }

    static class UnwrappedParent {
        public int id;
        @JsonUnwrapped
        public UnwrappedChild address;
    }

    static class CreatorWithUnwrappedParent {
        public final int id;
        @JsonUnwrapped
        public final UnwrappedChild address;

        @JsonCreator
        public CreatorWithUnwrappedParent(@JsonProperty("id") int id, @JsonProperty("address") UnwrappedChild address) {
            this.id = id;
            this.address = address;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdentifiedBean {
        public int id;
        public String name;
        public IdentifiedBean next;
    }

    static class FailingCreatorBean {
        @JsonCreator
        public FailingCreatorBean(@JsonProperty("name") String name) {
            throw new IllegalArgumentException("Constructor failed: " + name);
        }
    }

    static class FailingSetterBean {
        public void setName(String name) {
            throw new IllegalArgumentException("Setter failed: " + name);
        }
    }

    static class SingleScalarCreatorBean {
        final String value;

        @JsonCreator
        public SingleScalarCreatorBean(String value) {
            this.value = value;
        }
    }

    static class SingleIntCreatorBean {
        final int value;

        @JsonCreator
        public SingleIntCreatorBean(int value) {
            this.value = value;
        }
    }

    static class SingleDoubleCreatorBean {
        final double value;

        @JsonCreator
        public SingleDoubleCreatorBean(double value) {
            this.value = value;
        }
    }

    static class SingleBooleanCreatorBean {
        final boolean value;

        @JsonCreator
        public SingleBooleanCreatorBean(boolean value) {
            this.value = value;
        }
    }

    static class DelegatingListCreatorBean {
        final List<String> items;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public DelegatingListCreatorBean(List<String> items) {
            this.items = items;
        }
    }

    @JsonDeserialize(builder = ValueBeanBuilder.class)
    static class ValueWithBuilderBean {
        final String name;
        final int count;

        ValueWithBuilderBean(String name, int count) {
            this.name = name;
            this.count = count;
        }
    }

    @JsonPOJOBuilder(withPrefix = "with")
    static class ValueBeanBuilder {
        private String name;
        private int count;

        public ValueBeanBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ValueBeanBuilder withCount(int count) {
            this.count = count;
            return this;
        }

        public ValueWithBuilderBean build() {
            return new ValueWithBuilderBean(name, count);
        }
    }

    static class InjectedBean {
        public int id;
        @JacksonInject("injectedVal")
        public String injected;
    }

    static class ParentReferenceBean {
        public int id;
        @JsonManagedReference
        public ChildReferenceBean child;
    }

    static class ChildReferenceBean {
        public String name;
        @JsonBackReference
        public ParentReferenceBean parent;
    }

    // Tests normal vanilla deserialization
    @Test
    public void testVanillaDeserialize_validInput_success() throws IOException {
        String json = "{\"name\":\"John\",\"age\":30}";
        SimpleBean result = MAPPER.readValue(json, SimpleBean.class);
        assertNotNull(result);
        assertEquals("John", result.name);
        assertEquals(30, result.age);
    }

    // Tests property-based creator with properties in exact and non-creator order
    @Test
    public void testPropertyBasedCreator_validProperties_success() throws IOException {
        String json = "{\"c\":\"extra\",\"b\":42,\"a\":\"test\"}";
        CreatorBean result = MAPPER.readValue(json, CreatorBean.class);
        assertNotNull(result);
        assertEquals("test", result.a);
        assertEquals(42, result.b);
        assertEquals("extra", result.c);
    }

    // Tests property-based creator with trailing tokens and unknown properties (Defects4J 101 target)
    @Test
    public void testPropertyBasedCreator_withTrailingUnknownProperties_success() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        String json = "{\"id\":10,\"name\":\"testName\",\"unknownField\":123,\"extra\":\"extraVal\"}";
        CreatorWithUnwrappedAndTrailing result = mapper.readValue(json, CreatorWithUnwrappedAndTrailing.class);
        assertNotNull(result);
        assertEquals(10, result.id);
        assertEquals("testName", result.name);
        assertEquals("extraVal", result.extra);
    }

    // Tests property-based creator with ignorable properties
    @Test
    public void testPropertyBasedCreator_withIgnoredProperties_success() throws IOException {
        String json = "{\"ignored1\":\"val1\",\"x\":1,\"ignored2\":999,\"y\":2}";
        IgnoredPropertiesCreatorBean result = MAPPER.readValue(json, IgnoredPropertiesCreatorBean.class);
        assertNotNull(result);
        assertEquals(1, result.x);
        assertEquals(2, result.y);
    }

    // Tests unknown property failing when configured
    @Test(expected = UnrecognizedPropertyException.class)
    public void testDeserialize_unknownProperty_throwsException() throws IOException {
        String json = "{\"name\":\"John\",\"unknownProp\":123}";
        MAPPER.readValue(json, SimpleBean.class);
    }

    // Tests any setter during default deserialization
    @Test
    public void testAnySetter_standardDeserialization_populatesMap() throws IOException {
        String json = "{\"id\":5,\"dynamicA\":\"valA\",\"dynamicB\":100}";
        AnySetterBean result = MAPPER.readValue(json, AnySetterBean.class);
        assertNotNull(result);
        assertEquals(5, result.id);
        assertEquals("valA", result.getOther().get("dynamicA"));
        assertEquals(100, result.getOther().get("dynamicB"));
    }

    // Tests any setter with property-based creator buffering
    @Test
    public void testAnySetter_propertyBasedCreator_populatesMap() throws IOException {
        String json = "{\"dynamicA\":\"valA\",\"id\":7,\"dynamicB\":200}";
        CreatorWithAnySetterBean result = MAPPER.readValue(json, CreatorWithAnySetterBean.class);
        assertNotNull(result);
        assertEquals(7, result.id);
        assertEquals("valA", result.getOther().get("dynamicA"));
        assertEquals(200, result.getOther().get("dynamicB"));
    }

    // Tests unwrapped properties handling
    @Test
    public void testUnwrappedProperties_standardDeserialization_success() throws IOException {
        String json = "{\"id\":101,\"city\":\"New York\",\"street\":\"5th Ave\"}";
        UnwrappedParent result = MAPPER.readValue(json, UnwrappedParent.class);
        assertNotNull(result);
        assertEquals(101, result.id);
        assertNotNull(result.address);
        assertEquals("New York", result.address.city);
        assertEquals("5th Ave", result.address.street);
    }

    // Tests JSON view filtering during deserialization
    @Test
    public void testViews_viewProcessing_excludesNonViewFields() throws IOException {
        String json = "{\"publicField\":\"pub\",\"internalField\":\"priv\"}";
        ViewBean result = MAPPER.readerWithView(Views.Public.class)
                .forType(ViewBean.class)
                .readValue(json);
        assertNotNull(result);
        assertEquals("pub", result.publicField);
        assertNull(result.internalField);
    }

    // Tests Object ID handling and back-reference resolution
    @Test
    public void testObjectId_circularReference_resolvedCorrectly() throws IOException {
        String json = "{\"id\":1,\"name\":\"first\",\"next\":{\"id\":2,\"name\":\"second\",\"next\":1}}";
        IdentifiedBean result = MAPPER.readValue(json, IdentifiedBean.class);
        assertNotNull(result);
        assertEquals(1, result.id);
        assertEquals("first", result.name);
        assertNotNull(result.next);
        assertEquals(2, result.next.id);
        assertSame(result, result.next.next);
    }

    // Tests exception handling when constructor throws
    @Test(expected = JsonMappingException.class)
    public void testCreatorException_wrapsAndThrows() throws IOException {
        String json = "{\"name\":\"boom\"}";
        MAPPER.readValue(json, FailingCreatorBean.class);
    }

    // Tests exception handling when setter throws
    @Test(expected = JsonMappingException.class)
    public void testSetterException_wrapsAndThrows() throws IOException {
        String json = "{\"name\":\"boom\"}";
        MAPPER.readValue(json, FailingSetterBean.class);
    }

    // Tests unexpected token when deserializing bean from array/scalar
    @Test(expected = MismatchedInputException.class)
    public void testDeserialize_unexpectedToken_throwsException() throws IOException {
        String json = "[1, 2, 3]";
        MAPPER.readValue(json, SimpleBean.class);
    }

    // Tests updating an existing bean instance
    @Test
    public void testDeserialize_updatingExistingBean_mutatesObject() throws IOException {
        SimpleBean existing = new SimpleBean("Original", 20);
        String json = "{\"age\":25}";
        SimpleBean updated = MAPPER.readerForUpdating(existing).readValue(json);
        assertSame(existing, updated);
        assertEquals("Original", updated.name);
        assertEquals(25, updated.age);
    }

    // Tests empty JSON object deserialization
    @Test
    public void testDeserialize_emptyObject_returnsDefaultBean() throws IOException {
        String json = "{}";
        SimpleBean result = MAPPER.readValue(json, SimpleBean.class);
        assertNotNull(result);
        assertNull(result.name);
        assertEquals(0, result.age);
    }

    // Tests creator with unwrapped child properties
    @Test
    public void testCreatorWithUnwrappedProperties_success() throws IOException {
        String json = "{\"id\":200,\"address\":{\"city\":\"Tokyo\",\"street\":\"Ginza\"}}";
        CreatorWithUnwrappedParent result = MAPPER.readValue(json, CreatorWithUnwrappedParent.class);
        assertNotNull(result);
        assertEquals(200, result.id);
        assertNotNull(result.address);
        assertEquals("Tokyo", result.address.city);
        assertEquals("Ginza", result.address.street);
    }

    // Tests ignorable properties with standard bean
    @Test
    public void testIgnorablePropsBean_success() throws IOException {
        String json = "{\"id\":123,\"name\":\"Alice\"}";
        IgnorablePropsBean result = MAPPER.readValue(json, IgnorablePropsBean.class);
        assertNotNull(result);
        assertEquals(123, result.id);
        assertEquals("Alice", result.name);
    }

    // Tests single String argument creator (scalar deserialization)
    @Test
    public void testSingleStringCreator_success() throws IOException {
        String json = "\"helloWorld\"";
        SingleScalarCreatorBean result = MAPPER.readValue(json, SingleScalarCreatorBean.class);
        assertNotNull(result);
        assertEquals("helloWorld", result.value);
    }

    // Tests single int/number argument creator
    @Test
    public void testSingleIntCreator_success() throws IOException {
        String json = "12345";
        SingleIntCreatorBean result = MAPPER.readValue(json, SingleIntCreatorBean.class);
        assertNotNull(result);
        assertEquals(12345, result.value);
    }

    // Tests single double/float argument creator
    @Test
    public void testSingleDoubleCreator_success() throws IOException {
        String json = "3.14159";
        SingleDoubleCreatorBean result = MAPPER.readValue(json, SingleDoubleCreatorBean.class);
        assertNotNull(result);
        assertEquals(3.14159, result.value, 0.0001);
    }

    // Tests single boolean argument creator
    @Test
    public void testSingleBooleanCreator_success() throws IOException {
        String json = "true";
        SingleBooleanCreatorBean result = MAPPER.readValue(json, SingleBooleanCreatorBean.class);
        assertNotNull(result);
        assertTrue(result.value);
    }

    // Tests delegating creator from array
    @Test
    public void testDelegatingListCreator_success() throws IOException {
        String json = "[\"a\", \"b\", \"c\"]";
        DelegatingListCreatorBean result = MAPPER.readValue(json, DelegatingListCreatorBean.class);
        assertNotNull(result);
        assertNotNull(result.items);
        assertEquals(3, result.items.size());
        assertEquals("a", result.items.get(0));
    }

    // Tests builder-based deserialization
    @Test
    public void testBuilderDeserialization_success() throws IOException {
        String json = "{\"name\":\"BuilderTest\",\"count\":42}";
        ValueWithBuilderBean result = MAPPER.readValue(json, ValueWithBuilderBean.class);
        assertNotNull(result);
        assertEquals("BuilderTest", result.name);
        assertEquals(42, result.count);
    }

    // Tests JacksonInject annotation handling during deserialization
    @Test
    public void testJacksonInject_success() throws IOException {
        InjectableValues inject = new InjectableValues.Std().addValue("injectedVal", "injectedString");
        String json = "{\"id\":88}";
        InjectedBean result = MAPPER.reader(inject).forType(InjectedBean.class).readValue(json);
        assertNotNull(result);
        assertEquals(88, result.id);
        assertEquals("injectedString", result.injected);
    }

    // Tests managed and back reference deserialization
    @Test
    public void testManagedAndBackReference_success() throws IOException {
        String json = "{\"id\":10,\"child\":{\"name\":\"junior\"}}";
        ParentReferenceBean parent = MAPPER.readValue(json, ParentReferenceBean.class);
        assertNotNull(parent);
        assertEquals(10, parent.id);
        assertNotNull(parent.child);
        assertEquals("junior", parent.child.name);
        assertSame(parent, parent.child.parent);
    }

    // Tests DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT
    @Test
    public void testAcceptEmptyArrayAsNullObject_success() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        String json = "[]";
        SimpleBean result = mapper.readValue(json, SimpleBean.class);
        assertNull(result);
    }

    // Tests DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT
    @Test
    public void testAcceptEmptyStringAsNullObject_success() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        String json = "\"\"";
        SimpleBean result = mapper.readValue(json, SimpleBean.class);
        assertNull(result);
    }

    // Tests DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS
    @Test
    public void testUnwrapSingleValueArrays_success() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        String json = "[{\"name\":\"Wrapped\",\"age\":18}]";
        SimpleBean result = mapper.readValue(json, SimpleBean.class);
        assertNotNull(result);
        assertEquals("Wrapped", result.name);
        assertEquals(18, result.age);
    }

    // Tests JSON view filtering with creator-based bean
    @Test
    public void testViewCreatorBean_filtersPropertiesAccordingToView() throws IOException {
        String json = "{\"publicField\":\"visiblePub\",\"internalField\":\"hiddenPriv\"}";
        ViewCreatorBean result = MAPPER.readerWithView(Views.Public.class)
                .forType(ViewCreatorBean.class)
                .readValue(json);
        assertNotNull(result);
        assertEquals("visiblePub", result.publicField);
        assertNull(result.internalField);
    }
}