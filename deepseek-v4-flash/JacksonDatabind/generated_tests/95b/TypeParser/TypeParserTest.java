package com.fasterxml.jackson.databind.type;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;

public class TypeParserTest {
    private TypeParser parser;

    @Before
    public void setUp() {
        parser = new TypeParser(TypeFactory.defaultInstance());
    }

    // Normal case: simple class name
    @Test
    public void testParse_simpleClassName_returnsCorrectType() {
        JavaType type = parser.parse("java.lang.String");
        assertEquals(java.lang.String.class, type.getRawClass());
        assertNull(type.getBindings());
    }

    // Normal case: single generic parameter
    @Test
    public void testParse_singleGeneric_returnsParameterizedType() {
        JavaType type = parser.parse("java.util.List<java.lang.String>");
        assertEquals(java.util.List.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(java.lang.String.class, type.containedType(0).getRawClass());
    }

    // Normal case: nested generics
    @Test
    public void testParse_nestedGeneric_returnsNestedParameterizedType() {
        JavaType type = parser.parse("java.util.List<java.util.List<java.lang.String>>");
        assertEquals(java.util.List.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        JavaType inner = type.containedType(0);
        assertEquals(java.util.List.class, inner.getRawClass());
        assertEquals(1, inner.containedTypeCount());
        assertEquals(java.lang.String.class, inner.containedType(0).getRawClass());
    }

    // Normal case: multiple generic parameters
    @Test
    public void testParse_multipleTypeParams_returnsParameterizedType() {
        JavaType type = parser.parse("java.util.Map<java.lang.String,java.lang.Integer>");
        assertEquals(java.util.Map.class, type.getRawClass());
        assertEquals(2, type.containedTypeCount());
        assertEquals(java.lang.String.class, type.containedType(0).getRawClass());
        assertEquals(java.lang.Integer.class, type.containedType(1).getRawClass());
    }

    // Boundary: empty input
    @Test(expected = IllegalArgumentException.class)
    public void testParse_emptyInput_throwsIllegalArgumentException() {
        parser.parse("");
    }

    // Edge: missing closing bracket
    @Test(expected = IllegalArgumentException.class)
    public void testParse_missingClosingBracket_throwsIllegalArgumentException() {
        parser.parse("java.util.List<java.lang.String");
    }

    // Edge: unexpected token (trailing comma before closing)
    @Test(expected = IllegalArgumentException.class)
    public void testParse_trailingCommaBeforeClose_throwsIllegalArgumentException() {
        parser.parse("java.util.List<java.lang.String,>");
    }

    // Edge: extra tokens after complete type
    @Test(expected = IllegalArgumentException.class)
    public void testParse_extraTokensAfterComplete_throwsIllegalArgumentException() {
        parser.parse("java.util.List<java.lang.String> extra");
    }

    // Edge: unconsumed token after raw type (comma)
    @Test(expected = IllegalArgumentException.class)
    public void testParse_commaAfterRawType_throwsIllegalArgumentException() {
        parser.parse("java.lang.String,");
    }

    // Edge: non-existent class name
    @Test(expected = IllegalArgumentException.class)
    public void testParse_invalidClassName_throwsIllegalArgumentException() {
        parser.parse("some.nonexistent.Class");
    }

    // Defect-detection: whitespace before '<' should still produce parameterized type
    @Test
    public void testParse_whitespaceBeforeAngle_returnsParameterizedType() {
        JavaType type = parser.parse("java.util.List <java.lang.String>");
        assertEquals(java.util.List.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(java.lang.String.class, type.containedType(0).getRawClass());
    }

    // Defect-detection: whitespace after '<'
    @Test
    public void testParse_whitespaceAfterAngle_returnsParameterizedType() {
        JavaType type = parser.parse("java.util.List< java.lang.String>");
        assertEquals(java.util.List.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(java.lang.String.class, type.containedType(0).getRawClass());
    }

    // Defect-detection: whitespace around comma
    @Test
    public void testParse_whitespaceAroundComma_returnsParameterizedType() {
        JavaType type = parser.parse("java.util.Map<java.lang.String , java.lang.Integer>");
        assertEquals(java.util.Map.class, type.getRawClass());
        assertEquals(2, type.containedTypeCount());
        assertEquals(java.lang.String.class, type.containedType(0).getRawClass());
        assertEquals(java.lang.Integer.class, type.containedType(1).getRawClass());
    }

    // Defect-detection: whitespace around all delimiters
    @Test
    public void testParse_whitespaceAroundAll_returnsParameterizedType() {
        JavaType type = parser.parse("java.util.List < java.lang.String >");
        assertEquals(java.util.List.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(java.lang.String.class, type.containedType(0).getRawClass());
    }

    // Complex nested with multiple parameters and whitespace
    @Test
    public void testParse_nestedMultipleWithSpaces_returnsParameterizedType() {
        JavaType type = parser.parse("java.util.Map<java.util.List<java.lang.String> , java.lang.Integer>");
        assertEquals(java.util.Map.class, type.getRawClass());
        assertEquals(2, type.containedTypeCount());
        JavaType first = type.containedType(0);
        assertEquals(java.util.List.class, first.getRawClass());
        assertEquals(1, first.containedTypeCount());
        assertEquals(java.lang.String.class, first.containedType(0).getRawClass());
        assertEquals(java.lang.Integer.class, type.containedType(1).getRawClass());
    }

    // ==========  New test cases for uncovered coverage  ==========

    // Normal case: array type
    @Test
    public void testParse_arrayType_returnsArrayType() {
        JavaType type = parser.parse("java.lang.String[]");
        assertTrue(type.isArrayType());
        assertEquals(java.lang.String.class, type.getContentType().getRawClass());
    }

    // Normal case: inner class (e.g., Map.Entry)
    @Test
    public void testParse_innerClass_returnsCorrectType() {
        JavaType type = parser.parse("java.util.Map.Entry");
        assertEquals(java.util.Map.Entry.class, type.getRawClass());
        assertNull(type.getBindings());
    }

    // Normal case: wildcard parameter
    @Test
    public void testParse_wildcardParameter_returnsParameterizedWithWildcard() {
        JavaType type = parser.parse("java.util.List<?>");
        assertEquals(java.util.List.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        JavaType param = type.containedType(0);
        assertTrue(param.isWildcardType());
    }

    // Edge: empty generic parameter list (no type between < and >)
    @Test(expected = IllegalArgumentException.class)
    public void testParse_emptyGeneric_throwsIllegalArgumentException() {
        parser.parse("java.util.List<>");
    }

    // Edge: duplicate comma (two commas in a row)
    @Test(expected = IllegalArgumentException.class)
    public void testParse_duplicateComma_throwsIllegalArgumentException() {
        parser.parse("java.util.Map<java.lang.String,,java.lang.Integer>");
    }

    // Edge: nested missing close bracket (closing > missing for inner type)
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nestedMissingClose_throwsIllegalArgumentException() {
        parser.parse("java.util.List<java.util.List<java.lang.String>");
    }

    // Edge: primitive type (not a class name, should be rejected)
    @Test(expected = IllegalArgumentException.class)
    public void testParse_primitiveType_throwsIllegalArgumentException() {
        parser.parse("int");
    }
}