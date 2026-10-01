package com.fasterxml.jackson.databind;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.databind.util.ObjectBuffer;

public class DeserializationContextTest {

    private ObjectMapper _mapper;
    private DeserializationContext _context;

    @Before
    public void setUp() throws Exception {
        _mapper = new ObjectMapper();
        JsonParser p = _mapper.getFactory().createParser("{\"prop\":\"val\"}");
        p.nextToken();
        _context = _mapper.getDeserializationContext().createInstance(
                _mapper.getDeserializationConfig(),
                p,
                _mapper.getInjectableValues()
        );
    }

    // Tests feature flag checks
    @Test
    public void testIsEnabled_deserializationFeature_returnsConfiguredValue() {
        assertTrue(_context.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertFalse(_context.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));
        
        int mask = DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES.getMask();
        assertTrue(_context.hasSomeOfFeatures(mask));
        assertTrue(_context.hasDeserializationFeatures(mask));
        assertEquals(_mapper.getDeserializationConfig().getDeserializationFeatures(), _context.getDeserializationFeatures());
    }

    // Tests type construction helper
    @Test
    public void testConstructType_validAndNullClass_returnsCorrectJavaType() {
        assertNull(_context.constructType(null));
        JavaType stringType = _context.constructType(String.class);
        assertNotNull(stringType);
        assertEquals(String.class, stringType.getRawClass());
    }

    // Tests class resolution helper
    @Test
    public void testFindClass_validClassName_returnsClass() throws Exception {
        Class<?> clazz = _context.findClass(String.class.getName());
        assertEquals(String.class, clazz);
    }

    // Tests class resolution exception path
    @Test(expected = ClassNotFoundException.class)
    public void testFindClass_invalidClassName_throwsClassNotFoundException() throws Exception {
        _context.findClass("com.invalid.NonExistingClass123");
    }

    // Tests per-call contextual attributes
    @Test
    public void testAttribute_getAndSet_returnsConfiguredAttribute() {
        assertNull(_context.getAttribute("myKey"));
        _context.setAttribute("myKey", "myValue");
        assertEquals("myValue", _context.getAttribute("myKey"));
    }

    // Tests ObjectBuffer lifecycle and leasing
    @Test
    public void testLeaseAndReturnObjectBuffer_reuse_maintainsBuffer() {
        ObjectBuffer buf1 = _context.leaseObjectBuffer();
        assertNotNull(buf1);
        _context.returnObjectBuffer(buf1);

        ObjectBuffer buf2 = _context.leaseObjectBuffer();
        assertSame(buf1, buf2);
    }

    // Tests ArrayBuilders access
    @Test
    public void testGetArrayBuilders_callTwice_returnsSameInstance() {
        ArrayBuilders builders1 = _context.getArrayBuilders();
        assertNotNull(builders1);
        ArrayBuilders builders2 = _context.getArrayBuilders();
        assertSame(builders1, builders2);
    }

    // Tests date parsing with standard ISO/date formats
    @Test
    public void testParseDate_validDateString_returnsParsedDate() {
        Date date = _context.parseDate("2020-01-01T00:00:00.000+0000");
        assertNotNull(date);
    }

    // Tests date parsing with invalid format
    @Test(expected = IllegalArgumentException.class)
    public void testParseDate_invalidDateString_throwsException() {
        _context.parseDate("not-a-valid-date");
    }

    // Tests Calendar construction
    @Test
    public void testConstructCalendar_validDate_returnsMatchingCalendar() {
        Date d = new Date(100000L);
        Calendar c = _context.constructCalendar(d);
        assertNotNull(c);
        assertEquals(d.getTime(), c.getTimeInMillis());
        assertEquals(_context.getTimeZone(), c.getTimeZone());
    }

    // Tests unknown property handling with FAIL_ON_UNKNOWN_PROPERTIES enabled
    @Test(expected = UnrecognizedPropertyException.class)
    public void testHandleUnknownProperty_unhandled_throwsUnrecognizedPropertyException() throws Exception {
        _context.handleUnknownProperty(_context.getParser(), null, String.class, "unknownField");
    }

    // Tests unknown property handling with custom ProblemHandler
    @Test
    public void testHandleUnknownProperty_customHandler_returnsHandled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p,
                    JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) {
                return true;
            }
        });
        JsonParser p = mapper.getFactory().createParser("{}");
        DeserializationContext ctxt = mapper.getDeserializationContext().createInstance(
                mapper.getDeserializationConfig(), p, null
        );
        boolean handled = ctxt.handleUnknownProperty(p, null, String.class, "prop");
        assertTrue(handled);
    }

    // Tests weird string value error reporting
    @Test(expected = InvalidFormatException.class)
    public void testHandleWeirdStringValue_unhandled_throwsInvalidFormatException() throws Exception {
        _context.handleWeirdStringValue(Integer.class, "abc", "Cannot parse integer");
    }

    // Tests weird number value error reporting
    @Test(expected = InvalidFormatException.class)
    public void testHandleWeirdNumberValue_unhandled_throwsInvalidFormatException() throws Exception {
        _context.handleWeirdNumberValue(Boolean.class, 123, "Cannot convert number to boolean");
    }

    // Tests weird key handling
    @Test(expected = InvalidFormatException.class)
    public void testHandleWeirdKey_unhandled_throwsInvalidFormatException() throws Exception {
        _context.handleWeirdKey(Integer.class, "keyStr", "Invalid integer key");
    }

    // Tests invalid type id handling
    @Test(expected = InvalidTypeIdException.class)
    public void testHandleUnknownTypeId_failOnInvalidSubtype_throwsInvalidTypeIdException() throws Exception {
        JavaType baseType = _context.constructType(Object.class);
        _context.handleUnknownTypeId(baseType, "UnknownTypeId", null, "extra desc");
    }

    // Tests missing type id handling
    @Test(expected = InvalidTypeIdException.class)
    public void testHandleMissingTypeId_throwsInvalidTypeIdException() throws Exception {
        JavaType baseType = _context.constructType(Object.class);
        _context.handleMissingTypeId(baseType, null, "extra desc");
    }

    // Tests bad definition reporting
    @Test(expected = InvalidDefinitionException.class)
    public void testReportBadDefinition_javaType_throwsInvalidDefinitionException() throws Exception {
        JavaType type = _context.constructType(String.class);
        _context.reportBadDefinition(type, "Bad type definition");
    }

    // Tests input mismatch reporting
    @Test(expected = MismatchedInputException.class)
    public void testReportInputMismatch_targetClass_throwsMismatchedInputException() throws Exception {
        _context.reportInputMismatch(String.class, "Expected string input");
    }

    // Tests wrong token exception reporting
    @Test(expected = MismatchedInputException.class)
    public void testReportWrongTokenException_targetClass_throwsMismatchedInputException() throws Exception {
        _context.reportWrongTokenException(String.class, JsonToken.START_ARRAY, "Expected array token");
    }

    // Tests missing instantiator reporting
    @Test(expected = MismatchedInputException.class)
    public void testHandleMissingInstantiator_noCreator_throwsException() throws Exception {
        _context.handleMissingInstantiator(String.class, null, _context.getParser(), "Missing instantiator");
    }

    // Tests checking value deserializer existence
    @Test
    public void testHasValueDeserializerFor_standardType_returnsTrue() {
        JavaType type = _context.constructType(String.class);
        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        boolean hasDeser = _context.hasValueDeserializerFor(type, cause);
        assertTrue(hasDeser);
        assertNull(cause.get());
    }

    // --- New Tests ---

    @Test
    public void testGetConfigAndFactory_returnsNonNull() {
        assertNotNull(_context.getConfig());
        assertNotNull(_context.getFactory());
        assertNotNull(_context.getTypeFactory());
        assertNotNull(_context.getNodeFactory());
        assertNotNull(_context.getBase64Variant());
        assertNotNull(_context.getLocale());
        assertNotNull(_context.getTimeZone());
        assertNotNull(_context.getAnnotationIntrospector());
    }

    @Test
    public void testConstructSpecializedType_validSubclass_returnsSpecialized() {
        JavaType baseType = _context.constructType(Number.class);
        JavaType specialized = _context.constructSpecializedType(baseType, Integer.class);
        assertNotNull(specialized);
        assertEquals(Integer.class, specialized.getRawClass());
    }

    @Test
    public void testFindRootValueDeserializer_stringType_returnsDeserializer() throws Exception {
        JavaType type = _context.constructType(String.class);
        JsonDeserializer<Object> deser = _context.findRootValueDeserializer(type);
        assertNotNull(deser);
    }

    @Test
    public void testFindNonContextualValueDeserializer_stringType_returnsDeserializer() throws Exception {
        JavaType type = _context.constructType(String.class);
        JsonDeserializer<Object> deser = _context.findNonContextualValueDeserializer(type);
        assertNotNull(deser);
    }

    @Test
    public void testFindContextualValueDeserializer_stringType_returnsDeserializer() throws Exception {
        JavaType type = _context.constructType(String.class);
        JsonDeserializer<?> deser = _context.findContextualValueDeserializer(type, null);
        assertNotNull(deser);
    }

    @Test
    public void testFindKeyDeserializer_stringType_returnsKeyDeserializer() throws Exception {
        JavaType type = _context.constructType(String.class);
        KeyDeserializer keyDeser = _context.findKeyDeserializer(type, null);
        assertNotNull(keyDeser);
    }

    @Test(expected = MismatchedInputException.class)
    public void testHandleUnexpectedToken_targetClassAndParser_throwsMismatchedInputException() throws Exception {
        _context.handleUnexpectedToken(Integer.class, _context.getParser());
    }

    @Test(expected = MismatchedInputException.class)
    public void testHandleUnexpectedToken_targetJavaTypeAndParser_throwsMismatchedInputException() throws Exception {
        JavaType type = _context.constructType(Integer.class);
        _context.handleUnexpectedToken(type, _context.getParser());
    }

    @Test(expected = MismatchedInputException.class)
    public void testReportTrailingTokens_targetClass_throwsMismatchedInputException() throws Exception {
        _context.reportTrailingTokens(String.class, _context.getParser(), JsonToken.VALUE_STRING);
    }

    @Test(expected = InvalidFormatException.class)
    public void testHandleWeirdNativeValue_unhandled_throwsInvalidFormatException() throws Exception {
        _context.handleWeirdNativeValue(new Object(), String.class, "Unexpected native value");
    }

    @Test(expected = JsonMappingException.class)
    public void testHandleInstantiationProblem_throwsJsonMappingException() throws Exception {
        _context.handleInstantiationProblem(String.class, "argument", new RuntimeException("Instantiation failed"));
    }

    @Test
    public void testWeirdStringException_createsException() {
        JsonMappingException exc = _context.weirdStringException("testVal", Integer.class, "Cannot parse integer");
        assertNotNull(exc);
        assertTrue(exc instanceof InvalidFormatException);
    }

    @Test
    public void testWeirdNumberException_createsException() {
        JsonMappingException exc = _context.weirdNumberException(123, Boolean.class, "Cannot parse boolean");
        assertNotNull(exc);
        assertTrue(exc instanceof InvalidFormatException);
    }

    @Test
    public void testWeirdKeyException_createsException() {
        JsonMappingException exc = _context.weirdKeyException(Integer.class, "keyStr", "Invalid integer key");
        assertNotNull(exc);
        assertTrue(exc instanceof InvalidFormatException);
    }

    @Test
    public void testInstantiationException_createsException() {
        JsonMappingException exc = _context.instantiationException(String.class, new RuntimeException("cause"));
        assertNotNull(exc);
        assertTrue(exc instanceof ValueInstantiationException);
    }

    @Test
    public void testInvalidTypeIdException_createsException() {
        JavaType baseType = _context.constructType(Object.class);
        InvalidTypeIdException exc = _context.invalidTypeIdException(baseType, "typeId", "extra info");
        assertNotNull(exc);
        assertEquals("typeId", exc.getTypeId());
        assertEquals(baseType, exc.getBaseType());
    }

    @Test(expected = JsonMappingException.class)
    public void testMappingException_string_throwsJsonMappingException() throws Exception {
        throw _context.mappingException("Simple mapping error: %s", "detail");
    }

    @Test
    public void testReadPropertyValue_validClass_readsValue() throws Exception {
        JsonParser p = _mapper.getFactory().createParser("\"hello\"");
        p.nextToken();
        Object val = _context.readPropertyValue(p, null, String.class);
        assertEquals("hello", val);
    }

    @Test
    public void testReadPropertyValue_validJavaType_readsValue() throws Exception {
        JsonParser p = _mapper.getFactory().createParser("42");
        p.nextToken();
        JavaType type = _context.constructType(Integer.class);
        Object val = _context.readPropertyValue(p, null, type);
        assertEquals(42, val);
    }

    @Test
    public void testReadRootValue_validInput_returnsValue() throws Exception {
        JsonParser p = _mapper.getFactory().createParser("\"rootVal\"");
        p.nextToken();
        JavaType type = _context.constructType(String.class);
        Object val = _context.readRootValue(p, type, null, null);
        assertEquals("rootVal", val);
    }
}