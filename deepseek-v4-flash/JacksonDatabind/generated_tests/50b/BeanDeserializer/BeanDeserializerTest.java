package com.fasterxml.jackson.databind.deser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.exc.*;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.junit.Before;
import org.junit.Test;

public class BeanDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        // Disable some features to simplify testing
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    // -----------------------------------------------------------------------
    // Helper beans (existing)
    // -----------------------------------------------------------------------

    // Simple bean with fields
    static class SimpleBean {
        public int id;
        public String name;

        public SimpleBean() {}

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    // Bean with property-based creator (single arg)
    static class CreatorBean {
        final int id;
        String extra;

        @JsonCreator
        public CreatorBean(@JsonProperty("id") int id) {
            this.id = id;
        }

        public int getId() { return id; }
        public String getExtra() { return extra; }
        public void setExtra(String extra) { this.extra = extra; }
    }

    // Bean with two-arg creator and additional properties
    static class TwoArgCreatorBean {
        final int x;
        final String y;
        String z;

        @JsonCreator
        public TwoArgCreatorBean(@JsonProperty("x") int x,
                                 @JsonProperty("y") String y) {
            this.x = x;
            this.y = y;
        }

        public int getX() { return x; }
        public String getY() { return y; }
        public String getZ() { return z; }
        public void setZ(String z) { this.z = z; }
    }

    // Bean with unwrapped property
    static class UnwrappedBean {
        public int a;
        public String b;

        @JsonUnwrapped
        public Inner inner;

        public UnwrappedBean() {}

        static class Inner {
            public int c;
            public String d;
        }
    }

    // Bean with external type id (using type info on property)
    static class ExternalTypeBean {
        public int base;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        public Object value;

        public ExternalTypeBean() {}
    }

    static class AValue {
        public int a;
    }

    static class BValue {
        public int b;
    }

    @JsonSubTypes({
        @JsonSubTypes.Type(value = AValue.class, name = "A"),
        @JsonSubTypes.Type(value = BValue.class, name = "B")
    })
    static class AbstractValue {
    }

    // Bean with object id
    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    static class IdBean {
        public int data;
        public IdBean next;
        public IdBean() {}
    }

    // Bean with array shape
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    static class ArrayBean {
        public int a;
        public String b;
        public ArrayBean() {}
    }

    // Bean with ignorable properties
    @JsonIgnoreProperties({ "ignored1", "ignored2" })
    static class IgnorableBean {
        public int keep;
        public String keepString;
        public IgnorableBean() {}
    }

    // Bean with delegate creator (using JacksonCreators)
    static class DelegateBean {
        final String value;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public DelegateBean(String value) {
            this.value = value;
        }

        public String getValue() { return value; }
    }

    // Bean with @JsonView
    static class ViewBean {
        @JsonView(Views.Public.class)
        public int publicField;

        @JsonView(Views.Internal.class)
        public String internalField;

        public ViewBean() {}
    }

    static class Views {
        static class Public {}
        static class Internal {}
    }

    // -----------------------------------------------------------------------
    // New helper beans for additional coverage
    // -----------------------------------------------------------------------

    // Bean with a list property
    static class ListBean {
        public List<String> items;
        public ListBean() {}
    }

    // Bean with a map property
    static class MapBean {
        public Map<String, String> properties;
        public MapBean() {}
    }

    // Bean with an enum property
    static class EnumBean {
        public Status status;
        public EnumBean() {}
    }

    enum Status {
        ACTIVE, INACTIVE
    }

    // Bean with a nested object
    static class OuterBean {
        public int id;
        public InnerBean inner;
        public OuterBean() {}
    }

    static class InnerBean {
        public String value;
        public InnerBean() {}
    }

    // Bean with @JsonAnySetter
    static class AnySetterBean {
        public int known;
        protected Map<String, Object> extras = new LinkedHashMap<>();

        @JsonAnySetter
        public void setExtra(String name, Object value) {
            extras.put(name, value);
        }

        public Map<String, Object> getExtras() { return extras; }
    }

    // Bean with @JsonIgnore
    static class IgnoreFieldBean {
        public int visible;
        @JsonIgnore
        public String hidden;
        public IgnoreFieldBean() {}
    }

    // Bean with @JsonAlias
    static class AliasBean {
        @JsonAlias({ "alias1", "alias2" })
        public String name;
        public AliasBean() {}
    }

    // ----------------------------------------------------------------
    // Existing Tests
    // ----------------------------------------------------------------

    @Test
    public void testDeserialize_vanillaSimpleBean_returnsCorrectObject() throws IOException {
        String json = "{\"id\":42,\"name\":\"test\"}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertEquals(42, bean.id);
        assertEquals("test", bean.name);
    }

    @Test
    public void testDeserialize_unknownProperties_ignored() throws IOException {
        String json = "{\"keep\":1,\"keepString\":\"ok\",\"unknown\":99}";
        IgnorableBean bean = mapper.readValue(json, IgnorableBean.class);
        assertEquals(1, bean.keep);
        assertEquals("ok", bean.keepString);
    }

    @Test
    public void testDeserialize_ignorableProperties_ignored() throws IOException {
        String json = "{\"keep\":2,\"keepString\":\"yes\",\"ignored1\":\"x\",\"ignored2\":\"y\"}";
        IgnorableBean bean = mapper.readValue(json, IgnorableBean.class);
        assertEquals(2, bean.keep);
        assertEquals("yes", bean.keepString);
    }

    @Test
    public void testDeserialize_propertyBasedCreator_singleArg() throws IOException {
        String json = "{\"id\":100,\"extra\":\"extra\"}";
        CreatorBean bean = mapper.readValue(json, CreatorBean.class);
        assertEquals(100, bean.id);
        assertEquals("extra", bean.extra);
    }

    @Test
    public void testDeserialize_propertyBasedCreator_twoArgs() throws IOException {
        String json = "{\"x\":1,\"y\":\"two\",\"z\":\"three\"}";
        TwoArgCreatorBean bean = mapper.readValue(json, TwoArgCreatorBean.class);
        assertEquals(1, bean.x);
        assertEquals("two", bean.y);
        assertEquals("three", bean.z);
    }

    @Test
    public void testDeserialize_delegateCreator_string() throws IOException {
        String json = "\"delegateValue\"";
        DelegateBean bean = mapper.readValue(json, DelegateBean.class);
        assertEquals("delegateValue", bean.value);
    }

    @Test
    public void testDeserialize_unwrappedProperties_combinesInner() throws IOException {
        String json = "{\"a\":1,\"b\":\"outer\",\"c\":2,\"d\":\"inner\"}";
        UnwrappedBean bean = mapper.readValue(json, UnwrappedBean.class);
        assertEquals(1, bean.a);
        assertEquals("outer", bean.b);
        assertNotNull(bean.inner);
        assertEquals(2, bean.inner.c);
        assertEquals("inner", bean.inner.d);
    }

    @Test
    public void testDeserialize_externalTypeId_resolvesPolymorphic() throws IOException {
        SimpleModule mod = new SimpleModule();
        mod.addAbstractTypeMapping(AbstractValue.class, AValue.class);
        mod.addAbstractTypeMapping(AbstractValue.class, BValue.class);
        mapper.registerModule(mod);

        String json = "{\"base\":10,\"type\":\"A\",\"value\":{\"a\":5}}";
        ExternalTypeBean bean = mapper.readValue(json, ExternalTypeBean.class);
        assertEquals(10, bean.base);
        assertNotNull(bean.value);
        assertTrue(bean.value instanceof AValue);
        assertEquals(5, ((AValue) bean.value).a);
    }

    @Test
    public void testDeserialize_objectId_resolvesCycle() throws IOException {
        String json = "{\"@id\":1,\"data\":10,\"next\":{\"@id\":2,\"data\":20,\"next\":1}}";
        IdBean bean = mapper.readValue(json, IdBean.class);
        assertEquals(10, bean.data);
        assertNotNull(bean.next);
        assertEquals(20, bean.next.data);
        assertSame(bean, bean.next.next);
    }

    @Test
    public void testDeserialize_withView_ignoresHidden() throws IOException {
        String json = "{\"publicField\":123,\"internalField\":\"secret\"}";
        ObjectMapper mapperWithView = mapper.copy();
        mapperWithView.setConfig(mapperWithView.getDeserializationConfig().withView(Views.Public.class));
        ViewBean bean = mapperWithView.readValue(json, ViewBean.class);
        assertEquals(123, bean.publicField);
        assertNull(bean.internalField);
    }

    @Test(expected = MismatchedInputException.class)
    public void testDeserialize_unexpectedToken_String_throwsException() throws IOException {
        String json = "\"not an object\"";
        mapper.readValue(json, SimpleBean.class);
    }

    @Test
    public void testDeserialize_arrayShape_createsObject() throws IOException {
        String json = "[100,\"hello\"]";
        ArrayBean bean = mapper.readValue(json, ArrayBean.class);
        assertEquals(100, bean.a);
        assertEquals("hello", bean.b);
    }

    @Test
    public void testDeserialize_emptyObject_returnsDefaultInstance() throws IOException {
        String json = "{}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertNotNull(bean);
        assertEquals(0, bean.id);
        assertNull(bean.name);
    }

    @Test(expected = MismatchedInputException.class)
    public void testDeserialize_nullToken_throwsException() throws IOException {
        String json = "null";
        mapper.readValue(json, SimpleBean.class);
    }

    // ----------------------------------------------------------------
    // New Tests for uncovered areas
    // ----------------------------------------------------------------

    // Test type mismatch: string value for int field -> should throw JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_typeMismatch_throwsException() throws IOException {
        String json = "{\"id\":\"notAnInt\",\"name\":\"test\"}";
        mapper.readValue(json, SimpleBean.class);
    }

    // Test deserialization of a List property
    @Test
    public void testDeserialize_listProperty() throws IOException {
        String json = "{\"items\":[\"a\",\"b\",\"c\"]}";
        ListBean bean = mapper.readValue(json, ListBean.class);
        assertNotNull(bean.items);
        assertEquals(3, bean.items.size());
        assertEquals("a", bean.items.get(0));
        assertEquals("b", bean.items.get(1));
        assertEquals("c", bean.items.get(2));
    }

    // Test deserialization of a Map property
    @Test
    public void testDeserialize_mapProperty() throws IOException {
        String json = "{\"properties\":{\"key1\":\"val1\",\"key2\":\"val2\"}}";
        MapBean bean = mapper.readValue(json, MapBean.class);
        assertNotNull(bean.properties);
        assertEquals(2, bean.properties.size());
        assertEquals("val1", bean.properties.get("key1"));
        assertEquals("val2", bean.properties.get("key2"));
    }

    // Test deserialization of an enum property
    @Test
    public void testDeserialize_enumProperty() throws IOException {
        String json = "{\"status\":\"ACTIVE\"}";
        EnumBean bean = mapper.readValue(json, EnumBean.class);
        assertEquals(Status.ACTIVE, bean.status);
    }

    // Test deserialization of a nested object
    @Test
    public void testDeserialize_nestedObject() throws IOException {
        String json = "{\"id\":1,\"inner\":{\"value\":\"innerValue\"}}";
        OuterBean bean = mapper.readValue(json, OuterBean.class);
        assertEquals(1, bean.id);
        assertNotNull(bean.inner);
        assertEquals("innerValue", bean.inner.value);
    }

    // Test @JsonAnySetter with unknown properties
    @Test
    public void testDeserialize_anySetter_capturesExtras() throws IOException {
        String json = "{\"known\":42,\"unknown1\":\"value1\",\"unknown2\":true}";
        AnySetterBean bean = mapper.readValue(json, AnySetterBean.class);
        assertEquals(42, bean.known);
        assertNotNull(bean.extras);
        assertEquals(2, bean.extras.size());
        assertEquals("value1", bean.extras.get("unknown1"));
        assertEquals(true, bean.extras.get("unknown2"));
    }

    // Test @JsonIgnore field is not set during deserialization
    @Test
    public void testDeserialize_ignoreField_notSet() throws IOException {
        String json = "{\"visible\":10,\"hidden\":\"shouldBeIgnored\"}";
        IgnoreFieldBean bean = mapper.readValue(json, IgnoreFieldBean.class);
        assertEquals(10, bean.visible);
        assertNull(bean.hidden);
    }

    // Test @JsonAlias: property can be set via alias name
    @Test
    public void testDeserialize_aliasProperty() throws IOException {
        String json = "{\"alias1\":\"valueViaAlias\"}";
        AliasBean bean = mapper.readValue(json, AliasBean.class);
        assertEquals("valueViaAlias", bean.name);
    }

    // Test another alias name
    @Test
    public void testDeserialize_aliasProperty_secondAlias() throws IOException {
        String json = "{\"alias2\":\"valueViaAlias2\"}";
        AliasBean bean = mapper.readValue(json, AliasBean.class);
        assertEquals("valueViaAlias2", bean.name);
    }

    // Test invalid delegate creator: object instead of string -> should throw exception
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_delegateCreator_invalidPayload_throwsException() throws IOException {
        String json = "{\"not\":\"aString\"}";
        mapper.readValue(json, DelegateBean.class);
    }

    // Test external type id missing type property -> should throw exception
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_externalTypeId_missingType_throwsException() throws IOException {
        SimpleModule mod = new SimpleModule();
        mod.addAbstractTypeMapping(AbstractValue.class, AValue.class);
        mapper.registerModule(mod);
        String json = "{\"base\":10,\"value\":{\"a\":5}}";
        mapper.readValue(json, ExternalTypeBean.class);
    }

    // Test array-shaped bean with extra elements -> should ignore extra
    @Test
    public void testDeserialize_arrayShape_extraElements_ignored() throws IOException {
        String json = "[10,\"hello\",\"extra\",99]";
        ArrayBean bean = mapper.readValue(json, ArrayBean.class);
        assertEquals(10, bean.a);
        assertEquals("hello", bean.b);
    }
}