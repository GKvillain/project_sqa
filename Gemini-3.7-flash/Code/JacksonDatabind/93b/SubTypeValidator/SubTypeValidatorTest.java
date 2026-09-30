package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;

import static org.junit.Assert.*;

public class SubTypeValidatorTest {

    private SubTypeValidator validator;
    private TypeFactory typeFactory;

    @Before
    public void setUp() {
        validator = SubTypeValidator.instance();
        typeFactory = TypeFactory.defaultInstance();
    }

    // Tests that instance() returns a non-null singleton instance
    @Test
    public void testInstance_returnsSingleton_isNotNull() {
        SubTypeValidator instance1 = SubTypeValidator.instance();
        SubTypeValidator instance2 = SubTypeValidator.instance();
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    // Tests normal case where standard Java class passes validation
    @Test
    public void testValidateSubType_standardClass_doesNotThrow() throws Exception {
        JavaType type = typeFactory.constructType(String.class);
        validator.validateSubType(null, type);
    }

    // Tests normal case where standard Collection class passes validation
    @Test
    public void testValidateSubType_arrayListClass_doesNotThrow() throws Exception {
        JavaType type = typeFactory.constructType(ArrayList.class);
        validator.validateSubType(null, type);
    }

    // Tests normal case where standard Map class passes validation
    @Test
    public void testValidateSubType_hashMapClass_doesNotThrow() throws Exception {
        JavaType type = typeFactory.constructType(HashMap.class);
        validator.validateSubType(null, type);
    }

    // Tests boundary case where Object.class passes validation
    @Test
    public void testValidateSubType_objectClass_doesNotThrow() throws Exception {
        JavaType type = typeFactory.constructType(Object.class);
        validator.validateSubType(null, type);
    }

    // Tests interface type validation
    @Test
    public void testValidateSubType_interfaceType_doesNotThrow() throws Exception {
        JavaType type = typeFactory.constructType(List.class);
        validator.validateSubType(null, type);
    }

    // Tests another interface type validation
    @Test
    public void testValidateSubType_mapInterfaceType_doesNotThrow() throws Exception {
        JavaType type = typeFactory.constructType(Map.class);
        validator.validateSubType(null, type);
    }

    // Tests blacklisted JDK class FileHandler throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testValidateSubType_blacklistedFileHandler_throwsJsonMappingException() throws Exception {
        JavaType type = typeFactory.constructType(FileHandler.class);
        validator.validateSubType(null, type);
    }

    // Tests blacklisted JDK class UnicastRemoteObject throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testValidateSubType_blacklistedUnicastRemoteObject_throwsJsonMappingException() throws Exception {
        JavaType type = typeFactory.constructType(UnicastRemoteObject.class);
        validator.validateSubType(null, type);
    }

    // Tests exception message contains class name and security reason
    @Test
    public void testValidateSubType_blacklistedClass_verifiesExceptionMessage() {
        JavaType type = typeFactory.constructType(FileHandler.class);
        try {
            validator.validateSubType(null, type);
            fail("Expected JsonMappingException to be thrown");
        } catch (JsonMappingException e) {
            String message = e.getMessage();
            assertTrue(message.contains("java.util.logging.FileHandler"));
            assertTrue(message.contains("prevented for security reasons"));
        }
    }

    // Tests validation when DeserializationContext is provided
    @Test(expected = JsonMappingException.class)
    public void testValidateSubType_withDeserializationContext_throwsJsonMappingException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = typeFactory.constructType(FileHandler.class);
        validator.validateSubType(mapper.getDeserializationContext(), type);
    }
}