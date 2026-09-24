package com.fasterxml.jackson.databind.jsontype.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.FileHandler;
import java.rmi.server.UnicastRemoteObject;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class SubTypeValidatorTest {

    private SubTypeValidator validator;
    private TypeFactory typeFactory;

    @Before
    public void setUp() {
        validator = SubTypeValidator.instance();
        typeFactory = TypeFactory.defaultInstance();
    }

    // Tests singleton instance retrieval
    @Test
    public void testInstance_returnsNonNullSingleton() {
        SubTypeValidator instance1 = SubTypeValidator.instance();
        SubTypeValidator instance2 = SubTypeValidator.instance();
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    // Tests protected constructor instantiation
    @Test
    public void testConstructor_createsInstance() {
        SubTypeValidator customValidator = new SubTypeValidator();
        assertNotNull(customValidator);
    }

    // Tests default illegal class names set contains known dangerous classes
    @Test
    public void testDefaultNoDeserClassNames_containsExpectedEntries() {
        assertNotNull(SubTypeValidator.DEFAULT_NO_DESER_CLASS_NAMES);
        assertTrue(SubTypeValidator.DEFAULT_NO_DESER_CLASS_NAMES.contains("java.util.logging.FileHandler"));
        assertTrue(SubTypeValidator.DEFAULT_NO_DESER_CLASS_NAMES.contains("java.rmi.server.UnicastRemoteObject"));
        assertTrue(SubTypeValidator.DEFAULT_NO_DESER_CLASS_NAMES.contains("com.sun.rowset.JdbcRowSetImpl"));
    }

    // Tests normal safe types are allowed without throwing exception
    @Test
    public void testValidateSubType_safeTypes_allowsDeserialization() throws Exception {
        JavaType stringType = typeFactory.constructType(String.class);
        JavaType intType = typeFactory.constructType(Integer.class);
        JavaType objectType = typeFactory.constructType(Object.class);

        validator.validateSubType(null, stringType);
        validator.validateSubType(null, intType);
        validator.validateSubType(null, objectType);
    }

    // Tests standard collection safe type is allowed
    @Test
    public void testValidateSubType_safeCollectionType_allowsDeserialization() throws Exception {
        JavaType listType = typeFactory.constructType(java.util.ArrayList.class);
        validator.validateSubType(null, listType);
    }

    // Tests FileHandler is blocked as illegal type
    @Test(expected = JsonMappingException.class)
    public void testValidateSubType_fileHandler_throwsJsonMappingException() throws Exception {
        JavaType type = typeFactory.constructType(FileHandler.class);
        validator.validateSubType(null, type);
    }

    // Tests UnicastRemoteObject is blocked as illegal type
    @Test(expected = JsonMappingException.class)
    public void testValidateSubType_unicastRemoteObject_throwsJsonMappingException() throws Exception {
        JavaType type = typeFactory.constructType(UnicastRemoteObject.class);
        validator.validateSubType(null, type);
    }

    // Tests exception message contains class name and security reason
    @Test
    public void testValidateSubType_illegalTypeExceptionMessage_containsExpectedDetails() {
        JavaType type = typeFactory.constructType(FileHandler.class);
        try {
            validator.validateSubType(null, type);
            fail("Expected JsonMappingException to be thrown");
        } catch (JsonMappingException e) {
            String message = e.getMessage();
            assertTrue(message.contains(FileHandler.class.getName()));
            assertTrue(message.contains("prevented for security reasons"));
        }
    }

    // Tests custom illegal class names set blocking configured type
    @Test(expected = JsonMappingException.class)
    public void testValidateSubType_customIllegalClassNames_blocksConfiguredType() throws Exception {
        SubTypeValidator customValidator = new SubTypeValidator();
        Set<String> customSet = new HashSet<String>();
        customSet.add(String.class.getName());
        customValidator._cfgIllegalClassNames = Collections.unmodifiableSet(customSet);

        JavaType stringType = typeFactory.constructType(String.class);
        customValidator.validateSubType(null, stringType);
    }

    // Tests custom illegal class names allows unconfigured type
    @Test
    public void testValidateSubType_customIllegalClassNames_allowsUnconfiguredType() throws Exception {
        SubTypeValidator customValidator = new SubTypeValidator();
        Set<String> customSet = new HashSet<String>();
        customSet.add(String.class.getName());
        customValidator._cfgIllegalClassNames = Collections.unmodifiableSet(customSet);

        JavaType intType = typeFactory.constructType(Integer.class);
        customValidator.validateSubType(null, intType);
    }
}