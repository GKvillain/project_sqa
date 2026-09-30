package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.DiagnosticGroups;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.Result;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class TypeInferenceTest {
    private Compiler compiler;
    private CompilerOptions options;
    private List<JSSourceFile> externs;
    private JSTypeRegistry registry;

    @Before
    public void setUp() {
        compiler = new Compiler();
        options = new CompilerOptions();
        options.setCheckTypes(true);
        options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.OFF);
        externs = Collections.<JSSourceFile>emptyList();
        registry = compiler.getTypeRegistry();
    }

    private Node compileAndGetScript(String js) {
        Result result = compiler.compile(
                externs,
                Collections.<JSSourceFile>singletonList(JSSourceFile.fromCode("test.js", js)),
                options);
        assertTrue("Compilation failed: " + compiler.getErrors().length, result.success);
        // The root node contains two children: externs and main script
        return compiler.getRoot().getLastChild();
    }

    private Node findNameNode(Node root, String name) {
        if (root.isName() && name.equals(root.getString())) {
            return root;
        }
        for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
            Node found = findNameNode(child, name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    // Test for-in loop variable type is always string
    @Test
    public void testForIn_variable_typeString() {
        Node script = compileAndGetScript("var obj = {a:1}; for (var k in obj) {}");
        Node kNode = findNameNode(script, "k");
        assertNotNull("Name node 'k' not found", kNode);
        assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), kNode.getJSType());
    }

    // Test for-in over array still yields string (property names are strings)
    @Test
    public void testForIn_overArray_typeString() {
        Node script = compileAndGetScript("var arr = [1,2]; for (var k in arr) {}");
        Node kNode = findNameNode(script, "k");
        assertNotNull("Name node 'k' not found", kNode);
        assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), kNode.getJSType());
    }

    // Test var initialization infers the declared type
    @Test
    public void testAssign_varString_typeString() {
        Node script = compileAndGetScript("var x = 'hello';");
        Node xNode = findNameNode(script, "x");
        assertNotNull("Name node 'x' not found", xNode);
        assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), xNode.getJSType());
    }

    // Test property assignment infers type on object literal
    @Test
    public void testAssign_propertyInferred_typeNumber() {
        Node script = compileAndGetScript("var obj = {}; obj.x = 10;");
        // Find the GETPROP node for obj.x by searching for name "x" as the last child of GETPROP
        Node getPropNode = findGetPropNode(script, "x");
        assertNotNull("GETPROP node for 'x' not found", getPropNode);
        assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), getPropNode.getJSType());
    }

    // Test ternary (hook) expression produces union type
    @Test
    public void testTernary_unionType_numberOrString() {
        Node script = compileAndGetScript("var x = true ? 1 : 'a';");
        Node xNode = findNameNode(script, "x");
        assertNotNull("Name node 'x' not found", xNode);
        JSType expected = registry.createUnionType(
                registry.getNativeType(JSTypeNative.NUMBER_TYPE),
                registry.getNativeType(JSTypeNative.STRING_TYPE));
        assertEquals(expected, xNode.getJSType());
    }

    // Test addition of number and string yields string
    @Test
    public void testAdd_numberString_typeString() {
        Node script = compileAndGetScript("var x = 1 + 'a';");
        Node xNode = findNameNode(script, "x");
        assertNotNull("Name node 'x' not found", xNode);
        assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), xNode.getJSType());
    }

    // Test addition of two numbers yields number
    @Test
    public void testAdd_numberNumber_typeNumber() {
        Node script = compileAndGetScript("var x = 1 + 2;");
        Node xNode = findNameNode(script, "x");
        assertNotNull("Name node 'x' not found", xNode);
        assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), xNode.getJSType());
    }

    // Test call expression with function literal return type
    @Test
    public void testCall_literalFunction_typeNumber() {
        Node script = compileAndGetScript("var x = (function() { return 1; })();");
        Node xNode = findNameNode(script, "x");
        assertNotNull("Name node 'x' not found", xNode);
        assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), xNode.getJSType());
    }

    // Test property access (GETPROP) yields correct type
    @Test
    public void testGetProp_simple_typeNumber() {
        Node script = compileAndGetScript("var obj = {a:1}; var y = obj.a;");
        Node yNode = findNameNode(script, "y");
        assertNotNull("Name node 'y' not found", yNode);
        assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), yNode.getJSType());
    }

    // Test element access (GETELEM) on array yields number
    @Test
    public void testGetElem_array_typeNumber() {
        Node script = compileAndGetScript("var arr = [1,2]; var y = arr[0];");
        Node yNode = findNameNode(script, "y");
        assertNotNull("Name node 'y' not found", yNode);
        assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), yNode.getJSType());
    }

    // Test catch parameter is typed as unknown
    @Test
    public void testCatchParam_typeUnknown() {
        Node script = compileAndGetScript("try { throw 1; } catch(e) {}");
        Node eNode = findNameNode(script, "e");
        assertNotNull("Name node 'e' not found", eNode);
        assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), eNode.getJSType());
    }

    // Test assignment via += updates var type (number + string => string)
    @Test
    public void testAssignAdd_numberString_typeString() {
        Node script = compileAndGetScript("var x = 1; x += 'a';");
        Node xNode = findNameNode(script, "x");
        assertNotNull("Name node 'x' not found", xNode);
        // After x += 'a', type should be string
        assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), xNode.getJSType());
    }

    // Helper to find a GETPROP node with given property name
    private Node findGetPropNode(Node root, String propName) {
        if (root.isGetProp()) {
            Node propNode = root.getLastChild();
            if (propNode.isString() && propName.equals(propNode.getString())) {
                return root;
            }
        }
        for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
            Node found = findGetPropNode(child, propName);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}