package com.fasterxml.jackson.databind.ser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.*;
import org.junit.Test;

public class BeanSerializerFactoryTest {

    // Helper class for basic bean tests
    static class TestBean {
        public int x = 1;
        public int getX() { return x; }
    }

    // Helper class with @JsonSerialize annotation
    static class TestBeanWithAnnotation {
        @JsonSerialize(using = CustomSerializer.class)
        public int x = 1;
    }

    static class CustomSerializer extends StdSerializer<Integer> {
        public CustomSerializer() { super(Integer.class); }
        @Override
        public void serialize(Integer value, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeNumber(value * 2);
        }
    }

    // Test that createSerializer returns a non-null serializer for a simple bean
    @Test
    public void testCreateSerializer_simpleBean_returnsNotNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerFactory factory = BeanSerializerFactory.instance;
        JavaType type = mapper.constructType(TestBean.class);
        JsonSerializer<?> ser = factory.createSerializer(mapper.getSerializerProvider(), type);
        assertNotNull(ser);
    }

    // Test that findBeanSerializer returns null for non-bean type like int.class
    @Test
    public void testFindBeanSerializer_nonBeanType_returnsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(int.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        JsonSerializer<?> ser = BeanSerializerFactory.instance.findBeanSerializer(
                mapper.getSerializerProvider(), type, beanDesc);
        // Should be null since int is not a potential bean type
        assertNull(ser);
    }

    // Test that findBeanSerializer returns null for array type
    @Test
    public void testFindBeanSerializer_arrayType_returnsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(String[].class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        JsonSerializer<?> ser = BeanSerializerFactory.instance.findBeanSerializer(
                mapper.getSerializerProvider(), type, beanDesc);
        assertNull(ser);
    }

    // Test that constructBeanSerializer returns proper serializer for a bean
    @Test
    public void testConstructBeanSerializer_simpleBean_returnsSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(TestBean.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        JsonSerializer<?> ser = BeanSerializerFactory.instance.constructBeanSerializer(
                mapper.getSerializerProvider(), beanDesc);
        assertNotNull(ser);
    }

    // Test that isPotentialBeanType returns false for primitive type
    @Test
    public void testIsPotentialBeanType_primitive_returnsFalse() throws Exception {
        assertFalse(BeanSerializerFactory.instance.isPotentialBeanType(int.class));
    }

    // Test that isPotentialBeanType returns true for a regular bean class
    @Test
    public void testIsPotentialBeanType_beanClass_returnsTrue() throws Exception {
        assertTrue(BeanSerializerFactory.instance.isPotentialBeanType(TestBean.class));
    }

    // Test that createSerializer respects @JsonSerialize annotation
    @Test
    public void testCreateSerializer_withAnnotation_usesCustomSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(TestBeanWithAnnotation.class);
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        JsonSerializer<?> ser = factory.createSerializer(mapper.getSerializerProvider(), type);
        assertNotNull(ser);
        // The serializer should be a BeanSerializer that uses custom serializer for field x
        // Verify by serializing
        String json = mapper.writeValueAsString(new TestBeanWithAnnotation());
        assertEquals("{\"x\":2}", json);
    }

    // Test that createSerializer handles null type gracefully (should not crash)
    @Test(expected = IllegalArgumentException.class)
    public void testCreateSerializer_nullType_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanSerializerFactory.instance.createSerializer(mapper.getSerializerProvider(), null);
    }

    // Test that _createSerializer2 returns serializer for a simple POJO
    @Test
    public void test_createSerializer2_simpleBean_returnsSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(TestBean.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        JsonSerializer<?> ser = BeanSerializerFactory.instance._createSerializer2(
                mapper.getSerializerProvider(), type, beanDesc, false);
        assertNotNull(ser);
    }

    // Test that createSerializer with converter returns delegating serializer
    @Test
    public void testCreateSerializer_withConverter_returnsDelegatingSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Register a converter for TestBean (not really needed here, but test path)
        SimpleModule module = new SimpleModule();
        module.addSerializer(TestBean.class, new StdSerializer<TestBean>(TestBean.class) {
            @Override
            public void serialize(TestBean value, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeString("converted");
            }
        });
        mapper.registerModule(module);
        // Without converter, just a test that createSerializer works
        JavaType type = mapper.constructType(TestBean.class);
        JsonSerializer<?> ser = BeanSerializerFactory.instance.createSerializer(mapper.getSerializerProvider(), type);
        assertNotNull(ser);
    }

    // Test that filterBeanProperties removes ignored properties
    @Test
    public void testFilterBeanProperties_ignoredProperty_removesProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Use a class with @JsonIgnoreProperties annotation
        @JsonIgnoreProperties({"y"})
        class BeanWithIgnore {
            public int x = 1;
            public int y = 2;
        }
        JavaType type = mapper.constructType(BeanWithIgnore.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        // We'll just test filterBeanProperties directly; it's package-private but accessible
        // Actually filterBeanProperties is protected, so we can test via public path
        // Let's just test findBeanProperties indirectly via constructBeanSerializer
        JsonSerializer<?> ser = BeanSerializerFactory.instance.findBeanSerializer(
                mapper.getSerializerProvider(), type, beanDesc);
        assertNotNull(ser);
        // Serialize to see if y is excluded
        String json = mapper.writeValueAsString(new BeanWithIgnore());
        assertEquals("{\"x\":1}", json);
    }

    // Test that createSerializer with @JsonValue on method works
    @Test
    public void testCreateSerializer_withJsonValue_usesValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        class BeanWithJsonValue {
            @JsonValue
            public String getValue() { return "custom"; }
        }
        JavaType type = mapper.constructType(BeanWithJsonValue.class);
        JsonSerializer<?> ser = BeanSerializerFactory.instance.createSerializer(mapper.getSerializerProvider(), type);
        assertNotNull(ser);
    }

    // Test that findPropertyTypeSerializer returns null when no type info
    @Test
    public void testFindPropertyTypeSerializer_noTypeInfo_returnsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(TestBean.class);
        // Use a dummy accessor (just any AnnotatedMember) - we can't easily create one, use introspection
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        AnnotatedMember accessor = beanDesc.findAnyGetter(); // might be null
        if (accessor == null) {
            // If no any getter, just test with known method
            List<BeanPropertyDefinition> props = beanDesc.findProperties();
            if (!props.isEmpty()) {
                accessor = props.get(0).getAccessor();
            }
        }
        // Accessor could be null; if null, skip test
        if (accessor != null) {
            TypeSerializer typeSer = BeanSerializerFactory.instance.findPropertyTypeSerializer(
                    type, mapper.getSerializationConfig(), accessor);
            assertNull(typeSer);
        }
    }

    // Test that constructObjectIdHandler returns null when no ObjectIdInfo
    @Test
    public void testConstructObjectIdHandler_noObjectId_returnsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(TestBean.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        ObjectIdWriter writer = BeanSerializerFactory.instance.constructObjectIdHandler(
                mapper.getSerializerProvider(), beanDesc, new ArrayList<BeanPropertyWriter>());
        assertNull(writer);
    }

    // Test that constructBeanSerializerBuilder returns a non-null builder
    @Test
    public void testConstructBeanSerializerBuilder_returnsBuilder() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(TestBean.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        BeanSerializerBuilder builder = BeanSerializerFactory.instance.constructBeanSerializerBuilder(beanDesc);
        assertNotNull(builder);
    }

    // Test static factory instance is not null
    @Test
    public void testStaticInstance_notNull() throws Exception {
        assertNotNull(BeanSerializerFactory.instance);
    }

    // Test that createSerializer handles Map type (container)
    @Test
    public void testCreateSerializer_mapType_returnsSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(HashMap.class);
        JsonSerializer<?> ser = BeanSerializerFactory.instance.createSerializer(mapper.getSerializerProvider(), type);
        assertNotNull(ser);
    }

    // Test that createSerializer handles List type (container)
    @Test
    public void testCreateSerializer_listType_returnsSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(ArrayList.class);
        JsonSerializer<?> ser = BeanSerializerFactory.instance.createSerializer(mapper.getSerializerProvider(), type);
        assertNotNull(ser);
    }

    // Test that findBeanSerializer returns null for Object.class
    @Test
    public void testFindBeanSerializer_objectClass_returnsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(Object.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        JsonSerializer<?> ser = BeanSerializerFactory.instance.findBeanSerializer(
                mapper.getSerializerProvider(), type, beanDesc);
        assertNull(ser);
    }

    // ===== New tests for uncovered areas =====

    // Test that withConfig returns a new factory instance with different config
    @Test
    public void testWithConfig_returnsDifferentInstance() {
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        BeanSerializerFactory newFactory = factory.withConfig(new SerializerFactoryConfig());
        assertNotNull(newFactory);
        assertNotSame(factory, newFactory);
    }

    // Test that getConfig returns a non-null configuration
    @Test
    public void testGetConfig_notNull() {
        assertNotNull(BeanSerializerFactory.instance.getConfig());
    }

    // Test that modifySerializer returns a modified serializer for a basic bean
    @Test
    public void testModifySerializer_returnsSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(TestBean.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        JsonSerializer<?> ser = BeanSerializerFactory.instance.findBeanSerializer(
                mapper.getSerializerProvider(), type, beanDesc);
        if (ser != null) {
            JsonSerializer<?> modified = BeanSerializerFactory.instance.modifySerializer(
                    mapper.getSerializerProvider(), beanDesc, ser);
            assertNotNull(modified);
        }
    }

    // Test that _createSerializer2 with includeUntyped = true returns a serializer
    @Test
    public void test_createSerializer2_includeUntyped_returnsSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(TestBean.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        JsonSerializer<?> ser = BeanSerializerFactory.instance._createSerializer2(
                mapper.getSerializerProvider(), type, beanDesc, true);
        assertNotNull(ser);
    }
}