package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

public class BeanDeserializerTest {

    private final ObjectMapper MAPPER = new ObjectMapper();

    // Helper classes for testing various deserialization mechanisms

    static class SimpleBean {
        public int x;
        public String y;

        public SimpleBean() { }
        public SimpleBean(int x, String y) {
            this.x = x;
            this.y = y;
        }
    }

    static class CreatorBean {
        final int a;
        final String b;
        int c;

        @JsonCreator
        public CreatorBean(@JsonProperty("a") int a, @JsonProperty("b") String b) {
            this.a = a;
            this.b = b;
        }

        public void setC(int c) {
            this.c = c;
        }
    }

    static class AnySetterBean {
        public int id;
        public Map<String, Object> extra = new HashMap<String, Object>();

        @JsonAnySetter
        public void setExtra(String key, Object value) {
            extra.put(key, value);
        }
    }

    @JsonIgnoreProperties({ "ignored1", "ignored2" })
    static class IgnorableBean {
        public int value;
    }

    static class Views {
        static class Public { }
        static class Internal extends Public { }
    }

    static class ViewBean {
        @JsonView(Views.Public.class)
        public int pub;

        @JsonView(Views.Internal.class)
        public int internal;
    }

    static class UnwrappedContainer {
        public int id;
        @JsonUnwrapped
        public SimpleBean unwrapped;
    }

    static class PrefixedUnwrappedContainer {
        public int id;
        @JsonUnwrapped(prefix = "pre_")
        public SimpleBean unwrapped;
    }

    static class CreatorUnwrappedContainer {
        final int id;
        @JsonUnwrapped
        public SimpleBean unwrapped;

        @JsonCreator
        public CreatorUnwrappedContainer(@JsonProperty("id") int id) {
            this.id = id;
        }
    }

    // External type id test hierarchy
    interface ExtInterface { }

    static class ExtImpl1 implements ExtInterface {
        public int value1;
        public ExtImpl1() { }
        public ExtImpl1(int v) { this.value1 = v; }
    }

    static class ExtImpl2 implements ExtInterface {
        public String value2;
        public ExtImpl2() { }
        public ExtImpl2(String v) { this.value2 = v; }
    }

    static class ExternalTypeIdBean {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = ExtImpl1.class, name = "impl1"),
            @JsonSubTypes.Type(value = ExtImpl2.class, name = "impl2")
        })
        public ExtInterface ext;
    }

    // Defects4J bug 27: External type id combined with @JsonCreator property-based creator
    static class ExternalTypeIdCreatorBean {
        final String name;
        final String type;
        final ExtInterface ext;

        @JsonCreator
        public ExternalTypeIdCreatorBean(
                @JsonProperty("name") String name,
                @JsonProperty("type") String type,
                @JsonProperty("ext")
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
                @JsonSubTypes({
                    @JsonSubTypes.Type(value = ExtImpl1.class, name = "impl1"),
                    @JsonSubTypes.Type(value = ExtImpl2.class, name = "impl2")
                }) ExtInterface ext) {
            this.name = name;
            this.type = type;
            this.ext = ext;
        }
    }

    static class StringCtorBean {
        final String val;

        @JsonCreator
        public StringCtorBean(String s) {
            this.val = s;
        }
    }

    static class IntCtorBean {
        final int val;

        @JsonCreator
        public IntCtorBean(int v) {
            this.val = v;
        }
    }

    static class DoubleCtorBean {
        final double val;

        @JsonCreator
        public DoubleCtorBean(double v) {
            this.val = v;
        }
    }

    static class BooleanCtorBean {
        final boolean val;

        @JsonCreator
        public BooleanCtorBean(boolean v) {
            this.val = v;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    static class IdNode {
        public int val;
        public IdNode next;

        public IdNode() { }
        public IdNode(int val) { this.val = val; }
    }

    static class Parent {
        public int id;
        @JsonManagedReference
        public Child child;
    }

    static class Child {
        public String name;
        @JsonBackReference
        public Parent parent;
    }

    @JsonDeserialize(builder = BuilderValueClass.Builder.class)
    static class BuilderValueClass {
        final int a;
        final String b;

        BuilderValueClass(int a, String b) {
            this.a = a;
            this.b = b;
        }

        @JsonPOJOBuilder(withPrefix = "with")
        static class Builder {
            private int a;
            private String b;

            public Builder withA(int a) {
                this.a = a;
                return this;
            }

            public Builder withB(String b) {
                this.b = b;
                return this;
            }

            public BuilderValueClass build() {
                return new BuilderValueClass(a, b);
            }
        }
    }

    static class InjectedBean {
        public int x;
        @JacksonInject
        public String injected;
    }

    // Tests defect 27: Property-based creator with external type id
    @Test
    public void testDeserialize_externalTypeIdWithCreator_successfullyDeserializes() throws Exception {
        String json = "{\"name\":\"test\",\"ext\":{\"value1\":42},\"type\":\"impl1\"}";
        ExternalTypeIdCreatorBean bean = MAPPER.readValue(json, ExternalTypeIdCreatorBean.class);
        assertNotNull(bean);
        assertEquals("test", bean.name);
        assertEquals("impl1", bean.type);
        assertTrue(bean.ext instanceof ExtImpl1);
        assertEquals(42, ((ExtImpl1) bean.ext).value1);
    }

    // Tests normal vanilla deserialization
    @Test
    public void testDeserialize_vanillaObject_returnsCorrectValues() throws Exception {
        String json = "{\"x\":10,\"y\":\"hello\"}";
        SimpleBean bean = MAPPER.readValue(json, SimpleBean.class);
        assertNotNull(bean);
        assertEquals(10, bean.x);
        assertEquals("hello", bean.y);
    }

    // Tests updating an existing bean instance (deserialize with value)
    @Test
    public void testDeserialize_intoExistingBean_updatesValues() throws Exception {
        SimpleBean bean = new SimpleBean(1, "initial");
        SimpleBean updated = MAPPER.readerForUpdating(bean).readValue("{\"x\":99,\"y\":\"updated\"}");
        assertSame(bean, updated);
        assertEquals(99, bean.x);
        assertEquals("updated", bean.y);
    }

    // Tests property-based creator deserialization with buffered properties
    @Test
    public void testDeserialize_propertyBasedCreator_createsInstance() throws Exception {
        String json = "{\"c\":30,\"b\":\"text\",\"a\":20}";
        CreatorBean bean = MAPPER.readValue(json, CreatorBean.class);
        assertNotNull(bean);
        assertEquals(20, bean.a);
        assertEquals("text", bean.b);
        assertEquals(30, bean.c);
    }

    // Tests ignorable properties handling
    @Test
    public void testDeserialize_ignorableProperties_skipsIgnoredFields() throws Exception {
        String json = "{\"ignored1\":\"foo\",\"value\":100,\"ignored2\":123}";
        IgnorableBean bean = MAPPER.readValue(json, IgnorableBean.class);
        assertNotNull(bean);
        assertEquals(100, bean.value);
    }

    // Tests any setter handling for unknown properties
    @Test
    public void testDeserialize_anySetter_capturesUnknownProperties() throws Exception {
        String json = "{\"id\":1,\"customField\":\"customValue\",\"otherNum\":5}";
        AnySetterBean bean = MAPPER.readValue(json, AnySetterBean.class);
        assertNotNull(bean);
        assertEquals(1, bean.id);
        assertEquals("customValue", bean.extra.get("customField"));
        assertEquals(Integer.valueOf(5), bean.extra.get("otherNum"));
    }

    // Tests deserialization with active view
    @Test
    public void testDeserialize_withView_onlyDeserializesVisibleProperties() throws Exception {
        String json = "{\"pub\":10,\"internal\":20}";
        ViewBean bean = MAPPER.readerWithView(Views.Public.class)
                .forType(ViewBean.class)
                .readValue(json);
        assertNotNull(bean);
        assertEquals(10, bean.pub);
        assertEquals(0, bean.internal);
    }

    // Tests unwrapped properties deserialization with default constructor
    @Test
    public void testDeserialize_unwrappedProperties_populatesNestedBean() throws Exception {
        String json = "{\"id\":5,\"x\":12,\"y\":\"unwrapped\"}";
        UnwrappedContainer bean = MAPPER.readValue(json, UnwrappedContainer.class);
        assertNotNull(bean);
        assertEquals(5, bean.id);
        assertNotNull(bean.unwrapped);
        assertEquals(12, bean.unwrapped.x);
        assertEquals("unwrapped", bean.unwrapped.y);
    }

    // Tests unwrapped properties with prefix
    @Test
    public void testDeserialize_prefixedUnwrappedProperties_populatesNestedBean() throws Exception {
        String json = "{\"id\":8,\"pre_x\":55,\"pre_y\":\"prefixed\"}";
        PrefixedUnwrappedContainer bean = MAPPER.readValue(json, PrefixedUnwrappedContainer.class);
        assertNotNull(bean);
        assertEquals(8, bean.id);
        assertNotNull(bean.unwrapped);
        assertEquals(55, bean.unwrapped.x);
        assertEquals("prefixed", bean.unwrapped.y);
    }

    // Tests unwrapped properties with property-based creator
    @Test
    public void testDeserialize_unwrappedWithPropertyBasedCreator_populatesFields() throws Exception {
        String json = "{\"id\":7,\"x\":14,\"y\":\"creatorUnwrapped\"}";
        CreatorUnwrappedContainer bean = MAPPER.readValue(json, CreatorUnwrappedContainer.class);
        assertNotNull(bean);
        assertEquals(7, bean.id);
        assertNotNull(bean.unwrapped);
        assertEquals(14, bean.unwrapped.x);
        assertEquals("creatorUnwrapped", bean.unwrapped.y);
    }

    // Tests external type id deserialization with default constructor
    @Test
    public void testDeserialize_externalTypeIdDefaultConstructor_populatesSubtype() throws Exception {
        String json = "{\"type\":\"impl2\",\"ext\":{\"value2\":\"external\"}}";
        ExternalTypeIdBean bean = MAPPER.readValue(json, ExternalTypeIdBean.class);
        assertNotNull(bean);
        assertEquals("impl2", bean.type);
        assertTrue(bean.ext instanceof ExtImpl2);
        assertEquals("external", ((ExtImpl2) bean.ext).value2);
    }

    // Tests delegating creator from String scalar
    @Test
    public void testDeserialize_fromString_callsStringCreator() throws Exception {
        String json = "\"testString\"";
        StringCtorBean bean = MAPPER.readValue(json, StringCtorBean.class);
        assertNotNull(bean);
        assertEquals("testString", bean.val);
    }

    // Tests delegating creator from Int scalar
    @Test
    public void testDeserialize_fromInt_callsIntCreator() throws Exception {
        String json = "12345";
        IntCtorBean bean = MAPPER.readValue(json, IntCtorBean.class);
        assertNotNull(bean);
        assertEquals(12345, bean.val);
    }

    // Tests delegating creator from Double scalar
    @Test
    public void testDeserialize_fromDouble_callsDoubleCreator() throws Exception {
        String json = "3.1415";
        DoubleCtorBean bean = MAPPER.readValue(json, DoubleCtorBean.class);
        assertNotNull(bean);
        assertEquals(3.1415, bean.val, 0.00001);
    }

    // Tests delegating creator from Boolean scalar
    @Test
    public void testDeserialize_fromBoolean_callsBooleanCreator() throws Exception {
        String json = "true";
        BooleanCtorBean bean = MAPPER.readValue(json, BooleanCtorBean.class);
        assertNotNull(bean);
        assertTrue(bean.val);
    }

    // Tests unknown property failure path
    @Test(expected = UnrecognizedPropertyException.class)
    public void testDeserialize_unknownProperty_throwsException() throws Exception {
        String json = "{\"x\":1,\"unknown\":true}";
        MAPPER.readValue(json, SimpleBean.class);
    }

    // Tests deserialization with cyclic Object Identity
    @Test
    public void testDeserialize_withObjectIdentity_resolvesReferences() throws Exception {
        String json = "{\"@id\":1,\"val\":100,\"next\":1}";
        IdNode node = MAPPER.readValue(json, IdNode.class);
        assertNotNull(node);
        assertEquals(100, node.val);
        assertSame(node, node.next);
    }

    // Tests deserialization with Managed and Back references
    @Test
    public void testDeserialize_managedAndBackReference_linksObjects() throws Exception {
        String json = "{\"id\":10,\"child\":{\"name\":\"kid\"}}";
        Parent parent = MAPPER.readValue(json, Parent.class);
        assertNotNull(parent);
        assertEquals(10, parent.id);
        assertNotNull(parent.child);
        assertEquals("kid", parent.child.name);
        assertSame(parent, parent.child.parent);
    }

    // Tests builder-based deserialization
    @Test
    public void testDeserialize_withBuilder_buildsInstance() throws Exception {
        String json = "{\"a\":42,\"b\":\"builderVal\"}";
        BuilderValueClass val = MAPPER.readValue(json, BuilderValueClass.class);
        assertNotNull(val);
        assertEquals(42, val.a);
        assertEquals("builderVal", val.b);
    }

    // Tests unwrap single value array feature
    @Test
    public void testDeserialize_unwrapSingleValueArray_successfullyDeserializes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        SimpleBean bean = mapper.readValue("[{\"x\":5,\"y\":\"wrapped\"}]", SimpleBean.class);
        assertNotNull(bean);
        assertEquals(5, bean.x);
        assertEquals("wrapped", bean.y);
    }

    // Tests null value deserialization
    @Test
    public void testDeserialize_nullLiteral_returnsNull() throws Exception {
        SimpleBean bean = MAPPER.readValue("null", SimpleBean.class);
        assertNull(bean);
    }

    // Tests injectable values support
    @Test
    public void testDeserialize_withJacksonInject_injectsValues() throws Exception {
        InjectableValues.Std injectables = new InjectableValues.Std();
        injectables.addValue(String.class.getName(), "injectedValue");
        InjectedBean bean = MAPPER.reader(injectables)
                .forType(InjectedBean.class)
                .readValue("{\"x\":7}");
        assertNotNull(bean);
        assertEquals(7, bean.x);
        assertEquals("injectedValue", bean.injected);
    }
}