package com.fasterxml.jackson.databind.deser;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class CreatorPropertyTest {

    private JavaType _stringType;
    private PropertyName _propName;
    private CreatorProperty _creatorProperty;

    @Before
    public void setUp() {
        _stringType = TypeFactory.defaultInstance().constructType(String.class);
        _propName = new PropertyName("testProp");
        _creatorProperty = new CreatorProperty(
                _propName,
                _stringType,
                null,
                null,
                new AnnotationMap(),
                null,
                0,
                "injectId",
                PropertyMetadata.STD_REQUIRED
        );
    }

    // Tests construction and getter methods
    @Test
    public void testGetters_validInstance_returnsConfiguredValues() {
        assertEquals("testProp", _creatorProperty.getName());
        assertEquals(_stringType, _creatorProperty.getType());
        assertEquals(0, _creatorProperty.getCreatorIndex());
        assertEquals("injectId", _creatorProperty.getInjectableValueId());
        assertNull(_creatorProperty.getMember());
        assertNull(_creatorProperty.getAnnotation(Override.class));
        assertFalse(_creatorProperty.isIgnorable());
    }

    // Tests toString format
    @Test
    public void testToString_validInstance_returnsFormattedString() {
        String str = _creatorProperty.toString();
        assertTrue(str.contains("testProp"));
        assertTrue(str.contains("injectId"));
    }

    // Tests markAsIgnorable flag modification
    @Test
    public void testMarkAsIgnorable_defaultState_setsIgnorableToTrue() {
        assertFalse(_creatorProperty.isIgnorable());
        _creatorProperty.markAsIgnorable();
        assertTrue(_creatorProperty.isIgnorable());
    }

    // Tests withName creates copy with updated property name
    @Test
    public void testWithName_newName_returnsNewInstanceWithUpdatedName() {
        PropertyName newName = new PropertyName("renamedProp");
        SettableBeanProperty renamed = _creatorProperty.withName(newName);

        assertNotNull(renamed);
        assertNotSame(_creatorProperty, renamed);
        assertEquals("renamedProp", renamed.getName());
        assertEquals(_creatorProperty.getCreatorIndex(), renamed.getCreatorIndex());
        assertEquals(_creatorProperty.getInjectableValueId(), renamed.getInjectableValueId());
    }

    // Tests withValueDeserializer when same deserializer is provided (returns this)
    @Test
    public void testWithValueDeserializer_sameDeserializer_returnsSameInstance() {
        SettableBeanProperty result = _creatorProperty.withValueDeserializer(_creatorProperty.getValueDeserializer());
        assertSame(_creatorProperty, result);
    }

    // Tests withValueDeserializer when different deserializer is provided (returns new instance)
    @Test
    public void testWithValueDeserializer_newDeserializer_returnsNewInstance() {
        JsonDeserializer<Object> deser = NullifyingDeserializer.instance;
        SettableBeanProperty result = _creatorProperty.withValueDeserializer(deser);

        assertNotNull(result);
        assertNotSame(_creatorProperty, result);
        assertSame(deser, result.getValueDeserializer());
    }

    // Tests withNullProvider creates new instance with updated provider
    @Test
    public void testWithNullProvider_customProvider_returnsNewInstanceWithNullProvider() {
        NullValueProvider nva = NullsConstantProvider.nuller();
        SettableBeanProperty result = _creatorProperty.withNullProvider(nva);

        assertNotNull(result);
        assertNotSame(_creatorProperty, result);
        assertSame(nva, result.getNullValueProvider());
    }

    // Tests findInjectableValue when injectableValueId is null throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testFindInjectableValue_nullInjectableValueId_throwsException() throws Exception {
        CreatorProperty propWithoutInjectId = new CreatorProperty(
                _propName,
                _stringType,
                null,
                null,
                new AnnotationMap(),
                null,
                0,
                null,
                PropertyMetadata.STD_REQUIRED
        );
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        propWithoutInjectId.findInjectableValue(ctxt, new Object());
    }

    // Tests findInjectableValue with context when injectableValueId is present
    @Test
    public void testFindInjectableValue_withInjectableValueId_returnsValue() throws Exception {
        InjectableValues.Std injectables = new InjectableValues.Std();
        injectables.addValue("injectId", "injectedVal");
        ObjectMapper mapper = new ObjectMapper();
        mapper.setInjectableValues(injectables);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Object val = _creatorProperty.findInjectableValue(ctxt, new Object());
        assertEquals("injectedVal", val);
    }

    // Tests inject method delegates to findInjectableValue and set
    @Test(expected = InvalidDefinitionException.class)
    public void testInject_missingFallbackSetter_throwsInvalidDefinitionException() throws Exception {
        InjectableValues.Std injectables = new InjectableValues.Std();
        injectables.addValue("injectId", "injectedVal");
        ObjectMapper mapper = new ObjectMapper();
        mapper.setInjectableValues(injectables);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        _creatorProperty.inject(ctxt, new Object());
    }

    // Tests set without fallback setter throws InvalidDefinitionException
    @Test(expected = InvalidDefinitionException.class)
    public void testSet_missingFallbackSetter_throwsInvalidDefinitionException() throws IOException {
        _creatorProperty.set(new Object(), "value");
    }

    // Tests setAndReturn without fallback setter throws InvalidDefinitionException
    @Test(expected = InvalidDefinitionException.class)
    public void testSetAndReturn_missingFallbackSetter_throwsInvalidDefinitionException() throws IOException {
        _creatorProperty.setAndReturn(new Object(), "value");
    }

    // Tests deserializeAndSet without fallback setter throws InvalidDefinitionException
    @Test(expected = InvalidDefinitionException.class)
    public void testDeserializeAndSet_missingFallbackSetter_throwsInvalidDefinitionException() throws IOException {
        _creatorProperty.deserializeAndSet(null, null, new Object());
    }

    // Tests deserializeSetAndReturn without fallback setter throws InvalidDefinitionException
    @Test(expected = InvalidDefinitionException.class)
    public void testDeserializeSetAndReturn_missingFallbackSetter_throwsInvalidDefinitionException() throws IOException {
        _creatorProperty.deserializeSetAndReturn(null, null, new Object());
    }

    // Tests fixAccess when fallbackSetter is null does not throw exception
    @Test
    public void testFixAccess_nullFallbackSetter_noExceptionThrown() {
        ObjectMapper mapper = new ObjectMapper();
        _creatorProperty.fixAccess(mapper.getDeserializationConfig());
    }

    // Tests fallbackSetter assignment and fixAccess delegation
    @Test
    public void testSetFallbackSetter_validSetter_delegatesFixAccess() {
        CreatorProperty fallback = new CreatorProperty(
                _propName,
                _stringType,
                null,
                null,
                new AnnotationMap(),
                null,
                0,
                null,
                PropertyMetadata.STD_REQUIRED
        );
        _creatorProperty.setFallbackSetter(fallback);
        ObjectMapper mapper = new ObjectMapper();
        _creatorProperty.fixAccess(mapper.getDeserializationConfig());
    }
}