package com.fasterxml.jackson.databind.jsontype.impl;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class SubTypeValidatorTest {

    private final SubTypeValidator validator = SubTypeValidator.instance();
    private final DeserializationContext ctxt = mock(DeserializationContext.class);
    private final TypeFactory typeFactory = TypeFactory.defaultInstance();

    private JavaType type(Class<?> clazz) {
        return typeFactory.constructType(clazz);
    }

    // Normal case: valid class not in blacklist
    @Test
    public void testValidateSubType_normalClass_noException() throws Exception {
        validator.validateSubType(ctxt, type(String.class));
    }

    // Invalid: class name present in DEFAULT_NO_DESER_CLASS_NAMES (JDK class)
    @Test(expected = JsonMappingException.class)
    public void testValidateSubType_illegalClassNameInSet_throwsException() throws Exception {
        validator.validateSubType(ctxt, type(java.util.logging.FileHandler.class));
    }

    // Invalid: Spring class that is in blacklist (interface, but caught before interface check)
    @Test(expected = JsonMappingException.class)
    public void testValidateSubType_springFactoryObjectInSet_throwsException() throws Exception {
        validator.validateSubType(ctxt, type(Class.forName("org.springframework.beans.factory.ObjectFactory")));
    }

    // Spring interface not in blacklist -> should pass (isInterface() true)
    @Test
    public void testValidateSubType_springInterfaceNotInSet_noException() throws Exception {
        validator.validateSubType(ctxt, type(Class.forName("org.springframework.beans.factory.BeanFactory")));
    }

    // Spring class that extends AbstractPointcutAdvisor -> throws
    @Test(expected = JsonMappingException.class)
    public void testValidateSubType_springClassExtendsAbstractPointcutAdvisor_throwsException() throws Exception {
        validator.validateSubType(ctxt, type(Class.forName("org.springframework.aop.support.DefaultPointcutAdvisor")));
    }

    // Spring class that extends AbstractApplicationContext -> throws
    @Test(expected = JsonMappingException.class)
    public void testValidateSubType_springClassExtendsAbstractApplicationContext_throwsException() throws Exception {
        validator.validateSubType(ctxt, type(Class.forName("org.springframework.context.support.ClassPathXmlApplicationContext")));
    }

    // Spring class without dangerous superclass (no AbstractPointcutAdvisor/AbstractApplicationContext) -> should pass
    @Test
    public void testValidateSubType_springClassSafe_noException() throws Exception {
        validator.validateSubType(ctxt, type(Class.forName("org.springframework.core.io.ByteArrayResource")));
    }

    // Non-spring interface -> should pass (isInterface() true, no spring prefix)
    @Test
    public void testValidateSubType_interface_noException() throws Exception {
        validator.validateSubType(ctxt, type(java.io.Serializable.class));
    }
}