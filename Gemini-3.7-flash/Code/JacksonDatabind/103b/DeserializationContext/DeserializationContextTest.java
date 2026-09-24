package com.fasterxml.jackson.databind;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ObjectBuffer;

public class DeserializationContextTest
{
    private ObjectMapper _mapper;
    private DeserializationContext _context;
    private JsonParser _parser;

    @Before
    public void setUp() throws Exception {
        _mapper = new ObjectMapper();
        _parser = _mapper.getFactory().createParser("{\"key\":\"value\"}");
        DefaultDeserializationContext src = (DefaultDeserializationContext) _mapper.getDeserializationContext();
        _context = src.createInstance(_mapper.getDeserializationConfig(), _parser, null);
    }

    // Tests constructType with non-null class returns JavaType
    @Test
    public void testConstructType_validClass_returnsJavaType() {
        JavaType type = _context.constructType(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }

    // Tests constructType with null input returns null
    @Test
    public void testConstructType_nullInput_returnsNull() {
        assertNull(_context.constructType(null));
    }

    // Tests findClass with valid class name
    @Test
    public void testFindClass_validClassName_returnsClass() throws Exception {
        Class<?> cls = _context.findClass("java.lang.Integer");
        assertEquals(Integer.class, cls);
    }

    // Tests findClass with invalid class name throws ClassNotFoundException
    @Test(expected = ClassNotFoundException.class)
    public void testFindClass_invalidClassName_throwsClassNotFoundException() throws Exception {
        _context.findClass("com.nonexistent.NoSuchClass");
    }

    // Tests leasing and returning ObjectBuffer for recycling
    @Test
    public void testLeaseAndReturnObjectBuffer_reuseBuffer_succeeds() {
        ObjectBuffer buf = _context.leaseObjectBuffer();
        assertNotNull(buf);
        _context.returnObjectBuffer(buf);
        ObjectBuffer buf2 = _context.leaseObjectBuffer();
        assertNotNull(buf2);
    }

    // Tests lazy creation of ArrayBuilders
    @Test
    public void testGetArrayBuilders_initialCall_returnsNonNullInstance() {
        assertNotNull(_context.getArrayBuilders());
    }

    // Tests per-call attributes get and set
    @Test
    public void testAttributes_setAndGet_returnsSetValue() {
        _context.setAttribute("attrKey", "attrVal");
        assertEquals("attrVal", _context.getAttribute("attrKey"));
        assertNull(_context.getAttribute("nonExistingKey"));
    }

    // Tests parsing valid date string with default configuration
    @Test
    public void testParseDate_validDateString_returnsDate() {
        Date date = _context.parseDate("2020-01-01T00:00:00.000+0000");
        assertNotNull(date);
    }

    // Tests parsing invalid date string throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseDate_invalidDateString_throwsIllegalArgumentException() {
        _context.parseDate("invalid-date-format");
    }

    // Tests constructing Calendar from Date
    @Test
    public void testConstructCalendar_validDate_returnsCalendarWithMatchingTime() {
        Date d = new Date(123456789L);
        Calendar cal = _context.constructCalendar(d);
        assertNotNull(cal);
        assertEquals(d.getTime(), cal.getTimeInMillis());
    }

    // Tests unhandled weird String value throwing InvalidFormatException
    @Test(expected = InvalidFormatException.class)
    public void testHandleWeirdStringValue_unhandled_throwsInvalidFormatException() throws Exception {
        _context.handleWeirdStringValue(Integer.class, "abc", "Cannot parse integer");
    }

    // Tests unhandled weird Map key throwing InvalidFormatException
    @Test(expected = InvalidFormatException.class)
    public void testHandleWeirdKey_unhandled_throwsInvalidFormatException() throws Exception {
        _context.handleWeirdKey(Integer.class, "notAnInt", "Invalid integer key");
    }

    // Tests unhandled weird number value throwing InvalidFormatException
    @Test(expected = InvalidFormatException.class)
    public void testHandleWeirdNumberValue_unhandled_throwsInvalidFormatException() throws Exception {
        _context.handleWeirdNumberValue(String.class, 123.45, "Invalid string number");
    }

    // Tests handleInstantiationProblem propagates IOException directly
    @Test(expected = IOException.class)
    public void testHandleInstantiationProblem_withIOException_rethrowsIOException() throws Exception {
        _context.handleInstantiationProblem(String.class, "arg", new IOException("underlying IO problem"));
    }

    // Tests handleInstantiationProblem wraps generic Throwable in InvalidDefinitionException
    @Test(expected = InvalidDefinitionException.class)
    public void testHandleInstantiationProblem_withRuntimeException_throwsInvalidDefinitionException() throws Exception {
        _context.handleInstantiationProblem(String.class, "arg", new RuntimeException("runtime problem"));
    }

    // Tests handleUnexpectedToken without problem handlers throws MismatchedInputException
    @Test(expected = MismatchedInputException.class)
    public void testHandleUnexpectedToken_unhandled_throwsMismatchedInputException() throws Exception {
        _parser.nextToken(); // START_OBJECT
        _context.handleUnexpectedToken(String.class, _parser);
    }

    // Tests findInjectableValue with no InjectableValues configured throws InvalidDefinitionException
    @Test(expected = InvalidDefinitionException.class)
    public void testFindInjectableValue_nullInjectableValues_throwsInvalidDefinitionException() throws Exception {
        _context.findInjectableValue("valId", null, null);
    }

    // Tests feature flag accessors and checks
    @Test
    public void testFeatureFlags_checkDefaults_returnsExpectedFlags() {
        assertTrue(_context.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        int mask = DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES.getMask();
        assertTrue(_context.hasDeserializationFeatures(mask));
        assertTrue(_context.hasSomeOfFeatures(mask));
        assertTrue(_context.getDeserializationFeatures() != 0);
    }

    // Tests handleUnknownProperty when FAIL_ON_UNKNOWN_PROPERTIES is disabled
    @Test
    public void testHandleUnknownProperty_failOnUnknownDisabled_skipsAndReturnsTrue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        JsonParser p = mapper.getFactory().createParser("{\"dummy\": 123}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        p.nextToken(); // VALUE_NUMBER_INT
        DefaultDeserializationContext src = (DefaultDeserializationContext) mapper.getDeserializationContext();
        DeserializationContext ctxt = src.createInstance(mapper.getDeserializationConfig(), p, null);

        boolean handled = ctxt.handleUnknownProperty(p, null, Object.class, "dummy");
        assertTrue(handled);
    }

    // Tests handleUnknownProperty when FAIL_ON_UNKNOWN_PROPERTIES is enabled throws UnrecognizedPropertyException
    @Test(expected = UnrecognizedPropertyException.class)
    public void testHandleUnknownProperty_failOnUnknownEnabled_throwsUnrecognizedPropertyException() throws Exception {
        _parser.nextToken(); // START_OBJECT
        _parser.nextToken(); // FIELD_NAME
        _context.handleUnknownProperty(_parser, null, Object.class, "key");
    }

    // Tests reportBadMerge when IGNORE_MERGE_FOR_UNMERGEABLE is enabled returns null
    @Test
    public void testReportBadMerge_ignoreMergeEnabled_returnsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(MapperFeature.IGNORE_MERGE_FOR_UNMERGEABLE);
        DefaultDeserializationContext src = (DefaultDeserializationContext) mapper.getDeserializationContext();
        DeserializationContext ctxt = src.createInstance(mapper.getDeserializationConfig(), _parser, null);

        JsonDeserializer<?> deser = ctxt.findRootValueDeserializer(ctxt.constructType(String.class));
        assertNull(ctxt.reportBadMerge(deser));
    }

    // Tests reportBadMerge when IGNORE_MERGE_FOR_UNMERGEABLE is disabled throws InvalidDefinitionException
    @Test(expected = InvalidDefinitionException.class)
    public void testReportBadMerge_ignoreMergeDisabled_throwsInvalidDefinitionException() throws Exception {
        JsonDeserializer<?> deser = _context.findRootValueDeserializer(_context.constructType(String.class));
        _context.reportBadMerge(deser);
    }

    // Tests instantiation with null DeserializerFactory throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullFactory_throwsIllegalArgumentException() {
        new DefaultDeserializationContext.Impl((BeanDeserializerFactory) null);
    }

    // Tests accessors for basic helpers and configuration
    @Test
    public void testBasicAccessors_returnsConfiguredInstances() {
        assertSame(_parser, _context.getParser());
        assertNotNull(_context.getConfig());
        assertNotNull(_context.getTypeFactory());
        assertNotNull(_context.getNodeFactory());
        assertEquals(JsonNodeFactory.instance, _context.getNodeFactory());
        assertNotNull(_context.getAnnotationIntrospector());
        assertEquals(Base64Variants.getDefaultVariant(), _context.getBase64Variant());
        assertNotNull(_context.getLocale());
        assertNotNull(_context.getTimeZone());
    }

    // Tests findInjectableValue when InjectableValues is configured
    @Test
    public void testFindInjectableValue_configuredInjectableValues_returnsInjectedValue() throws Exception {
        InjectableValues.Std injectables = new InjectableValues.Std();
        injectables.addValue("testId", "injectedResult");
        DefaultDeserializationContext src = (DefaultDeserializationContext) _mapper.getDeserializationContext();
        DeserializationContext ctxt = src.createInstance(_mapper.getDeserializationConfig(), _parser, injectables);

        Object val = ctxt.findInjectableValue("testId", null, null);
        assertEquals("injectedResult", val);
    }

    // Tests hasValueDeserializerFor with supported and cached type
    @Test
    public void testHasValueDeserializerFor_validType_returnsTrue() {
        JavaType strType = _context.constructType(String.class);
        AtomicReference<Throwable> cause = new AtomicReference<>();
        assertTrue(_context.hasValueDeserializerFor(strType, cause));
        assertNull(cause.get());
        assertTrue(_context.hasValueDeserializerFor(strType));
    }

    // Tests findKeyDeserializer for standard key type
    @Test
    public void testFindKeyDeserializer_validType_returnsKeyDeserializer() throws Exception {
        JavaType strType = _context.constructType(String.class);
        KeyDeserializer kd = _context.findKeyDeserializer(strType, null);
        assertNotNull(kd);
    }

    // Tests findNonContextualValueDeserializer returns root deserializer
    @Test
    public void testFindNonContextualValueDeserializer_validType_returnsDeserializer() throws Exception {
        JavaType intType = _context.constructType(Integer.class);
        JsonDeserializer<Object> deser = _context.findNonContextualValueDeserializer(intType);
        assertNotNull(deser);
    }

    // Tests reportBadDefinition with JavaType throws InvalidDefinitionException
    @Test(expected = InvalidDefinitionException.class)
    public void testReportBadDefinition_withJavaType_throwsInvalidDefinitionException() throws Exception {
        _context.reportBadDefinition(_context.constructType(String.class), "bad definition message");
    }

    // Tests reportBadDefinition with Class throws InvalidDefinitionException
    @Test(expected = InvalidDefinitionException.class)
    public void testReportBadDefinition_withClass_throwsInvalidDefinitionException() throws Exception {
        _context.reportBadDefinition(String.class, "bad definition message");
    }

    // Tests reportInputMismatch with Class throws MismatchedInputException
    @Test(expected = MismatchedInputException.class)
    public void testReportInputMismatch_withClass_throwsMismatchedInputException() throws Exception {
        _context.reportInputMismatch(String.class, "mismatched input %s", "arg");
    }

    // Tests reportInputMismatch with JavaType throws MismatchedInputException
    @Test(expected = MismatchedInputException.class)
    public void testReportInputMismatch_withJavaType_throwsMismatchedInputException() throws Exception {
        _context.reportInputMismatch(_context.constructType(String.class), "mismatched input message");
    }

    // Tests reportTrailingTokens throws MismatchedInputException
    @Test(expected = MismatchedInputException.class)
    public void testReportTrailingTokens_throwsMismatchedInputException() throws Exception {
        _context.reportTrailingTokens(String.class, _parser, JsonToken.END_OBJECT);
    }

    // Tests handleMissingInstantiator unhandled throws MismatchedInputException
    @Test(expected = MismatchedInputException.class)
    public void testHandleMissingInstantiator_unhandled_throwsMismatchedInputException() throws Exception {
        _context.handleMissingInstantiator(String.class, (ValueInstantiator) null, _parser, "missing instantiator");
    }

    // Tests handleWeirdNativeValue unhandled throws InvalidFormatException
    @Test(expected = InvalidFormatException.class)
    public void testHandleWeirdNativeValue_unhandled_throwsInvalidFormatException() throws Exception {
        _context.handleWeirdNativeValue(Integer.class, Boolean.TRUE, _parser);
    }

    // Tests handleUnknownTypeId unhandled throws InvalidTypeIdException
    @Test(expected = InvalidTypeIdException.class)
    public void testHandleUnknownTypeId_unhandled_throwsInvalidTypeIdException() throws Exception {
        _context.handleUnknownTypeId(_context.constructType(Object.class), "UnknownType123", null, "no type id");
    }

    // Tests handleMissingTypeId unhandled throws InvalidTypeIdException
    @Test(expected = InvalidTypeIdException.class)
    public void testHandleMissingTypeId_unhandled_throwsInvalidTypeIdException() throws Exception {
        _context.handleMissingTypeId(_context.constructType(Object.class), null, "missing type id");
    }

    // Tests readRootValue from DefaultDeserializationContext
    @Test
    public void testReadRootValue_validJson_readsCorrectObject() throws Exception {
        JsonParser p = _mapper.getFactory().createParser("\"hello world\"");
        DefaultDeserializationContext dc = (DefaultDeserializationContext) _context;
        JavaType type = _context.constructType(String.class);
        JsonDeserializer<Object> deser = _context.findRootValueDeserializer(type);
        Object result = dc.readRootValue(p, type, deser, null);
        assertEquals("hello world", result);
    }

    // Tests readRootValue with empty parser returns null
    @Test
    public void testReadRootValue_emptyInput_returnsNull() throws Exception {
        JsonParser p = _mapper.getFactory().createParser("");
        DefaultDeserializationContext dc = (DefaultDeserializationContext) _context;
        JavaType type = _context.constructType(String.class);
        JsonDeserializer<Object> deser = _context.findRootValueDeserializer(type);
        Object result = dc.readRootValue(p, type, deser, null);
        assertNull(result);
    }
}