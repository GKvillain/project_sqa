package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

public class BuilderBasedDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // Helper classes for test cases

    @JsonDeserialize(builder = SimpleBeanBuilder.class)
    static class SimpleBean {
        final int id;
        final String name;

        SimpleBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    static class SimpleBeanBuilder {
        private int id;
        private String name;

        public SimpleBeanBuilder withId(int id) {
            this.id = id;
            return this;
        }

        public SimpleBeanBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public SimpleBean build() {
            return new SimpleBean(id, name);
        }
    }

    @JsonDeserialize(builder = CreatorBuilder.class)
    static class CreatorBean {
        final int x;
        final int y;
        final String name;

        CreatorBean(int x, int y, String name) {
            this.x = x;
            this.y = y;
            this.name = name;
        }
    }

    @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "set")
    static class CreatorBuilder {
        private final int x;
        private final int y;
        private String name;

        @JsonCreator
        public CreatorBuilder(@JsonProperty("x") int x, @JsonProperty("y") int y) {
            this.x = x;
            this.y = y;
        }

        public CreatorBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public CreatorBean create() {
            return new CreatorBean(x, y, name);
        }
    }

    @JsonDeserialize(builder = IgnorablePropBuilder.class)
    static class IgnorableBean {
        final String value;

        IgnorableBean(String value) {
            this.value = value;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class IgnorablePropBuilder {
        private String value;

        public IgnorablePropBuilder withValue(String value) {
            this.value = value;
            return this;
        }

        public IgnorableBean build() {
            return new IgnorableBean(value);
        }
    }

    static class Views {
        static class Public {}
        static class Internal extends Public {}
    }

    @JsonDeserialize(builder = ViewBeanBuilder.class)
    static class ViewBean {
        final String publicVal;
        final String internalVal;

        ViewBean(String publicVal, String internalVal) {
            this.publicVal = publicVal;
            this.internalVal = internalVal;
        }
    }

    static class ViewBeanBuilder {
        private String publicVal;
        private String internalVal;

        @JsonView(Views.Public.class)
        public ViewBeanBuilder withPublicVal(String publicVal) {
            this.publicVal = publicVal;
            return this;
        }

        @JsonView(Views.Internal.class)
        public ViewBeanBuilder withInternalVal(String internalVal) {
            this.internalVal = internalVal;
            return this;
        }

        public ViewBean build() {
            return new ViewBean(publicVal, internalVal);
        }
    }

    @JsonDeserialize(builder = ExceptionBuilder.class)
    static class ExceptionBean {
        final int val;

        ExceptionBean(int val) {
            this.val = val;
        }
    }

    static class ExceptionBuilder {
        private int val;

        public ExceptionBuilder withVal(int val) {
            this.val = val;
            return this;
        }

        public ExceptionBean build() {
            if (val < 0) {
                throw new IllegalStateException("Negative value not allowed");
            }
            return new ExceptionBean(val);
        }
    }

    static class Location {
        public String city;
        public String country;
    }

    @JsonDeserialize(builder = UnwrappedBuilder.class)
    static class UnwrappedBean {
        final String name;
        final Location location;

        UnwrappedBean(String name, Location location) {
            this.name = name;
            this.location = location;
        }
    }

    static class UnwrappedBuilder {
        private String name;
        private Location location;

        public UnwrappedBuilder withName(String name) {
            this.name = name;
            return this;
        }

        @JsonUnwrapped
        public UnwrappedBuilder withLocation(Location location) {
            this.location = location;
            return this;
        }

        public UnwrappedBean build() {
            return new UnwrappedBean(name, location);
        }
    }

    @JsonDeserialize(builder = CreatorUnwrappedBuilder.class)
    static class CreatorUnwrappedBean {
        final int id;
        final Location location;

        CreatorUnwrappedBean(int id, Location location) {
            this.id = id;
            this.location = location;
        }
    }

    static class CreatorUnwrappedBuilder {
        private final int id;
        private Location location;

        @JsonCreator
        public CreatorUnwrappedBuilder(@JsonProperty("id") int id) {
            this.id = id;
        }

        @JsonUnwrapped
        public CreatorUnwrappedBuilder withLocation(Location location) {
            this.location = location;
            return this;
        }

        public CreatorUnwrappedBean build() {
            return new CreatorUnwrappedBean(id, location);
        }
    }

    // Additional helper classes for missing coverage

    @JsonDeserialize(builder = AnySetterBuilder.class)
    static class AnySetterBean {
        final String name;
        final Map<String, Object> others;

        AnySetterBean(String name, Map<String, Object> others) {
            this.name = name;
            this.others = others;
        }
    }

    static class AnySetterBuilder {
        private String name;
        private final Map<String, Object> others = new HashMap<String, Object>();

        public AnySetterBuilder withName(String name) {
            this.name = name;
            return this;
        }

        @JsonAnySetter
        public AnySetterBuilder addOther(String key, Object value) {
            this.others.put(key, value);
            return this;
        }

        public AnySetterBean build() {
            return new AnySetterBean(name, others);
        }
    }

    @JsonDeserialize(builder = CreatorAnySetterBuilder.class)
    static class CreatorAnySetterBean {
        final int id;
        final Map<String, Object> others;

        CreatorAnySetterBean(int id, Map<String, Object> others) {
            this.id = id;
            this.others = others;
        }
    }

    static class CreatorAnySetterBuilder {
        private final int id;
        private final Map<String, Object> others = new HashMap<String, Object>();

        @JsonCreator
        public CreatorAnySetterBuilder(@JsonProperty("id") int id) {
            this.id = id;
        }

        @JsonAnySetter
        public CreatorAnySetterBuilder addOther(String key, Object value) {
            this.others.put(key, value);
            return this;
        }

        public CreatorAnySetterBean build() {
            return new CreatorAnySetterBean(id, others);
        }
    }

    @JsonDeserialize(builder = InjectBuilder.class)
    static class InjectBean {
        final String name;
        final String injected;

        InjectBean(String name, String injected) {
            this.name = name;
            this.injected = injected;
        }
    }

    static class InjectBuilder {
        private String name;
        @JacksonInject("injectedVal")
        private String injected;

        public InjectBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public InjectBean build() {
            return new InjectBean(name, injected);
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = PolyBeanImpl.class, name = "impl")
    })
    interface PolyBean {
        String getData();
    }

    @JsonDeserialize(builder = PolyBeanBuilder.class)
    static class PolyBeanImpl implements PolyBean {
        private final String data;

        PolyBeanImpl(String data) {
            this.data = data;
        }

        @Override
        public String getData() {
            return data;
        }
    }

    static class PolyBeanBuilder {
        private String data;

        public PolyBeanBuilder withData(String data) {
            this.data = data;
            return this;
        }

        public PolyBeanImpl build() {
            return new PolyBeanImpl(data);
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "extType")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = ExtPolyImpl.class, name = "extImpl")
    })
    interface ExtPolyInterface {
    }

    @JsonDeserialize(builder = ExtPolyBuilder.class)
    static class ExtPolyImpl implements ExtPolyInterface {
        final String text;

        ExtPolyImpl(String text) {
            this.text = text;
        }
    }

    static class ExtPolyBuilder {
        private String text;

        public ExtPolyBuilder withText(String text) {
            this.text = text;
            return this;
        }

        public ExtPolyImpl build() {
            return new ExtPolyImpl(text);
        }
    }

    static class ExtPolyContainer {
        public ExtPolyInterface poly;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonDeserialize(builder = IdBeanBuilder.class)
    static class IdBean {
        final int id;
        final String value;
        final IdBean next;

        IdBean(int id, String value, IdBean next) {
            this.id = id;
            this.value = value;
            this.next = next;
        }
    }

    static class IdBeanBuilder {
        private int id;
        private String value;
        private IdBean next;

        public IdBeanBuilder withId(int id) {
            this.id = id;
            return this;
        }

        public IdBeanBuilder withValue(String value) {
            this.value = value;
            return this;
        }

        public IdBeanBuilder withNext(IdBean next) {
            this.next = next;
            return this;
        }

        public IdBean build() {
            return new IdBean(id, value, next);
        }
    }

    @JsonDeserialize(builder = ExplicitIgnoreBuilder.class)
    static class ExplicitIgnoreBean {
        final String kept;

        ExplicitIgnoreBean(String kept) {
            this.kept = kept;
        }
    }

    static class ExplicitIgnoreBuilder {
        private String kept;

        public ExplicitIgnoreBuilder withKept(String kept) {
            this.kept = kept;
            return this;
        }

        @JsonIgnore
        public ExplicitIgnoreBuilder withIgnored(String ignored) {
            return this;
        }

        public ExplicitIgnoreBean build() {
            return new ExplicitIgnoreBean(kept);
        }
    }

    @JsonDeserialize(builder = CreatorViewBuilder.class)
    static class CreatorViewBean {
        final String pubVal;
        final String privVal;

        CreatorViewBean(String pubVal, String privVal) {
            this.pubVal = pubVal;
            this.privVal = privVal;
        }
    }

    static class CreatorViewBuilder {
        private final String pubVal;
        private String privVal;

        @JsonCreator
        public CreatorViewBuilder(@JsonProperty("pubVal") String pubVal) {
            this.pubVal = pubVal;
        }

        @JsonView(Views.Internal.class)
        public CreatorViewBuilder withPrivVal(String privVal) {
            this.privVal = privVal;
            return this;
        }

        public CreatorViewBean build() {
            return new CreatorViewBean(pubVal, privVal);
        }
    }

    // Tests

    // Tests normal vanilla deserialization through builder
    @Test
    public void testDeserialize_standardProperties_createsBean() throws IOException {
        String json = "{\"id\":123,\"name\":\"Jackson\"}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertNotNull(bean);
        assertEquals(123, bean.id);
        assertEquals("Jackson", bean.name);
    }

    // Tests empty object deserialization
    @Test
    public void testDeserialize_emptyObject_createsDefaultBean() throws IOException {
        String json = "{}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertNotNull(bean);
        assertEquals(0, bean.id);
        assertNull(bean.name);
    }

    // Tests deserialization using property-based creator on builder
    @Test
    public void testDeserialize_propertyBasedCreator_createsBean() throws IOException {
        String json = "{\"x\":10,\"y\":20,\"name\":\"point\"}";
        CreatorBean bean = mapper.readValue(json, CreatorBean.class);
        assertNotNull(bean);
        assertEquals(10, bean.x);
        assertEquals(20, bean.y);
        assertEquals("point", bean.name);
    }

    // Tests property-based creator with properties reordered (creator params after normal params)
    @Test
    public void testDeserialize_propertyBasedCreatorReordered_createsBean() throws IOException {
        String json = "{\"name\":\"reordered\",\"y\":5,\"x\":3}";
        CreatorBean bean = mapper.readValue(json, CreatorBean.class);
        assertNotNull(bean);
        assertEquals(3, bean.x);
        assertEquals(5, bean.y);
        assertEquals("reordered", bean.name);
    }

    // Tests unknown property handling when unknown properties are ignored
    @Test
    public void testDeserialize_unknownPropertyIgnored_succeeds() throws IOException {
        String json = "{\"value\":\"test\",\"unknownExtra\":\"ignoreMe\"}";
        IgnorableBean bean = mapper.readValue(json, IgnorableBean.class);
        assertNotNull(bean);
        assertEquals("test", bean.value);
    }

    // Tests unknown property throwing exception when not ignored
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_unknownPropertyNotIgnored_throwsException() throws IOException {
        String json = "{\"id\":1,\"unknownField\":\"fail\"}";
        mapper.readValue(json, SimpleBean.class);
    }

    // Tests view filtering with Builder: public view excludes internal properties
    @Test
    public void testDeserialize_withViewFiltering_includesOnlyViewProperties() throws IOException {
        String json = "{\"publicVal\":\"pub\",\"internalVal\":\"priv\"}";
        ViewBean bean = mapper.readerWithView(Views.Public.class)
                              .forType(ViewBean.class)
                              .readValue(json);
        assertNotNull(bean);
        assertEquals("pub", bean.publicVal);
        assertNull(bean.internalVal);
    }

    // Tests view filtering with Builder: internal view includes both properties
    @Test
    public void testDeserialize_withInternalView_includesAllProperties() throws IOException {
        String json = "{\"publicVal\":\"pub\",\"internalVal\":\"priv\"}";
        ViewBean bean = mapper.readerWithView(Views.Internal.class)
                              .forType(ViewBean.class)
                              .readValue(json);
        assertNotNull(bean);
        assertEquals("pub", bean.publicVal);
        assertEquals("priv", bean.internalVal);
    }

    // Tests unwrapped properties support with builder
    @Test
    public void testDeserialize_unwrappedProperties_populatesUnwrappedObject() throws IOException {
        String json = "{\"name\":\"Place\",\"city\":\"Seattle\",\"country\":\"USA\"}";
        UnwrappedBean bean = mapper.readValue(json, UnwrappedBean.class);
        assertNotNull(bean);
        assertEquals("Place", bean.name);
        assertNotNull(bean.location);
        assertEquals("Seattle", bean.location.city);
        assertEquals("USA", bean.location.country);
    }

    // Tests property-based creator combined with unwrapped properties
    @Test
    public void testDeserialize_creatorWithUnwrappedProperties_createsBean() throws IOException {
        String json = "{\"id\":42,\"city\":\"Boston\",\"country\":\"USA\"}";
        CreatorUnwrappedBean bean = mapper.readValue(json, CreatorUnwrappedBean.class);
        assertNotNull(bean);
        assertEquals(42, bean.id);
        assertNotNull(bean.location);
        assertEquals("Boston", bean.location.city);
        assertEquals("USA", bean.location.country);
    }

    // Tests property-based creator with unwrapped properties arriving before creator properties
    @Test
    public void testDeserialize_creatorWithUnwrappedPropertiesReordered_createsBean() throws IOException {
        String json = "{\"city\":\"San Francisco\",\"country\":\"USA\",\"id\":99}";
        CreatorUnwrappedBean bean = mapper.readValue(json, CreatorUnwrappedBean.class);
        assertNotNull(bean);
        assertEquals(99, bean.id);
        assertNotNull(bean.location);
        assertEquals("San Francisco", bean.location.city);
        assertEquals("USA", bean.location.country);
    }

    // Tests build method throwing exception wraps properly
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_buildMethodThrowsException_throwsJsonMappingException() throws IOException {
        String json = "{\"val\":-1}";
        mapper.readValue(json, ExceptionBean.class);
    }

    // Tests unexpected token when deserializing builder (e.g. JSON array instead of object)
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_unexpectedToken_throwsJsonMappingException() throws IOException {
        String json = "[1, 2, 3]";
        mapper.readValue(json, SimpleBean.class);
    }

    // Tests unexpected token when deserializing builder from JSON string
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_stringTokenWithoutCreator_throwsJsonMappingException() throws IOException {
        String json = "\"unexpectedString\"";
        mapper.readValue(json, SimpleBean.class);
    }

    // Additional tests for complete coverage

    // Tests any-setter support on builder
    @Test
    public void testDeserialize_anySetter_capturesUnknownProperties() throws IOException {
        String json = "{\"name\":\"custom\",\"extra1\":123,\"extra2\":\"value2\"}";
        AnySetterBean bean = mapper.readValue(json, AnySetterBean.class);
        assertNotNull(bean);
        assertEquals("custom", bean.name);
        assertEquals(123, bean.others.get("extra1"));
        assertEquals("value2", bean.others.get("extra2"));
    }

    // Tests any-setter combined with property-based creator on builder
    @Test
    public void testDeserialize_creatorWithAnySetter_capturesProperties() throws IOException {
        String json = "{\"extraKey\":\"extraVal\",\"id\":7}";
        CreatorAnySetterBean bean = mapper.readValue(json, CreatorAnySetterBean.class);
        assertNotNull(bean);
        assertEquals(7, bean.id);
        assertEquals("extraVal", bean.others.get("extraKey"));
    }

    // Tests JacksonInject support on builder
    @Test
    public void testDeserialize_withJacksonInject_injectsValue() throws IOException {
        InjectableValues.Std inject = new InjectableValues.Std();
        inject.addValue("injectedVal", "injectedResult");
        String json = "{\"name\":\"test\"}";
        InjectBean bean = mapper.reader(inject).forType(InjectBean.class).readValue(json);
        assertNotNull(bean);
        assertEquals("test", bean.name);
        assertEquals("injectedResult", bean.injected);
    }

    // Tests polymorphic deserialization with type info and builder
    @Test
    public void testDeserialize_polymorphicBuilder_createsSubtype() throws IOException {
        String json = "{\"type\":\"impl\",\"data\":\"polyData\"}";
        PolyBean bean = mapper.readValue(json, PolyBean.class);
        assertNotNull(bean);
        assertTrue(bean instanceof PolyBeanImpl);
        assertEquals("polyData", bean.getData());
    }

    // Tests external type id deserialization with builder
    @Test
    public void testDeserialize_externalTypeIdWithBuilder_createsSubtype() throws IOException {
        String json = "{\"extType\":\"extImpl\",\"poly\":{\"text\":\"externalData\"}}";
        ExtPolyContainer container = mapper.readValue(json, ExtPolyContainer.class);
        assertNotNull(container);
        assertNotNull(container.poly);
        assertTrue(container.poly instanceof ExtPolyImpl);
        assertEquals("externalData", ((ExtPolyImpl) container.poly).text);
    }

    // Tests object identity handling with builder
    @Test
    public void testDeserialize_identityInfoWithBuilder_resolvesReferences() throws IOException {
        String json = "{\"id\":1,\"value\":\"first\",\"next\":{\"id\":2,\"value\":\"second\",\"next\":1}}";
        IdBean bean = mapper.readValue(json, IdBean.class);
        assertNotNull(bean);
        assertEquals(1, bean.id);
        assertEquals("first", bean.value);
        assertNotNull(bean.next);
        assertEquals(2, bean.next.id);
        assertSame(bean, bean.next.next);
    }

    // Tests explicitly ignored properties on builder
    @Test
    public void testDeserialize_explicitIgnoreProperty_ignoresProperty() throws IOException {
        String json = "{\"kept\":\"keptValue\",\"ignored\":\"ignoredValue\"}";
        ExplicitIgnoreBean bean = mapper.readValue(json, ExplicitIgnoreBean.class);
        assertNotNull(bean);
        assertEquals("keptValue", bean.kept);
    }

    // Tests creator with view filtering
    @Test
    public void testDeserialize_creatorWithViewFiltering_includesOnlyViewProperties() throws IOException {
        String json = "{\"pubVal\":\"pub\",\"privVal\":\"priv\"}";
        CreatorViewBean bean = mapper.readerWithView(Views.Public.class)
                                     .forType(CreatorViewBean.class)
                                     .readValue(json);
        assertNotNull(bean);
        assertEquals("pub", bean.pubVal);
        assertNull(bean.privVal);
    }
}