package com.fasterxml.jackson.databind.type;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;

public class TypeParserTest {

    private TypeFactory _typeFactory;
    private TypeParser _parser;

    @Before
    public void setUp() {
        _typeFactory = TypeFactory.defaultInstance();
        _parser = new TypeParser(_typeFactory);
    }

    // Tests parsing simple non-generic class
    @Test
    public void testParse_simpleClass_returnsJavaType() {
        JavaType type = _parser.parse("java.lang.String");
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }

    // Tests parsing generic type with single parameter
    @Test
    public void testParse_singleGenericParameter_returnsJavaType() {
        JavaType type = _parser.parse("java.util.ArrayList<java.lang.String>");
        assertNotNull(type);
        assertEquals(java.util.ArrayList.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    // Tests parsing generic type with multiple parameters
    @Test
    public void testParse_multipleGenericParameters_returnsJavaType() {
        JavaType type = _parser.parse("java.util.HashMap<java.lang.String,java.lang.Integer>");
        assertNotNull(type);
        assertEquals(java.util.HashMap.class, type.getRawClass());
        assertEquals(2, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(Integer.class, type.containedType(1).getRawClass());
    }

    // Tests parsing nested generic types
    @Test
    public void testParse_nestedGenerics_returnsJavaType() {
        JavaType type = _parser.parse("java.util.HashMap<java.lang.String,java.util.List<java.lang.Long>>");
        assertNotNull(type);
        assertEquals(java.util.HashMap.class, type.getRawClass());
        JavaType valueType = type.containedType(1);
        assertEquals(java.util.List.class, valueType.getRawClass());
        assertEquals(Long.class, valueType.containedType(0).getRawClass());
    }

    // Tests parsing input with extra whitespace around tokens
    @Test
    public void testParse_withWhitespaces_returnsJavaType() {
        JavaType type = _parser.parse("  java.util.Map < java.lang.String , java.lang.Double >  ");
        assertNotNull(type);
        assertEquals(java.util.Map.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(Double.class, type.containedType(1).getRawClass());
    }

    // Tests parsing empty string throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testParse_emptyString_throwsException() {
        _parser.parse("");
    }

    // Tests parsing unexpected tokens after complete type
    @Test(expected = IllegalArgumentException.class)
    public void testParse_unexpectedTokensAfterCompleteType_throwsException() {
        _parser.parse("java.lang.String extra");
    }

    // Tests parsing unclosed generic type
    @Test(expected = IllegalArgumentException.class)
    public void testParse_unclosedGenerics_throwsException() {
        _parser.parse("java.util.List<java.lang.String");
    }

    // Tests parsing invalid separator in generic parameter list
    @Test(expected = IllegalArgumentException.class)
    public void testParse_invalidSeparatorInGenerics_throwsException() {
        _parser.parse("java.util.Map<java.lang.String;java.lang.Integer>");
    }

    // Tests parsing unknown class name
    @Test(expected = IllegalArgumentException.class)
    public void testParse_unknownClassName_throwsException() {
        _parser.parse("com.nonexistent.UnknownClass");
    }

    // Tests withFactory returning same instance when same factory is passed
    @Test
    public void testWithFactory_sameFactory_returnsSameInstance() {
        TypeParser result = _parser.withFactory(_typeFactory);
        assertSame(_parser, result);
    }

    // Tests withFactory returning new instance when different factory is passed
    @Test
    public void testWithFactory_differentFactory_returnsNewInstance() {
        TypeFactory newFactory = TypeFactory.defaultInstance().withModifier(null);
        TypeParser result = _parser.withFactory(newFactory);
        assertNotSame(_parser, result);
    }

    // Tests tokenizer pushBack and remainingInput functionality
    @Test
    public void testTokenizer_pushBackAndRemainingInput_behavesCorrectly() {
        TypeParser.MyTokenizer tokenizer = new TypeParser.MyTokenizer("java.util.List<java.lang.String>");
        assertTrue(tokenizer.hasMoreTokens());
        assertEquals("java.util.List", tokenizer.nextToken());
        assertTrue(tokenizer.hasMoreTokens());
        String token = tokenizer.nextToken();
        assertEquals("<", token);
        tokenizer.pushBack(token);
        assertTrue(tokenizer.hasMoreTokens());
        assertEquals("<", tokenizer.nextToken());
        assertEquals("java.util.List<java.lang.String>", tokenizer.getAllInput());
    }

    // Tests tokenizer getUsedInput and getRemainingInput methods
    @Test
    public void testTokenizer_getUsedAndRemainingInput() {
        TypeParser.MyTokenizer tokenizer = new TypeParser.MyTokenizer("java.util.List<java.lang.String>");
        assertEquals("", tokenizer.getUsedInput());
        assertEquals("java.util.List<java.lang.String>", tokenizer.getRemainingInput());

        tokenizer.nextToken(); // "java.util.List"
        assertEquals("java.util.List", tokenizer.getUsedInput());
        assertEquals("<java.lang.String>", tokenizer.getRemainingInput());

        tokenizer.nextToken(); // "<"
        assertEquals("java.util.List<", tokenizer.getUsedInput());
        assertEquals("java.lang.String>", tokenizer.getRemainingInput());
    }

    // Tests parsing empty generic parameter list
    @Test(expected = IllegalArgumentException.class)
    public void testParse_emptyGenericParams_throwsException() {
        _parser.parse("java.util.List<>");
    }

    // Tests parsing generic type missing closing bracket after comma
    @Test(expected = IllegalArgumentException.class)
    public void testParse_missingTypeAfterComma_throwsException() {
        _parser.parse("java.util.Map<java.lang.String,>");
    }

    // Tests parsing generic type that ends abruptly after opening bracket
    @Test(expected = IllegalArgumentException.class)
    public void testParse_abruptEndingAfterOpenBracket_throwsException() {
        _parser.parse("java.util.List<");
    }

    // Tests parsing input with leading closing bracket
    @Test(expected = IllegalArgumentException.class)
    public void testParse_unexpectedClosingBracket_throwsException() {
        _parser.parse(">");
    }
}