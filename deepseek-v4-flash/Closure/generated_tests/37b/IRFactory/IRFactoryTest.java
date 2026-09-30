package com.google.javascript.jscomp.parsing;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.head.ErrorReporter;
import com.google.javascript.rhino.head.ast.AstRoot;
import com.google.javascript.rhino.jstype.StaticSourceFile;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class IRFactoryTest {

    private List<String> errors;
    private List<String> warnings;
    private ErrorReporter testReporter;
    private StaticSourceFile sourceFile;
    private String sourceName = "test.js";
    private Config config;

    @Before
    public void setUp() throws Exception {
        errors = new ArrayList<String>();
        warnings = new ArrayList<String>();
        testReporter = new ErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line,
                                String lineSource, int lineOffset) {
                warnings.add(message);
            }
            @Override
            public void error(String message, String sourceName, int line,
                              String lineSource, int lineOffset) {
                errors.add(message);
            }
        };
        sourceFile = new StaticSourceFile() {
            @Override
            public String getName() {
                return sourceName;
            }
        };
        // Use reflection to create Config to avoid constructor signature guessing
        Constructor<?> configCtor = Config.class.getDeclaredConstructor(
                LanguageMode.class, boolean.class, boolean.class);
        configCtor.setAccessible(true);
        config = (Config) configCtor.newInstance(LanguageMode.ECMASCRIPT5, false, false);
        // isIdeMode = false, acceptConstKeyword = false
    }

    // Helper: parse source string, invoke IRFactory.transformTree and return root Node
    private Node parseTransform(String source) throws Exception {
        // Use reflection to create Rhino parser
        Class<?> parserClass = Class.forName("com.google.javascript.rhino.head.Parser");
        // Use default constructor, then set error reporter
        Object parser = parserClass.getConstructor().newInstance();
        Method setReporter = parserClass.getMethod("setErrorReporter", ErrorReporter.class);
        setReporter.invoke(parser, testReporter);
        Method parseMethod = parserClass.getMethod("parse", String.class, String.class, int.class);
        AstRoot ast = (AstRoot) parseMethod.invoke(parser, source, sourceName, 1);
        return IRFactory.transformTree(ast, sourceFile, source, config, testReporter);
    }

    @Test
    public void testTransform_functionStatement_returnsFunctionNode() throws Exception {
        Node root = parseTransform("function foo() {}");
        assertEquals(Token.SCRIPT, root.getType());
        Node funcNode = root.getFirstChild();
        assertNotNull(funcNode);
        assertEquals(Token.FUNCTION, funcNode.getType());
        Node nameNode = funcNode.getFirstChild();
        assertEquals(Token.NAME, nameNode.getType());
        assertEquals("foo", nameNode.getString());
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testTransform_unnamedFunctionStatement_error() throws Exception {
        Node root = parseTransform("function() {}");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("unnamed function statement"));
        Node placeholder = root.getFirstChild();
        assertEquals(Token.EXPR_RESULT, placeholder.getType());
        assertTrue(placeholder.getFirstChild().isNumber());
        assertEquals(0.0, placeholder.getFirstChild().getDouble(), 0.0);
    }

    @Test
    public void testTransform_reservedKeyword_emitsError() throws Exception {
        Node root = parseTransform("var class = 1;");
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("reserved word")));
    }

    @Test
    public void testTransform_constKeywordNotAccepted_emitsError() throws Exception {
        Node root = parseTransform("const a = 1;");
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("const")));
    }

    @Test
    public void testTransform_destructuringAssignment_emitsError() throws Exception {
        Node root = parseTransform("var [a, b] = [1, 2];");
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("destructuring")));
    }

    @Test
    public void testTransform_getterInEs3_emitsError() throws Exception {
        // Recreate config for ECMASCRIPT3
        Constructor<?> configCtor = Config.class.getDeclaredConstructor(
                LanguageMode.class, boolean.class, boolean.class);
        configCtor.setAccessible(true);
        config = (Config) configCtor.newInstance(LanguageMode.ECMASCRIPT3, false, false);
        Node root = parseTransform("var o = {get x() { return 1; }};");
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("getters")));
    }

    @Test
    public void testTransform_setterInEs3_emitsError() throws Exception {
        Constructor<?> configCtor = Config.class.getDeclaredConstructor(
                LanguageMode.class, boolean.class, boolean.class);
        configCtor.setAccessible(true);
        config = (Config) configCtor.newInstance(LanguageMode.ECMASCRIPT3, false, false);
        Node root = parseTransform("var o = {set x(v) {}};");
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("setters")));
    }

    @Test
    public void testTransform_deleteNonProperty_emitsError() throws Exception {
        Node root = parseTransform("delete x;");
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Invalid delete operand")));
    }

    @Test
    public void testTransform_invalidIncrementTarget_emitsError() throws Exception {
        Node root = parseTransform("1++;");
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("invalid increment target")));
    }

    @Test
    public void testTransform_forEachLoop_emitsError() throws Exception {
        Node root = parseTransform("for each (var x in {});");
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("for each")));
    }

    @Test
    public void testTransform_withStatement_returnsWithNode() throws Exception {
        Node root = parseTransform("with (x) {}");
        Node withNode = root.getFirstChild();
        assertEquals(Token.WITH, withNode.getType());
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testTransform_stringWithVerticalTab_setsSlashVProperty() throws Exception {
        Node root = parseTransform("var s = 'a\\vb';");
        Node varNode = root.getFirstChild();
        assertEquals(Token.VAR, varNode.getType());
        Node nameNode = varNode.getFirstChild();
        assertEquals(Token.NAME, nameNode.getType());
        Node strNode = nameNode.getFirstChild();
        assertNotNull(strNode);
        assertEquals(Token.STRING, strNode.getType());
        assertTrue(strNode.getBooleanProp(Node.SLASH_V));
    }

    @Test
    public void testTransform_blockCommentWithAnnotation_warning() throws Exception {
        Node root = parseTransform("/** not jsdoc */\n/* @foo */ var x;");
        assertFalse(warnings.isEmpty());
        assertTrue(warnings.stream().anyMatch(w -> w.contains("Non-JSDoc comment has annotations")));
    }

    @Test
    public void testTransform_jsDocComment_attachesJSDocInfo() throws Exception {
        Node root = parseTransform("/** @param {number} x */ function f(x) {}");
        Node funcNode = root.getFirstChild();
        JSDocInfo info = funcNode.getJSDocInfo();
        assertNotNull(info);
    }

    @Test
    public void testTransform_fileOverviewJsDoc_attachedToRoot() throws Exception {
        Node root = parseTransform("/** @fileoverview Description */ var x;");
        JSDocInfo rootInfo = root.getJSDocInfo();
        assertNotNull(rootInfo);
    }

    @Test
    public void testTransform_forLoop_normal() throws Exception {
        Node root = parseTransform("for(var i=0; i<10; i++) {}");
        Node forNode = root.getFirstChild();
        assertEquals(Token.FOR, forNode.getType());
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testTransform_tryCatchFinally_normal() throws Exception {
        Node root = parseTransform("try { } catch(e) { } finally { }");
        Node tryNode = root.getFirstChild();
        assertEquals(Token.TRY, tryNode.getType());
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testTransform_returnWithValue() throws Exception {
        Node root = parseTransform("function f() { return 1; }");
        Node func = root.getFirstChild();
        Node body = func.getChildAtIndex(2);
        Node returnNode = body.getFirstChild();
        assertEquals(Token.RETURN, returnNode.getType());
        assertNotNull(returnNode.getFirstChild());
    }

    @Test
    public void testTransform_objectLiteral_normal() throws Exception {
        Node root = parseTransform("var o = {a: 1};");
        Node varNode = root.getFirstChild();
        Node objectLit = varNode.getFirstChild().getFirstChild();
        assertEquals(Token.OBJECTLIT, objectLit.getType());
        assertEquals(1, objectLit.getChildCount());
        Node prop = objectLit.getFirstChild();
        assertEquals(Token.STRING, prop.getType());
        assertEquals("a", prop.getString());
        Node value = prop.getFirstChild();
        assertEquals(Token.NUMBER, value.getType());
        assertEquals(1.0, value.getDouble(), 0.0);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testTransform_setterInEs5_noError() throws Exception {
        // Config already set to ES5
        Node root = parseTransform("var o = {set x(v) {}};");
        assertTrue(errors.isEmpty());
    }
}