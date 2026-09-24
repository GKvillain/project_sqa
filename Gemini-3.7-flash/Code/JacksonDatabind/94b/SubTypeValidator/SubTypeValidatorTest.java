package com.fasterxml.jackson.databind.jsontype.impl;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class SubTypeValidatorTest {

    private SubTypeValidator validator;
    private TypeFactory typeFactory;

    // Helper dummy types for testing
    static class SafeBean {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    interface SafeInterface {
        void doSomething();
    }

    @Before
    public void setUp() {
        validator = SubTypeValidator.instance();
        typeFactory = TypeFactory.defaultInstance();
    }

    // Tests singleton instance retrieval
    @Test
    public void testInstance_always_returnsNonNullSingleton() {
        SubTypeValidator instance1 = SubTypeValidator.instance();
        SubTypeValidator instance2 = SubTypeValidator.instance();
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    // Tests normal case with standard JDK class
    @Test
    public void testValidateSubType_safeJdkClass_noExceptionThrown() throws Exception {
        JavaType type = typeFactory.constructType(String.class);
        validator.validateSubType(null, type);
    }

    // Tests normal case with standard JDK collection
    @Test
    public void testValidateSubType_safeJdkCollection_noExceptionThrown() throws Exception {
        JavaType type = typeFactory.constructType(ArrayList.class);
        validator.validateSubType(null, type);
    }

    // Tests normal case with standard JDK map
    @Test
    public void testValidateSubType_safeJdkMap_noExceptionThrown() throws Exception {
        JavaType type = typeFactory.constructType(HashMap.class);
        validator.validateSubType(null, type);
    }

    // Tests root Object class
    @Test
    public void testValidateSubType_objectClass_noExceptionThrown() throws Exception {
        JavaType type = typeFactory.constructType(Object.class);
        validator.validateSubType(null, type);
    }

    // Tests branch for interface type (isInterface true)
    @Test
    public void testValidateSubType_jdkInterface_noExceptionThrown() throws Exception {
        JavaType type = typeFactory.constructType(List.class);
        validator.validateSubType(null, type);
    }

    // Tests branch for custom interface
    @Test
    public void testValidateSubType_customInterface_noExceptionThrown() throws Exception {
        JavaType type = typeFactory.constructType(SafeInterface.class);
        validator.validateSubType(null, type);
    }

    // Tests branch for custom class (isInterface false, not spring, not blacklisted)
    @Test
    public void testValidateSubType_customSafeClass_noExceptionThrown() throws Exception {
        JavaType type = typeFactory.constructType(SafeBean.class);
        validator.validateSubType(null, type);
    }

    // Tests exception path for blacklisted JDK class FileHandler
    @Test(expected = JsonMappingException.class)
    public void testValidateSubType_blacklistedFileHandler_throwsJsonMappingException() throws Exception {
        JavaType type = typeFactory.constructType(FileHandler.class);
        validator.validateSubType(null, type);
    }

    // Tests exception path and error message for blacklisted FileHandler
    @Test
    public void testValidateSubType_blacklistedFileHandler_containsDescriptiveErrorMessage() {
        JavaType type = typeFactory.constructType(FileHandler.class);
        try {
            validator.validateSubType(null, type);
            fail("Expected JsonMappingException for illegal type: FileHandler");
        } catch (JsonMappingException e) {
            String message = e.getMessage();
            assertNotNull(message);
            assertTrue(message.contains("Illegal type"));
            assertTrue(message.contains("FileHandler"));
            assertTrue(message.contains("prevented for security reasons"));
        }
    }

    // Tests exception path for blacklisted JDK class UnicastRemoteObject
    @Test(expected = JsonMappingException.class)
    public void testValidateSubType_blacklistedUnicastRemoteObject_throwsJsonMappingException() throws Exception {
        JavaType type = typeFactory.constructType(UnicastRemoteObject.class);
        validator.validateSubType(null, type);
    }

    // Tests exception path and error message for blacklisted UnicastRemoteObject
    @Test
    public void testValidateSubType_blacklistedUnicastRemoteObject_containsDescriptiveErrorMessage() {
        JavaType type = typeFactory.constructType(UnicastRemoteObject.class);
        try {
            validator.validateSubType(null, type);
            fail("Expected JsonMappingException for illegal type: UnicastRemoteObject");
        } catch (JsonMappingException e) {
            String message = e.getMessage();
            assertNotNull(message);
            assertTrue(message.contains("Illegal type"));
            assertTrue(message.contains("UnicastRemoteObject"));
            assertTrue(message.contains("prevented for security reasons"));
        }
    }

    // Tests exception path for blacklisted com.sun.rowset.JdbcRowSetImpl if available on classpath
    @Test
    public void testValidateSubType_blacklistedJdbcRowSetImpl_throwsJsonMappingException() {
        try {
            Class<?> clazz = Class.forName("com.sun.rowset.JdbcRowSetImpl");
            JavaType type = typeFactory.constructType(clazz);
            try {
                validator.validateSubType(null, type);
                fail("Expected JsonMappingException for illegal type: JdbcRowSetImpl");
            } catch (JsonMappingException e) {
                assertTrue(e.getMessage().contains("JdbcRowSetImpl"));
            }
        } catch (ClassNotFoundException ignored) {
            // Class not present in runtime environment, skip assertion
        }
    }
}