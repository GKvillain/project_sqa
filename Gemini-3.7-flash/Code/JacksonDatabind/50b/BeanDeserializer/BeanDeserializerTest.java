package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class BeanDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // Helper classes for testing

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
        private final String name;
        private final int age;
        public String extra;

        @JsonCreator
        public CreatorBean(@JsonProperty("name") String name, @JsonProperty("age") int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }

    static class Views {
        static class Public {}
        static class Extended extends Public {}
    }

    static class ViewBean {
        @JsonView(Views.Public.class)
        public String publicName;

        @JsonView(Views.Extended.class)
        public String extendedName;
    }

    static class Location {
        public String city;
        public String country;
    }

    static class PersonWithUnwrapped {
        public String name;

        @JsonUnwrapped
        public Location location;
    }

    static class UnwrappedCreatorBean {
        public final String name;

        @JsonUnwrapped
        public final Location location;

        @JsonCreator
        public UnwrappedCreatorBean(@JsonProperty("name") String name,
                                   @JsonProperty("city") String city,
                                   @JsonProperty("country") String country) {
            this.name = name;
            this.location = new Location();
            this.location.city = city;
            this.location.country = country;
        }
    }

    static class AnySetterBean {
        public String name;
        private Map<String, Object> other = new HashMap<String, Object>();

        @JsonAnySetter
        public void setOther(String key, Object value) {
            other.put(key, value);
        }

        public Map<String, Object> getOther() {
            return other;
        }
    }

    @JsonIgnoreProperties({"ignoredField"})
    static class IgnorableBean {
        public String name;
        public String ignoredField;
    }

    static class FromStringBean {
        String value;

        @JsonCreator
        public FromStringBean(String v) {
            this.value = v;
        }
    }

    static class FromNumberBean {
        long value;

        @JsonCreator
        public FromNumberBean(long v) {
            this.value = v;
        }
    }

    static class FromDoubleBean {
        double value;

        @JsonCreator
        public FromDoubleBean(double v) {
            this.value = v;
        }
    }

    static class FromBooleanBean {
        boolean value;

        @JsonCreator
        public FromBooleanBean(boolean v) {
            this.value = v;
        }
    }

    static class FromArrayBean {
        List<String> items;

        @JsonCreator
        public FromArrayBean(List<String> items) {
            this.items = items;
        }
    }

    // Tests normal vanilla deserialization of POJO
    @Test
    public void testDeserialize_vanillaObject_returnsPopulatedBean() throws IOException {
        String json = "{\"name\":\"John\",\"age\":30}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);

        assertNotNull(bean);
        assertEquals("John", bean.name);
        assertEquals(30, bean.age);
    }

    // Tests updating an existing bean instance
    @Test
    public void testDeserialize_existingBean_updatesFields() throws IOException {
        SimpleBean bean = new SimpleBean("Original", 20);
        String json = "{\"name\":\"Updated\",\"age\":25}";

        SimpleBean result = mapper.readerForUpdating(bean).readValue(json);

        assertSame(bean, result);
        assertEquals("Updated", bean.name);
        assertEquals(25, bean.age);
    }

    // Tests updating an existing bean with empty JSON object
    @Test
    public void testDeserialize_existingBeanEmptyJson_leavesBeanUnchanged() throws IOException {
        SimpleBean bean = new SimpleBean("Initial", 40);
        String json = "{}";

        SimpleBean result = mapper.readerForUpdating(bean).readValue(json);

        assertSame(bean, result);
        assertEquals("Initial", bean.name);
        assertEquals(40, bean.age);
    }

    // Tests deserialization using PropertyBasedCreator
    @Test
    public void testDeserialize_propertyBasedCreator_constructsBean() throws IOException {
        String json = "{\"extra\":\"ExtraValue\",\"age\":28,\"name\":\"Alice\"}";
        CreatorBean bean = mapper.readValue(json, CreatorBean.class);

        assertNotNull(bean);
        assertEquals("Alice", bean.getName());
        assertEquals(28, bean.getAge());
        assertEquals("ExtraValue", bean.extra);
    }

    // Tests property-based creator with ignorable property
    @Test
    public void testDeserialize_propertyBasedCreatorWithIgnoredProperty_ignoresProperty() throws IOException {
        String json = "{\"name\":\"Bob\",\"age\":35,\"ignoredField\":\"skipMe\"}";
        CreatorBean bean = mapper.readValue(json, CreatorBean.class);

        assertNotNull(bean);
        assertEquals("Bob", bean.getName());
        assertEquals(35, bean.getAge());
    }

    // Tests deserialization with JSON Views
    @Test
    public void testDeserialize_withView_populatesOnlyVisibleFields() throws IOException {
        String json = "{\"publicName\":\"Pub\",\"extendedName\":\"Ext\"}";

        ViewBean bean = mapper.readerWithView(Views.Public.class)
                .forType(ViewBean.class)
                .readValue(json);

        assertNotNull(bean);
        assertEquals("Pub", bean.publicName);
        assertNull(bean.extendedName);
    }

    // Tests deserialization with unwrapped properties
    @Test
    public void testDeserialize_unwrappedProperties_populatesUnwrappedBean() throws IOException {
        String json = "{\"name\":\"Charlie\",\"city\":\"Boston\",\"country\":\"USA\"}";
        PersonWithUnwrapped person = mapper.readValue(json, PersonWithUnwrapped.class);

        assertNotNull(person);
        assertEquals("Charlie", person.name);
        assertNotNull(person.location);
        assertEquals("Boston", person.location.city);
        assertEquals("USA", person.location.country);
    }

    // Tests deserialization with unwrapped properties and property-based creator
    @Test
    public void testDeserialize_unwrappedWithPropertyBasedCreator_constructsBean() throws IOException {
        String json = "{\"name\":\"Diana\",\"city\":\"London\",\"country\":\"UK\"}";
        UnwrappedCreatorBean bean = mapper.readValue(json, UnwrappedCreatorBean.class);

        assertNotNull(bean);
        assertEquals("Diana", bean.name);
        assertNotNull(bean.location);
        assertEquals("London", bean.location.city);
        assertEquals("UK", bean.location.country);
    }

    // Tests deserialization with @JsonAnySetter
    @Test
    public void testDeserialize_anySetter_capturesUnmappedProperties() throws IOException {
        String json = "{\"name\":\"Edward\",\"custom1\":\"val1\",\"custom2\":\"val2\"}";
        AnySetterBean bean = mapper.readValue(json, AnySetterBean.class);

        assertNotNull(bean);
        assertEquals("Edward", bean.name);
        assertEquals("val1", bean.getOther().get("custom1"));
        assertEquals("val2", bean.getOther().get("custom2"));
    }

    // Tests deserialization with ignorable properties
    @Test
    public void testDeserialize_ignorableProperties_skipsIgnoredProperties() throws IOException {
        String json = "{\"name\":\"Frank\",\"ignoredField\":\"secret\"}";
        IgnorableBean bean = mapper.readValue(json, IgnorableBean.class);

        assertNotNull(bean);
        assertEquals("Frank", bean.name);
        assertNull(bean.ignoredField);
    }

    // Tests deserialization from String token via creator
    @Test
    public void testDeserialize_fromStringToken_callsStringCreator() throws IOException {
        String json = "\"testStringValue\"";
        FromStringBean bean = mapper.readValue(json, FromStringBean.class);

        assertNotNull(bean);
        assertEquals("testStringValue", bean.value);
    }

    // Tests deserialization from integer number token via creator
    @Test
    public void testDeserialize_fromIntToken_callsNumberCreator() throws IOException {
        String json = "123456789";
        FromNumberBean bean = mapper.readValue(json, FromNumberBean.class);

        assertNotNull(bean);
        assertEquals(123456789L, bean.value);
    }

    // Tests deserialization from float/double number token via creator
    @Test
    public void testDeserialize_fromDoubleToken_callsDoubleCreator() throws IOException {
        String json = "12.34";
        FromDoubleBean bean = mapper.readValue(json, FromDoubleBean.class);

        assertNotNull(bean);
        assertEquals(12.34, bean.value, 0.0001);
    }

    // Tests deserialization from boolean token via creator
    @Test
    public void testDeserialize_fromBooleanToken_callsBooleanCreator() throws IOException {
        String json = "true";
        FromBooleanBean bean = mapper.readValue(json, FromBooleanBean.class);

        assertNotNull(bean);
        assertTrue(bean.value);
    }

    // Tests deserialization from array token via delegating creator
    @Test
    public void testDeserialize_fromArrayToken_callsArrayCreator() throws IOException {
        String json = "[\"item1\",\"item2\"]";
        FromArrayBean bean = mapper.readValue(json, FromArrayBean.class);

        assertNotNull(bean);
        assertEquals(Arrays.asList("item1", "item2"), bean.items);
    }

    // Tests exception on unknown property when FAIL_ON_UNKNOWN_PROPERTIES is enabled
    @Test(expected = UnrecognizedPropertyException.class)
    public void testDeserialize_unknownProperty_throwsUnrecognizedPropertyException() throws IOException {
        String json = "{\"name\":\"George\",\"unknownProp\":\"unknown\"}";
        mapper.readValue(json, SimpleBean.class);
    }

    // Tests copy-mutation methods on BeanDeserializer
    @Test
    public void testBeanDeserializer_mutatorMethods_returnNewInstances() throws Exception {
        JavaType type = mapper.constructType(SimpleBean.class);
        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);
        JsonDeserializer<?> deser = ctxt.findRootValueDeserializer(type);

        assertTrue(deser instanceof BeanDeserializer);
        BeanDeserializer beanDeser = (BeanDeserializer) deser;

        JsonDeserializer<Object> unwrapped = beanDeser.unwrappingDeserializer(NameTransformer.NOP);
        assertNotNull(unwrapped);
        assertTrue(unwrapped instanceof BeanDeserializer);

        Set<String> ignorable = Collections.singleton("dummy");
        BeanDeserializer withIgnored = beanDeser.withIgnorableProperties(ignorable);
        assertNotNull(withIgnored);
        assertNotSame(beanDeser, withIgnored);

        BeanDeserializerBase asArray = beanDeser.asArrayDeserializer();
        assertNotNull(asArray);
    }

    // Tests lazy creation of null exception root cause
    @Test
    public void testCreatorReturnedNullException_lazyInstantiation() {
        BeanDeserializer deser = null;
        try {
            JavaType type = mapper.constructType(SimpleBean.class);
            DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                    .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);
            deser = (BeanDeserializer) ctxt.findRootValueDeserializer(type);
        } catch (Exception e) {
            fail("Failed to retrieve BeanDeserializer: " + e.getMessage());
        }

        Exception ex1 = deser._creatorReturnedNullException();
        assertNotNull(ex1);
        assertTrue(ex1 instanceof NullPointerException);

        Exception ex2 = deser._creatorReturnedNullException();
        assertSame(ex1, ex2);
    }
}