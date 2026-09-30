package com.fasterxml.jackson.databind.type;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;

public class TypeParserTest {

    private TypeFactory _typeFactory;
    private TypeParser _typeParser;

    @Before
    public void setUp() {
        _typeFactory = TypeFactory.defaultInstance();
        _typeParser = new TypeParser(_typeFactory);
    }

    // Tests withFactory returning the same instance when passing the identical factory
    @Test
    public void testWithFactory_sameFactory_returnsSameInstance() {
        TypeParser sameParser = _typeParser.withFactory(_typeFactory);
        assertSame(_typeParser, sameParser);
    }

    // Tests withFactory returning a new instance when passing a new factory
    @Test
    public void testWithFactory_differentFactory_returnsNewInstance() {
        TypeFactory newFactory = TypeFactory.defaultInstance().withModifier(null);
        TypeParser newParser = _typeParser.withFactory(newFactory);
        assertNotSame(_typeParser, newParser);
    }

    // Tests parsing simple non-generic class
    @Test
    public void testParse_simpleClass_returnsCorrectJavaType() {
        JavaType type = _typeParser.parse("java.lang.String");
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }

    // Tests parsing single generic parameter
    @Test
    public void testParse_singleGeneric_returnsCorrectJavaType() {
        JavaType type = _typeParser.parse("java.util.ArrayList<java.lang.String>");
        assertNotNull(type);
        assertEquals(java.util.ArrayList.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    // Tests parsing multiple generic parameters
    @Test
    public void testParse_multipleGenerics_returnsCorrectJavaType() {
        JavaType type = _typeParser.parse("java.util.HashMap<java.lang.String,java.lang.Integer>");
        assertNotNull(type);
        assertEquals(java.util.HashMap.class, type.getRawClass());
        assertEquals(2, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(Integer.class, type.containedType(1).getRawClass());
    }

    // Tests parsing nested generic structures
    @Test
    public void testParse_nestedGenerics_returnsCorrectJavaType() {
        JavaType type = _typeParser.parse("java.util.Map<java.lang.String,java.util.List<java.lang.Integer>>");
        assertNotNull(type);
        assertEquals(java.util.Map.class, type.getRawClass());
        assertEquals(2, type.containedTypeCount());
        JavaType valueType = type.containedType(1);
        assertEquals(java.util.List.class, valueType.getRawClass());
        assertEquals(Integer.class, valueType.containedType(0).getRawClass());
    }

    // Tests exception path when input is empty string
    @Test(expected = IllegalArgumentException.class)
    public void testParse_emptyInput_throwsIllegalArgumentException() {
        _typeParser.parse("");
    }

    // Tests exception path when input has unexpected tokens after complete type
    @Test(expected = IllegalArgumentException.class)
    public void testParse_unexpectedTokensAfterType_throwsIllegalArgumentException() {
        _typeParser.parse("java.lang.String extra");
    }

    // Tests exception path when generic closing bracket is missing
    @Test(expected = IllegalArgumentException.class)
    public void testParse_unclosedGenerics_throwsIllegalArgumentException() {
        _typeParser.parse("java.util.List<java.lang.String");
    }

    // Tests exception path when comma or closing bracket is missing between generic parameters
    @Test(expected = IllegalArgumentException.class)
    public void testParse_missingCommaInGenerics_throwsIllegalArgumentException() {
        _typeParser.parse("java.util.Map<java.lang.String java.lang.Integer>");
    }

    // Tests exception path when class cannot be located
    @Test(expected = IllegalArgumentException.class)
    public void testParse_unknownClassName_throwsIllegalArgumentException() {
        _typeParser.parse("com.nonexistent.NoSuchClass");
    }

    // Tests MyTokenizer pushBack and token retrieval functionality
    @Test
    public void testMyTokenizer_pushBack_retainsCorrectToken() {
        TypeParser.MyTokenizer tokenizer = new TypeParser.MyTokenizer("java.util.List<java.lang.String>");
        assertTrue(tokenizer.hasMoreTokens());
        String token = tokenizer.nextToken();
        assertEquals("java.util.List", token);
        tokenizer.pushBack(token);
        assertTrue(tokenizer.hasMoreTokens());
        assertEquals("java.util.List", tokenizer.nextToken());
        assertEquals("java.util.List<java.lang.String>", tokenizer.getAllInput());
    }

    // Tests MyTokenizer getUsedInput and getRemainingInput methods
    @Test
    public void testMyTokenizer_usedAndRemainingInput() {
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

    // Tests parsing with whitespace around delimiters and tokens
    @Test
    public void testParse_withWhitespace() {
        JavaType type = _typeParser.parse("  java.util.Map < java.lang.String , java.lang.Integer >  ");
        assertNotNull(type);
        assertEquals(java.util.Map.class, type.getRawClass());
        assertEquals(2, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(Integer.class, type.containedType(1).getRawClass());
    }

    // Tests exception path when generic parameter list ends abruptly right after opening bracket
    @Test(expected = IllegalArgumentException.class)
    public void testParse_emptyGenericParams_throwsIllegalArgumentException() {
        _typeParser.parse("java.util.List<");
    }

    // Tests exception path when invalid delimiter is encountered inside generic parameter list
    @Test(expected = IllegalArgumentException.class)
    public void testParse_invalidDelimiterInGenerics_throwsIllegalArgumentException() {
        _typeParser.parse("java.util.Map<java.lang.String;java.lang.Integer>");
    }

    // Tests exception message formatting on parse error
    @Test
    public void testParse_exceptionMessageContainsDetailedContext() {
        try {
            _typeParser.parse("java.util.List<java.lang.String");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Unexpected end-of-string"));
        }
    }
}