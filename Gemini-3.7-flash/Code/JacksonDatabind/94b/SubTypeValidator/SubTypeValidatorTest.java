package com.fasterxml.jackson.databind.jsontype.impl;

import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

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

    // Tests singleton instance is not null
    @Test
    public void testInstance_default_returnsNonNullSingleton() {
        SubTypeValidator inst1 = SubTypeValidator.instance();
        SubTypeValidator inst2 = SubTypeValidator.instance();
        assertNotNull(inst1);
        assertSame(inst1, inst2);
    }

    // Tests protected constructor can be instantiated
    @Test
    public void testConstructor_newSubclass_initializesCorrectly() {
        SubTypeValidator customValidator = new SubTypeValidator();
        assertNotNull(customValidator);
        assertNotNull(customValidator._cfgIllegalClassNames);
        assertTrue(customValidator._cfgIllegalClassNames.contains("java.util.logging.FileHandler"));
    }

    // Tests DEFAULT_NO_DESER_CLASS_NAMES contains expected default illegal classes
    @Test
    public void testDefaultNoDeserClassNames_checkContents_containsExpectedClasses() {
        Set<String> illegalSet = SubTypeValidator.DEFAULT_NO_DESER_CLASS_NAMES;
        assertNotNull(illegalSet);
        assertTrue(illegalSet.contains("java.util.logging.FileHandler"));
        assertTrue(illegalSet.contains("java.rmi.server.UnicastRemoteObject"));
        assertTrue(illegalSet.contains("org.apache.commons.collections.functors.InvokerTransformer"));
        assertTrue(illegalSet.contains("org.apache.commons.collections.functors.InstantiateTransformer"));
        assertTrue(illegalSet.contains("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl"));
        assertTrue(illegalSet.contains("com.sun.rowset.JdbcRowSetImpl"));
        assertTrue(illegalSet.contains("org.apache.tomcat.dbcp.dbcp2.BasicDataSource"));
        assertTrue(illegalSet.contains("com.sun.org.apache.bcel.internal.util.ClassLoader"));
    }

    // Tests validation succeeds for standard safe class
    @Test
    public void testValidateSubType_safeClass_noExceptionThrown() throws Exception {
        JavaType type = typeFactory.constructType(String.class);
        validator.validateSubType(null, type);
    }

    // Tests validation succeeds for collection safe class
    @Test
    public void testValidateSubType_safeCollectionClass_noExceptionThrown() throws Exception {
        JavaType type = typeFactory.constructType(ArrayList.class);
        validator.validateSubType(null, type);
    }

    // Tests validation succeeds for interface type
    @Test
    public void testValidateSubType_interfaceType_noExceptionThrown() throws Exception {
        JavaType type = typeFactory.constructType(List.class);
        validator.validateSubType(null, type);
    }

    // Tests validation succeeds for map interface type
    @Test
    public void testValidateSubType_mapInterfaceType_noExceptionThrown() throws Exception {
        JavaType type = typeFactory.constructType(Map.class);
        validator.validateSubType(null, type);
    }

    // Tests validation succeeds for Object class
    @Test
    public void testValidateSubType_objectClass_noExceptionThrown() throws Exception {
        JavaType type = typeFactory.constructType(Object.class);
        validator.validateSubType(null, type);
    }

    // Tests illegal JDK class FileHandler throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testValidateSubType_fileHandlerClass_throwsJsonMappingException() throws Exception {
        JavaType type = typeFactory.constructType(FileHandler.class);
        validator.validateSubType(null, type);
    }

    // Tests illegal JDK class UnicastRemoteObject throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testValidateSubType_unicastRemoteObjectClass_throwsJsonMappingException() throws Exception {
        JavaType type = typeFactory.constructType(UnicastRemoteObject.class);
        validator.validateSubType(null, type);
    }

    // Tests exception message contains expected details
    @Test
    public void testValidateSubType_illegalClass_verifyExceptionMessage() {
        JavaType type = typeFactory.constructType(FileHandler.class);
        try {
            validator.validateSubType(null, type);
            fail("Expected JsonMappingException to be thrown");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("Illegal type (java.util.logging.FileHandler) to deserialize"));
            assertTrue(msg.contains("prevented for security reasons"));
        }
    }

    // Tests custom illegal set correctly blocks configured class
    @Test(expected = JsonMappingException.class)
    public void testValidateSubType_customIllegalClass_throwsJsonMappingException() throws Exception {
        SubTypeValidator customValidator = new SubTypeValidator();
        Set<String> customSet = new HashSet<String>();
        customSet.add(Integer.class.getName());
        customValidator._cfgIllegalClassNames = Collections.unmodifiableSet(customSet);

        JavaType type = typeFactory.constructType(Integer.class);
        customValidator.validateSubType(null, type);
    }

    // Tests custom illegal set allows non-listed class
    @Test
    public void testValidateSubType_customIllegalSetUnlistedClass_success() throws Exception {
        SubTypeValidator customValidator = new SubTypeValidator();
        Set<String> customSet = new HashSet<String>();
        customSet.add(Integer.class.getName());
        customValidator._cfgIllegalClassNames = Collections.unmodifiableSet(customSet);

        JavaType type = typeFactory.constructType(String.class);
        customValidator.validateSubType(null, type);
    }
}