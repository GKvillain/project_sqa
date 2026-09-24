package com.fasterxml.jackson.databind;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;

import static org.junit.Assert.*;

public class DatabindContextTest {

    private ObjectMapper _mapper;
    private TestDatabindContext _context;

    static class TestDatabindContext extends DatabindContext {
        private final MapperConfig<?> _config;
        private final TypeFactory _typeFactory;
        private HandlerInstantiator _handlerInstantiator;

        public TestDatabindContext(MapperConfig<?> config, TypeFactory tf) {
            _config = config;
            _typeFactory = tf != null ? tf : TypeFactory.defaultInstance();
        }

        public void setHandlerInstantiator(HandlerInstantiator hi) {
            _handlerInstantiator = hi;
        }

        @Override
        public MapperConfig<?> getConfig() {
            return _config;
        }

        @Override
        public AnnotationIntrospector getAnnotationIntrospector() {
            return _config.getAnnotationIntrospector();
        }

        @Override
        public boolean isEnabled(MapperFeature feature) {
            return _config.isEnabled(feature);
        }

        @Override
        public boolean canOverrideAccessModifiers() {
            return _config.canOverrideAccessModifiers();
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
        protected JsonMappingException invalidTypeIdException(JavaType baseType, String typeId, String extraDesc) {
            return InvalidTypeIdException.from(null, extraDesc, baseType, typeId);
        }

        @Override
        public TypeFactory getTypeFactory() {
            return _typeFactory;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T reportBadDefinition(JavaType type, String msg) throws JsonMappingException {
            throw new JsonMappingException(null, msg);
        }
    }

    public static class StringToIntegerConverter extends StdConverter<String, Integer> {
        @Override
        public Integer convert(String value) {
            return Integer.parseInt(value);
        }
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _context = new TestDatabindContext(_mapper.getDeserializationConfig(), _mapper.getTypeFactory());
    }

    // Tests constructType with null input
    @Test
    public void testConstructType_nullType_returnsNull() {
        assertNull(_context.constructType(null));
    }

    // Tests constructType with valid Class type
    @Test
    public void testConstructType_validType_returnsConstructedJavaType() {
        JavaType javaType = _context.constructType(String.class);
        assertNotNull(javaType);
        assertEquals(String.class, javaType.getRawClass());
    }

    // Tests constructSpecializedType when subclass matches raw class
    @Test
    public void testConstructSpecializedType_sameClass_returnsSameInstance() {
        JavaType baseType = _context.constructType(Number.class);
        JavaType specializedType = _context.constructSpecializedType(baseType, Number.class);
        assertSame(baseType, specializedType);
    }

    // Tests constructSpecializedType when subclass is a subtype
    @Test
    public void testConstructSpecializedType_subclass_returnsSpecializedType() {
        JavaType baseType = _context.constructType(Number.class);
        JavaType specializedType = _context.constructSpecializedType(baseType, Integer.class);
        assertNotNull(specializedType);
        assertEquals(Integer.class, specializedType.getRawClass());
    }

    // Tests resolveSubType with generic canonical name subtype
    @Test
    public void testResolveSubType_genericCanonicalSubtype_returnsResolvedType() throws Exception {
        JavaType baseType = _context.constructType(List.class);
        JavaType resolvedType = _context.resolveSubType(baseType, "java.util.ArrayList<java.lang.String>");
        assertNotNull(resolvedType);
        assertEquals(ArrayList.class, resolvedType.getRawClass());
        assertEquals(String.class, resolvedType.getContentType().getRawClass());
    }

    // Tests resolveSubType with generic canonical name not matching base type
    @Test(expected = JsonMappingException.class)
    public void testResolveSubType_genericCanonicalNotSubtype_throwsException() throws Exception {
        JavaType baseType = _context.constructType(List.class);
        _context.resolveSubType(baseType, "java.util.HashMap<java.lang.String,java.lang.String>");
    }

    // Tests resolveSubType with simple class name subtype
    @Test
    public void testResolveSubType_validClassSubtype_returnsResolvedType() throws Exception {
        JavaType baseType = _context.constructType(Number.class);
        JavaType resolvedType = _context.resolveSubType(baseType, "java.lang.Long");
        assertNotNull(resolvedType);
        assertEquals(Long.class, resolvedType.getRawClass());
    }

    // Tests resolveSubType when class is not a subtype of base type
    @Test(expected = JsonMappingException.class)
    public void testResolveSubType_classNotSubtype_throwsException() throws Exception {
        JavaType baseType = _context.constructType(Number.class);
        _context.resolveSubType(baseType, "java.lang.String");
    }

    // Tests resolveSubType when class name does not exist
    @Test
    public void testResolveSubType_unknownClassName_returnsNull() throws Exception {
        JavaType baseType = _context.constructType(Object.class);
        JavaType resolvedType = _context.resolveSubType(baseType, "com.invalid.NonExistingClass12345");
        assertNull(resolvedType);
    }

    // Tests objectIdGeneratorInstance with default generator
    @Test
    public void testObjectIdGeneratorInstance_validObjectIdInfo_returnsGenerator() throws Exception {
        ObjectIdInfo info = new ObjectIdInfo(
                PropertyName.construct("id"),
                Object.class,
                ObjectIdGenerators.IntSequenceGenerator.class,
                SimpleObjectIdResolver.class
        );
        ObjectIdGenerator<?> generator = _context.objectIdGeneratorInstance(null, info);
        assertNotNull(generator);
        assertEquals(ObjectIdGenerators.IntSequenceGenerator.class, generator.getClass());
        assertEquals(Object.class, generator.getScope());
    }

    // Tests objectIdResolverInstance with default resolver
    @Test
    public void testObjectIdResolverInstance_validObjectIdInfo_returnsResolver() {
        ObjectIdInfo info = new ObjectIdInfo(
                PropertyName.construct("id"),
                Object.class,
                ObjectIdGenerators.IntSequenceGenerator.class,
                SimpleObjectIdResolver.class
        );
        ObjectIdResolver resolver = _context.objectIdResolverInstance(null, info);
        assertNotNull(resolver);
        assertEquals(SimpleObjectIdResolver.class, resolver.getClass());
    }

    // Tests converterInstance with null definition
    @Test
    public void testConverterInstance_nullDefinition_returnsNull() throws Exception {
        assertNull(_context.converterInstance(null, null));
    }

    // Tests converterInstance with already instantiated Converter
    @Test
    public void testConverterInstance_converterInstance_returnsSameInstance() throws Exception {
        Converter<?, ?> existingConverter = new StringToIntegerConverter();
        Converter<?, ?> result = _context.converterInstance(null, existingConverter);
        assertSame(existingConverter, result);
    }

    // Tests converterInstance with Converter.None marker class
    @Test
    public void testConverterInstance_converterNoneClass_returnsNull() throws Exception {
        assertNull(_context.converterInstance(null, Converter.None.class));
    }

    // Tests converterInstance with Class implementing Converter
    @Test
    public void testConverterInstance_validConverterClass_instantiatesConverter() throws Exception {
        Converter<?, ?> result = _context.converterInstance(null, StringToIntegerConverter.class);
        assertNotNull(result);
        assertEquals(StringToIntegerConverter.class, result.getClass());
    }

    // Tests converterInstance with invalid non-class non-converter object
    @Test(expected = IllegalStateException.class)
    public void testConverterInstance_invalidObjectType_throwsIllegalStateException() throws Exception {
        _context.converterInstance(null, "NotAConverterObject");
    }

    // Tests converterInstance with class not implementing Converter
    @Test(expected = IllegalStateException.class)
    public void testConverterInstance_nonConverterClass_throwsIllegalStateException() throws Exception {
        _context.converterInstance(null, String.class);
    }

    // Tests reportBadDefinition with Class delegating to JavaType method
    @Test(expected = JsonMappingException.class)
    public void testReportBadDefinition_classAndMessage_throwsJsonMappingException() throws Exception {
        _context.reportBadDefinition(String.class, "Bad definition for String");
    }

    // Tests helper string formatting methods
    @Test
    public void testHelperFormatting_variousInputs_formatsCorrectly() {
        assertEquals("Hello World", _context._format("Hello %s", "World"));
        assertEquals("Plain text", _context._format("Plain text"));

        assertEquals("", _context._truncate(null));
        assertEquals("short text", _context._truncate("short text"));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 600; i++) {
            sb.append('A');
        }
        String longStr = sb.toString();
        String truncated = _context._truncate(longStr);
        assertEquals(1005, truncated.length());
        assertTrue(truncated.contains("]...["));

        assertEquals("[N/A]", _context._quotedString(null));
        assertEquals("\"quoted\"", _context._quotedString("quoted"));

        assertEquals("base", _context._colonConcat("base", null));
        assertEquals("base: extra", _context._colonConcat("base", "extra"));

        assertEquals("[N/A]", _context._desc(null));
        assertEquals("description", _context._desc("description"));
    }
}