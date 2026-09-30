package com.fasterxml.jackson.databind;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.databind.annotation.NoClass;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;

public class DatabindContextTest {

    private TestContext context;
    private ObjectMapper mapper;

    static class TestContext extends DatabindContext {
        private final MapperConfig<?> _config;
        private final TypeFactory _typeFactory;

        public TestContext(MapperConfig<?> config, TypeFactory tf) {
            _config = config;
            _typeFactory = (tf == null) ? TypeFactory.defaultInstance() : tf;
        }

        @Override
        public MapperConfig<?> getConfig() {
            return _config;
        }

        @Override
        public AnnotationIntrospector getAnnotationIntrospector() {
            return (_config == null) ? null : _config.getAnnotationIntrospector();
        }

        @Override
        public boolean isEnabled(MapperFeature feature) {
            return _config != null && _config.isEnabled(feature);
        }

        @Override
        public boolean canOverrideAccessModifiers() {
            return _config != null && _config.canOverrideAccessModifiers();
        }

        @Override
        public Class<?> getActiveView() {
            return null;
        }

        @Override
        public Locale getLocale() {
            return Locale.getDefault();
        }

        @Override
        public TimeZone getTimeZone() {
            return TimeZone.getDefault();
        }

        @Override
        public JsonFormat.Value getDefaultPropertyFormat(Class<?> baseType) {
            return JsonFormat.Value.empty();
        }

        @Override
        public Object getAttribute(Object key) {
            return null;
        }

        @Override
        public DatabindContext setAttribute(Object key, Object value) {
            return this;
        }

        @Override
        public TypeFactory getTypeFactory() {
            return _typeFactory;
        }

        @Override
        protected JsonMappingException invalidTypeIdException(JavaType baseType, String typeId, String extraDesc) {
            return JsonMappingException.from((DeserializationContext) null, "Invalid type id '" + typeId + "': " + extraDesc);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T reportBadDefinition(JavaType type, String msg) throws JsonMappingException {
            throw JsonMappingException.from((DeserializationContext) null, "Bad definition for " + type + ": " + msg);
        }
    }

    public static class SimpleStringConverter extends StdConverter<String, Integer> {
        @Override
        public Integer convert(String value) {
            return Integer.parseInt(value);
        }
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        context = new TestContext(mapper.getDeserializationConfig(), mapper.getTypeFactory());
    }

    // Tests constructType with null input
    @Test
    public void testConstructType_nullInput_returnsNull() {
        assertNull(context.constructType((Type) null));
    }

    // Tests constructType with valid Class input
    @Test
    public void testConstructType_validClass_returnsJavaType() {
        JavaType type = context.constructType(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }

    // Tests constructSpecializedType when subclass equals base raw class
    @Test
    public void testConstructSpecializedType_sameClass_returnsSameBaseType() {
        JavaType baseType = context.constructType(List.class);
        JavaType specialized = context.constructSpecializedType(baseType, List.class);
        assertSame(baseType, specialized);
    }

    // Tests constructSpecializedType with actual subclass
    @Test
    public void testConstructSpecializedType_subclass_returnsSpecializedType() {
        JavaType baseType = context.constructType(List.class);
        JavaType specialized = context.constructSpecializedType(baseType, ArrayList.class);
        assertNotNull(specialized);
        assertEquals(ArrayList.class, specialized.getRawClass());
    }

    // Tests resolveSubType with generic canonical name subtype
    @Test
    public void testResolveSubType_validGenericSubtype_returnsResolvedType() throws Exception {
        JavaType baseType = context.constructType(List.class);
        JavaType resolved = context.resolveSubType(baseType, "java.util.ArrayList<java.lang.String>");
        assertNotNull(resolved);
        assertEquals(ArrayList.class, resolved.getRawClass());
    }

    // Tests resolveSubType with generic canonical name not matching base type
    @Test(expected = JsonMappingException.class)
    public void testResolveSubType_incompatibleGenericSubtype_throwsException() throws Exception {
        JavaType baseType = context.constructType(String.class);
        context.resolveSubType(baseType, "java.util.ArrayList<java.lang.String>");
    }

    // Tests resolveSubType with valid simple class name
    @Test
    public void testResolveSubType_validSimpleSubtype_returnsResolvedType() throws Exception {
        JavaType baseType = context.constructType(List.class);
        JavaType resolved = context.resolveSubType(baseType, "java.util.ArrayList");
        assertNotNull(resolved);
        assertEquals(ArrayList.class, resolved.getRawClass());
    }

    // Tests resolveSubType with unknown class name returning null
    @Test
    public void testResolveSubType_unknownClass_returnsNull() throws Exception {
        JavaType baseType = context.constructType(List.class);
        JavaType resolved = context.resolveSubType(baseType, "com.nonexisting.UnknownClass");
        assertNull(resolved);
    }

    // Tests resolveSubType with class that is not a subtype
    @Test(expected = JsonMappingException.class)
    public void testResolveSubType_incompatibleSimpleClass_throwsException() throws Exception {
        JavaType baseType = context.constructType(List.class);
        context.resolveSubType(baseType, "java.lang.String");
    }

    // Tests converterInstance with null definition
    @Test
    public void testConverterInstance_nullDef_returnsNull() throws Exception {
        assertNull(context.converterInstance(null, null));
    }

    // Tests converterInstance with existing Converter instance
    @Test
    public void testConverterInstance_instanceDef_returnsSameInstance() throws Exception {
        Converter<Object, Object> conv = new SimpleStringConverter();
        Converter<Object, Object> result = context.converterInstance(null, conv);
        assertSame(conv, result);
    }

    // Tests converterInstance with None marker class
    @Test
    public void testConverterInstance_noneMarker_returnsNull() throws Exception {
        assertNull(context.converterInstance(null, Converter.None.class));
    }

    // Tests converterInstance with valid Converter class
    @Test
    public void testConverterInstance_validConverterClass_createsInstance() throws Exception {
        Converter<Object, Object> result = context.converterInstance(null, SimpleStringConverter.class);
        assertNotNull(result);
        assertTrue(result instanceof SimpleStringConverter);
    }

    // Tests converterInstance with invalid non-class non-converter object
    @Test(expected = IllegalStateException.class)
    public void testConverterInstance_invalidTypeDef_throwsException() throws Exception {
        context.converterInstance(null, "invalidObject");
    }

    // Tests converterInstance with non-Converter class
    @Test(expected = IllegalStateException.class)
    public void testConverterInstance_nonConverterClass_throwsException() throws Exception {
        context.converterInstance(null, String.class);
    }

    // Tests objectIdGeneratorInstance resolution
    @Test
    public void testObjectIdGeneratorInstance_validInfo_returnsGenerator() throws Exception {
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class,
                ObjectIdGenerators.StringIdGenerator.class, SimpleObjectIdResolver.class);
        ObjectIdGenerator<?> gen = context.objectIdGeneratorInstance(null, info);
        assertNotNull(gen);
        assertTrue(gen instanceof ObjectIdGenerators.StringIdGenerator);
    }

    // Tests objectIdResolverInstance resolution
    @Test
    public void testObjectIdResolverInstance_validInfo_returnsResolver() {
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class,
                ObjectIdGenerators.StringIdGenerator.class, SimpleObjectIdResolver.class);
        ObjectIdResolver resolver = context.objectIdResolverInstance(null, info);
        assertNotNull(resolver);
        assertTrue(resolver instanceof SimpleObjectIdResolver);
    }

    // Tests _truncate formatting helper
    @Test
    public void testTruncate_variousLengths_handlesCorrectly() {
        assertEquals("", context._truncate(null));
        assertEquals("short string", context._truncate("short string"));

        StringBuilder longStr = new StringBuilder();
        for (int i = 0; i < 600; i++) {
            longStr.append("a");
        }
        String truncated = context._truncate(longStr.toString());
        assertTrue(truncated.contains("]...["));
        assertEquals(1005, truncated.length());
    }

    // Tests _quotedString helper
    @Test
    public void testQuotedString_nullAndNonNull_returnsFormatted() {
        assertEquals("[N/A]", context._quotedString(null));
        assertEquals("\"test\"", context._quotedString("test"));
    }

    // Tests _desc helper
    @Test
    public void testDesc_nullAndNonNull_returnsFormatted() {
        assertEquals("[N/A]", context._desc(null));
        assertEquals("description", context._desc("description"));
    }

    // Tests _colonConcat helper
    @Test
    public void testColonConcat_nullAndNonNullExtra_returnsFormatted() {
        assertEquals("base", context._colonConcat("base", null));
        assertEquals("base: extra", context._colonConcat("base", "extra"));
    }

    // Tests _format helper
    @Test
    public void testFormat_withAndWithoutArgs_returnsFormatted() {
        assertEquals("message", context._format("message"));
        assertEquals("msg with 42 and text", context._format("msg with %d and %s", 42, "text"));
    }

    // Tests reportBadDefinition with Class overload
    @Test(expected = JsonMappingException.class)
    public void testReportBadDefinition_classInput_throwsJsonMappingException() throws Exception {
        context.reportBadDefinition(String.class, "Type problem");
    }

    // New tests covering additional DatabindContext branches and methods

    @Test
    public void testConverterInstance_noClassMarker_returnsNull() throws Exception {
        assertNull(context.converterInstance(null, NoClass.class));
    }

    @Test
    public void testObjectIdGeneratorInstance_nullInfo_returnsNull() throws Exception {
        assertNull(context.objectIdGeneratorInstance(null, null));
    }

    @Test
    public void testObjectIdResolverInstance_nullInfo_returnsNull() {
        assertNull(context.objectIdResolverInstance(null, null));
    }

    @Test
    public void testConverterInstance_withHandlerInstantiator() throws Exception {
        final Converter<?, ?> customConverter = new SimpleStringConverter();
        HandlerInstantiator hi = new HandlerInstantiator() {
            @Override
            public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> deserClass) { return null; }
            @Override
            public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> keyDeserClass) { return null; }
            @Override
            public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) { return null; }
            @Override
            public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated, Class<?> builderClass) { return null; }
            @Override
            public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) { return null; }
            @Override
            public ValueInstantiator valueInstantiatorInstance(MapperConfig<?> config, Annotated annotated, Class<?> instantiatorClass) { return null; }
            @Override
            public Converter<?, ?> converterInstance(MapperConfig<?> config, Annotated annotated, Class<?> implClass) {
                return customConverter;
            }
        };

        ObjectMapper customMapper = new ObjectMapper().setHandlerInstantiator(hi);
        TestContext customContext = new TestContext(customMapper.getDeserializationConfig(), customMapper.getTypeFactory());
        Converter<Object, Object> result = customContext.converterInstance(null, SimpleStringConverter.class);
        assertSame(customConverter, result);
    }

    @Test
    public void testObjectIdGeneratorInstance_withHandlerInstantiator() throws Exception {
        final ObjectIdGenerator<?> customGen = new ObjectIdGenerators.IntSequenceGenerator();
        HandlerInstantiator hi = new HandlerInstantiator() {
            @Override
            public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> deserClass) { return null; }
            @Override
            public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> keyDeserClass) { return null; }
            @Override
            public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) { return null; }
            @Override
            public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated, Class<?> builderClass) { return null; }
            @Override
            public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) { return null; }
            @Override
            public ValueInstantiator valueInstantiatorInstance(MapperConfig<?> config, Annotated annotated, Class<?> instantiatorClass) { return null; }
            @Override
            public ObjectIdGenerator<?> objectIdGeneratorInstance(MapperConfig<?> config, Annotated annotated, Class<?> implClass) {
                return customGen;
            }
        };

        ObjectMapper customMapper = new ObjectMapper().setHandlerInstantiator(hi);
        TestContext customContext = new TestContext(customMapper.getDeserializationConfig(), customMapper.getTypeFactory());
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class,
                ObjectIdGenerators.StringIdGenerator.class, SimpleObjectIdResolver.class);
        ObjectIdGenerator<?> gen = customContext.objectIdGeneratorInstance(null, info);
        assertSame(customGen, gen);
    }

    @Test
    public void testObjectIdResolverInstance_withHandlerInstantiator() {
        final ObjectIdResolver customResolver = new SimpleObjectIdResolver();
        HandlerInstantiator hi = new HandlerInstantiator() {
            @Override
            public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> deserClass) { return null; }
            @Override
            public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> keyDeserClass) { return null; }
            @Override
            public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) { return null; }
            @Override
            public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated, Class<?> builderClass) { return null; }
            @Override
            public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) { return null; }
            @Override
            public ValueInstantiator valueInstantiatorInstance(MapperConfig<?> config, Annotated annotated, Class<?> instantiatorClass) { return null; }
            @Override
            public ObjectIdResolver resolverIdGeneratorInstance(MapperConfig<?> config, Annotated annotated, Class<?> implClass) {
                return customResolver;
            }
        };

        ObjectMapper customMapper = new ObjectMapper().setHandlerInstantiator(hi);
        TestContext customContext = new TestContext(customMapper.getDeserializationConfig(), customMapper.getTypeFactory());
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class,
                ObjectIdGenerators.StringIdGenerator.class, SimpleObjectIdResolver.class);
        ObjectIdResolver resolver = customContext.objectIdResolverInstance(null, info);
        assertSame(customResolver, resolver);
    }

    @Test(expected = JsonMappingException.class)
    public void testResolveSubType_malformedCanonicalGeneric_throwsException() throws Exception {
        JavaType baseType = context.constructType(List.class);
        context.resolveSubType(baseType, "java.util.ArrayList<[invalid syntax");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedType_incompatibleSubclass_throwsException() {
        JavaType baseType = context.constructType(String.class);
        context.constructSpecializedType(baseType, Integer.class);
    }
}