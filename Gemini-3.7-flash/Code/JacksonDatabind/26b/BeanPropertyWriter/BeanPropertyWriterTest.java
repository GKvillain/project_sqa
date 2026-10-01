package com.fasterxml.jackson.databind.ser;

import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.POJOPropertyBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class BeanPropertyWriterTest {

    private ObjectMapper _mapper;

    static class SimpleBean {
        public String name = "test";
        private int count = 42;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    static class SelfReferencingBean {
        public SelfReferencingBean self = this;
    }

    private static class SubBeanPropertyWriter extends BeanPropertyWriter {
        public SubBeanPropertyWriter() {
            super();
        }

        public SubBeanPropertyWriter(BeanPropertyWriter base) {
            super(base);
        }

        public SubBeanPropertyWriter(BeanPropertyWriter base, PropertyName name) {
            super(base, name);
        }

        public SubBeanPropertyWriter(BeanPropertyWriter base, SerializedString name) {
            super(base, name);
        }
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
    }

    private BeanPropertyWriter createFieldWriter(String propName, String fieldName, JavaType type) throws Exception {
        Field field = SimpleBean.class.getField(fieldName);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(SimpleBean.class, _mapper.getDeserializationConfig());
        AnnotatedField am = new AnnotatedField(field, new AnnotationMap());
        PropertyName pName = new PropertyName(propName);
        POJOPropertyBuilder propDef = new POJOPropertyBuilder(pName, _mapper.getAnnotationIntrospector(), true);
        return new BeanPropertyWriter(propDef, am, ac.getAnnotations(), type, null, null, null, false, null);
    }

    private BeanPropertyWriter createMethodWriter(String propName, String methodName, JavaType type) throws Exception {
        Method method = SimpleBean.class.getMethod(methodName);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(SimpleBean.class, _mapper.getDeserializationConfig());
        AnnotatedMethod am = new AnnotatedMethod(method, new AnnotationMap(), null);
        PropertyName pName = new PropertyName(propName);
        POJOPropertyBuilder propDef = new POJOPropertyBuilder(pName, _mapper.getAnnotationIntrospector(), true);
        return new BeanPropertyWriter(propDef, am, ac.getAnnotations(), type, null, null, null, false, null);
    }

    // Tests default constructor and getter methods on empty instance
    @Test
    public void testDefaultConstructor_defaultState_returnsNullAndDefaults() {
        SubBeanPropertyWriter writer = new SubBeanPropertyWriter();
        assertNull(writer.getName());
        assertNull(writer.getType());
        assertNull(writer.getMember());
        assertNull(writer.getSerializer());
        assertFalse(writer.hasSerializer());
        assertFalse(writer.hasNullSerializer());
        assertFalse(writer.isVirtual());
        assertFalse(writer.isUnwrapping());
        assertFalse(writer.willSuppressNulls());
    }

    // Tests field-backed writer properties and value access
    @Test
    public void testGet_fieldAccessor_returnsCorrectValue() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanPropertyWriter writer = createFieldWriter("name", "name", type);

        assertEquals("name", writer.getName());
        assertEquals("name", writer.getFullName().getSimpleName());
        assertEquals(type, writer.getType());
        assertEquals(String.class, writer.getPropertyType());
        assertEquals(String.class, writer.getGenericPropertyType());

        SimpleBean bean = new SimpleBean();
        assertEquals("test", writer.get(bean));
    }

    // Tests method-backed writer properties and value access
    @Test
    public void testGet_methodAccessor_returnsCorrectValue() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(int.class);
        BeanPropertyWriter writer = createMethodWriter("count", "getCount", type);

        assertEquals("count", writer.getName());
        assertEquals(Integer.TYPE, writer.getPropertyType());
        assertEquals(Integer.TYPE, writer.getGenericPropertyType());

        SimpleBean bean = new SimpleBean();
        assertEquals(42, writer.get(bean));
    }

    // Tests renaming of property with NameTransformer
    @Test
    public void testRename_customTransformer_returnsRenamedWriter() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanPropertyWriter writer = createFieldWriter("name", "name", type);

        NameTransformer transformer = new NameTransformer() {
            @Override
            public String transform(String name) {
                return "prefix_" + name;
            }

            @Override
            public String reverse(String transformed) {
                return transformed.substring(7);
            }
        };

        BeanPropertyWriter renamed = writer.rename(transformer);
        assertNotSame(writer, renamed);
        assertEquals("prefix_name", renamed.getName());

        BeanPropertyWriter unchanged = writer.rename(NameTransformer.NOP);
        assertSame(writer, unchanged);
    }

    // Tests copy constructors
    @Test
    public void testCopyConstructors_baseWriter_preservesProperties() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanPropertyWriter base = createFieldWriter("name", "name", type);
        base.setInternalSetting("key1", "val1");

        SubBeanPropertyWriter copy1 = new SubBeanPropertyWriter(base);
        assertEquals(base.getName(), copy1.getName());
        assertEquals("val1", copy1.getInternalSetting("key1"));

        PropertyName newName = new PropertyName("newName");
        SubBeanPropertyWriter copy2 = new SubBeanPropertyWriter(base, newName);
        assertEquals("newName", copy2.getName());
        assertEquals("val1", copy2.getInternalSetting("key1"));

        SerializedString serName = new SerializedString("serName");
        SubBeanPropertyWriter copy3 = new SubBeanPropertyWriter(base, serName);
        assertEquals("serName", copy3.getName());
        assertEquals("val1", copy3.getInternalSetting("key1"));
    }

    // Tests internal settings management (get, set, remove)
    @Test
    public void testInternalSettings_setAndRemove_managesSettingsCorrectly() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanPropertyWriter writer = createFieldWriter("name", "name", type);

        assertNull(writer.getInternalSetting("k1"));
        assertNull(writer.removeInternalSetting("k1"));

        Object prev = writer.setInternalSetting("k1", "v1");
        assertNull(prev);
        assertEquals("v1", writer.getInternalSetting("k1"));

        prev = writer.setInternalSetting("k1", "v2");
        assertEquals("v1", prev);
        assertEquals("v2", writer.getInternalSetting("k1"));

        Object removed = writer.removeInternalSetting("k1");
        assertEquals("v2", removed);
        assertNull(writer.getInternalSetting("k1"));
    }

    // Tests serializer assignment and overriding protection
    @Test
    public void testAssignSerializer_validAndOverride_assignsOrThrows() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanPropertyWriter writer = createFieldWriter("name", "name", type);
        assertFalse(writer.hasSerializer());

        JsonSerializer<Object> ser1 = _mapper.getSerializerProviderInstance().findValueSerializer(String.class, writer);
        writer.assignSerializer(ser1);
        assertTrue(writer.hasSerializer());
        assertSame(ser1, writer.getSerializer());

        // Re-assigning the same instance should succeed
        writer.assignSerializer(ser1);

        // Assigning a different serializer should throw IllegalStateException
        JsonSerializer<Object> ser2 = _mapper.getSerializerProviderInstance().findValueSerializer(Integer.class, writer);
        try {
            writer.assignSerializer(ser2);
            fail("Expected IllegalStateException when overriding serializer");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Can not override serializer"));
        }
    }

    // Tests null serializer assignment and overriding protection
    @Test
    public void testAssignNullSerializer_validAndOverride_assignsOrThrows() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanPropertyWriter writer = createFieldWriter("name", "name", type);
        assertFalse(writer.hasNullSerializer());

        JsonSerializer<Object> nullSer1 = _mapper.getSerializerProviderInstance().getDefaultNullValueSerializer();
        writer.assignNullSerializer(nullSer1);
        assertTrue(writer.hasNullSerializer());

        writer.assignNullSerializer(nullSer1);

        JsonSerializer<Object> nullSer2 = _mapper.getSerializerProviderInstance().findValueSerializer(String.class, writer);
        try {
            writer.assignNullSerializer(nullSer2);
            fail("Expected IllegalStateException when overriding null serializer");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Can not override null serializer"));
        }
    }

    // Tests wouldConflictWithName matching
    @Test
    public void testWouldConflictWithName_matchingAndNonMatching_returnsExpected() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanPropertyWriter writer = createFieldWriter("name", "name", type);

        assertTrue(writer.wouldConflictWithName(new PropertyName("name")));
        assertFalse(writer.wouldConflictWithName(new PropertyName("other")));
        assertFalse(writer.wouldConflictWithName(new PropertyName("name", "http://example.com")));
    }

    // Tests assignTypeSerializer
    @Test
    public void testAssignTypeSerializer_validSerializer_setsTypeSerializer() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanPropertyWriter writer = createFieldWriter("name", "name", type);
        assertNull(writer.getTypeSerializer());

        TypeSerializer typeSer = _mapper.getSerializerProviderInstance().findTypeSerializer(type);
        writer.assignTypeSerializer(typeSer);
        assertEquals(typeSer, writer.getTypeSerializer());
    }

    // Tests unwrapping writer creation
    @Test
    public void testUnwrappingWriter_validTransformer_returnsUnwrappingInstance() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanPropertyWriter writer = createFieldWriter("name", "name", type);

        BeanPropertyWriter unwrapping = writer.unwrappingWriter(NameTransformer.NOP);
        assertNotNull(unwrapping);
        assertTrue(unwrapping.isUnwrapping());
    }

    // Tests serialization as field with null value and null serializer
    @Test
    public void testSerializeAsField_nullValueWithNullSerializer_writesNullField() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanPropertyWriter writer = createFieldWriter("name", "name", type);
        writer.assignNullSerializer(_mapper.getSerializerProviderInstance().getDefaultNullValueSerializer());

        SimpleBean bean = new SimpleBean();
        bean.name = null;

        StringWriter sw = new StringWriter();
        JsonGenerator gen = _mapper.getFactory().createGenerator(sw);
        gen.writeStartObject();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();
        writer.serializeAsField(bean, gen, prov);
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"name\":null}", sw.toString());
    }

    // Tests serialization as element in tabular output
    @Test
    public void testSerializeAsElement_validValue_writesElementWithoutFieldName() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanPropertyWriter writer = createFieldWriter("name", "name", type);
        SimpleBean bean = new SimpleBean();

        StringWriter sw = new StringWriter();
        JsonGenerator gen = _mapper.getFactory().createGenerator(sw);
        gen.writeStartArray();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();
        writer.serializeAsElement(bean, gen, prov);
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"test\"]", sw.toString());
    }

    // Tests placeholder serialization in tabular output
    @Test
    public void testSerializeAsPlaceholder_withoutNullSerializer_writesNull() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanPropertyWriter writer = createFieldWriter("name", "name", type);
        SimpleBean bean = new SimpleBean();

        StringWriter sw = new StringWriter();
        JsonGenerator gen = _mapper.getFactory().createGenerator(sw);
        gen.writeStartArray();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();
        writer.serializeAsPlaceholder(bean, gen, prov);
        gen.writeEndArray();
        gen.close();

        assertEquals("[null]", sw.toString());
    }

    // Tests self-reference cycle detection exception
    @Test(expected = JsonMappingException.class)
    public void testSerializeAsField_selfReference_throwsJsonMappingException() throws Exception {
        Field field = SelfReferencingBean.class.getField("self");
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(SelfReferencingBean.class, _mapper.getDeserializationConfig());
        AnnotatedField am = new AnnotatedField(field, new AnnotationMap());
        PropertyName pName = new PropertyName("self");
        POJOPropertyBuilder propDef = new POJOPropertyBuilder(pName, _mapper.getAnnotationIntrospector(), true);
        JavaType type = TypeFactory.defaultInstance().constructType(SelfReferencingBean.class);
        BeanPropertyWriter writer = new BeanPropertyWriter(propDef, am, ac.getAnnotations(), type, null, null, null, false, null);

        SelfReferencingBean bean = new SelfReferencingBean();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = _mapper.getFactory().createGenerator(sw);
        gen.writeStartObject();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();
        writer.serializeAsField(bean, gen, prov);
    }

    // Tests readResolve deserialization helper
    @Test
    public void testReadResolve_fieldMember_restoresField() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanPropertyWriter writer = createFieldWriter("name", "name", type);
        Object resolved = writer.readResolve();
        assertSame(writer, resolved);
    }

    // Tests toString representation
    @Test
    public void testToString_fieldWriter_containsPropertyNameAndField() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanPropertyWriter writer = createFieldWriter("name", "name", type);
        String desc = writer.toString();
        assertTrue(desc.contains("property 'name'"));
        assertTrue(desc.contains("field \""));
    }

    // Tests findFormatOverrides caching behavior
    @Test
    public void testFindFormatOverrides_nullAndRepeatedLookup_returnsCachedOrNull() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanPropertyWriter writer = createFieldWriter("name", "name", type);

        JsonFormat.Value val1 = writer.findFormatOverrides(null);
        assertNull(val1);

        JsonFormat.Value val2 = writer.findFormatOverrides(_mapper.getAnnotationIntrospector());
        assertNull(val2);
    }
}