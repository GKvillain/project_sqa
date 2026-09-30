package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Collections;
import java.util.List;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;

public class FlowSensitiveInlineVariablesTest {

    private String runPass(String code) {
        // Prepare externs with helper functions to avoid undefined errors
        String externsCode = "function print(x) {}; function foo() {}; function someFunction() {};";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        // Use whitespace-only compilation to avoid other optimizations
        com.google.javascript.jscomp.CompilationLevel.WHITESPACE_ONLY.apply(options);
        // Disable all other checks and optimizations
        options.setChecksOnly(false);
        compiler.init(
                Collections.singletonList(SourceFile.fromCode("externs.js", externsCode)),
                Collections.singletonList(SourceFile.fromCode("test.js", code)),
                options);
        compiler.compile();
        Node root = compiler.getRoot();
        // Run FlowSensitiveInlineVariables pass
        FlowSensitiveInlineVariables pass = new FlowSensitiveInlineVariables(compiler);
        pass.process(null, root);
        return compiler.toSource();
    }

    @Test
    public void testInlineVar_simple_noSideEffects_inlinesSuccessfully() {
        String input = "function f() { var a = 1; print(a); }";
        String output = runPass(input);
        // Expect print(1) and no var a
        assertTrue("Should have inlined to print(1)", output.contains("print(1)"));
        assertFalse("Should not contain var a", output.contains("var a"));
    }

    @Test
    public void testInlineVar_rhsHasSideEffects_doesNotInline() {
        String input = "function f() { var a = foo(); print(a); }";
        String output = runPass(input);
        // Should keep print(a)
        assertTrue("Should keep print(a)", output.contains("print(a)"));
        assertFalse("Should not have print(foo())", output.contains("print(foo())"));
    }

    @Test
    public void testInlineVar_multipleUses_doesNotInline() {
        String input = "function f() { var a = 1; print(a); print(a); }";
        String output = runPass(input);
        assertTrue("Should keep print(a)", output.contains("print(a)"));
        assertTrue("Should have two print(a) calls", output.contains("print(a);print(a)") || output.contains("print(a); print(a)"));
    }

    @Test
    public void testInlineVar_parameter_doesNotInline() {
        String input = "function f(x) { print(x); }";
        String output = runPass(input);
        // Should remain unchanged
        assertTrue("Should keep print(x)", output.contains("print(x)"));
    }

    @Test
    public void testInlineVar_useInsideLoop_doesNotInline() {
        String input = "function f() { var a = 1; while(true) { print(a); break; } }";
        String output = runPass(input);
        assertTrue("Should keep print(a)", output.contains("print(a)"));
    }

    // Bug detection: definition inside loop, use outside loop
    // Buggy version will inline (incorrectly), fixed version will not
    @Test
    public void testInlineVar_definitionInsideLoop_useOutside_doesNotInline() {
        String input = "function f() { for(var i=0;i<3;i++) { var x = i; } print(x); }";
        String output = runPass(input);
        // Should keep print(x) because definition is inside loop
        assertTrue("Should keep print(x) (definition inside loop)", output.contains("print(x)"));
        assertFalse("Should not have print(i)", output.contains("print(i)"));
    }

    @Test
    public void testInlineVar_sideEffectBetweenDefAndUse_doesNotInline() {
        String input = "function f() { var a = 1; someFunction(); print(a); }";
        String output = runPass(input);
        assertTrue("Should keep print(a) due to side effect between", output.contains("print(a)"));
    }

    @Test
    public void testInlineVar_rhsContainsGetProp_doesNotInline() {
        String input = "function f() { var a = obj.prop; print(a); }";
        String output = runPass(input);
        assertTrue("Should keep print(a)", output.contains("print(a)"));
    }

    @Test
    public void testInlineVar_rhsContainsArrayLit_doesNotInline() {
        String input = "function f() { var a = [1,2]; print(a); }";
        String output = runPass(input);
        assertTrue("Should keep print(a)", output.contains("print(a)"));
    }

    @Test
    public void testInlineVar_exportedName_doesNotInline() {
        // Use a name ending with _ as exported convention
        String input = "function f() { var a_ = 1; print(a_); }";
        String output = runPass(input);
        assertTrue("Should keep print(a_) for exported variable", output.contains("print(a_)"));
    }

    @Test
    public void testInlineVar_dependsOnOuterScopeVar_doesNotInline() {
        // Variable a depends on outer variable b
        String input = "var b = 1; function f() { var a = b; print(a); }";
        String output = runPass(input);
        assertTrue("Should keep print(a) because it depends on outer var", output.contains("print(a)"));
    }

    @Test
    public void testInlineVar_rhsIsAssignmentExprStmt_inlinesSuccessfully() {
        // var a = 1; then b = a; print(b);
        // a will be inlined into b = 1
        String input = "function f() { var a = 1; var b = a; print(b); }";
        String output = runPass(input);
        assertTrue("Should remove var a", !output.contains("var a") || output.contains("var b = 1"));
        assertTrue("Should have print(b)", output.contains("print(b)"));
    }
}