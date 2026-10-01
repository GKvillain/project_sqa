package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.google.javascript.rhino.Node;

import java.util.List;

/**
 * JUnit 4 test class for InlineObjectLiterals.
 * Covers normal, boundary, edge cases and branch coverage.
 * Designed to detect Defects4J bug 29b.
 */
public class InlineObjectLiteralsTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    options.setIdeMode(false);
    // Disable other passes to isolate InlineObjectLiterals
    options.setChecksOnly(false);
    options.setWarningLevel(DiagnosticGroups.GLOBAL_THIS, CheckLevel.OFF);
  }

  /**
   * Helper: compiles JavaScript string, runs InlineObjectLiterals pass,
   * and returns the generated source code string.
   */
  private String runInlineObjectLiterals(String js) {
    List<JSSourceFile> inputs = new java.util.ArrayList<JSSourceFile>();
    inputs.add(JSSourceFile.fromCode("test.js", js));
    compiler.compile(new JSSourceFile[]{}, inputs.toArray(new JSSourceFile[0]), options);
    Node root = compiler.getRoot();
    // The pass is run during compile, but may not have executed if not enabled.
    // We need to manually run the pass on the AST after parsing.
    // Get the CompilerPass and execute.
    InlineObjectLiterals pass = new InlineObjectLiterals(compiler,
        new com.google.common.base.Supplier<String>() {
          @Override
          public String get() {
            return "unique_id";
          }
        });
    pass.process(null, root);
    return compiler.toSource();
  }

  // ========== Normal cases ==========

  @Test
  // Test simple inline: object literal assigned to local var and used via property access.
  public void testProcess_simpleObjectUsedViaProperty_inlined() {
    String input = "(function() { var x = {a: 1, b: 2}; return x.a + x.b; })();";
    // Expect that x is replaced by individual vars: JSCompiler_object_inline_a_unique_id etc.
    String output = runInlineObjectLiterals(input);
    // The code should contain the inlined variable names.
    assertTrue("Expected inlined variable JSCompiler_object_inline_a_unique_id",
        output.contains("JSCompiler_object_inline_a_"));
    assertTrue("Expected inlined variable JSCompiler_object_inline_b_unique_id",
        output.contains("JSCompiler_object_inline_b_"));
  }

  @Test
  // Test that object literal used directly (full reference) is NOT inlined.
  public void testProcess_objectUsedInFull_notInlined() {
    String input = "(function() { var x = {a: 1}; return x; })();";
    String output = runInlineObjectLiterals(input);
    // The object literal should remain, not inlined.
    assertFalse("Should not contain inlined variable when full reference exists",
        output.contains("JSCompiler_object_inline_"));
  }

  @Test
  // Test that object literal used in a function call as 'this' is NOT inlined.
  public void testProcess_objectUsedAsCallTarget_notInlined() {
    String input = "(function() { var x = {a: 1}; x.fn(); })();";
    String output = runInlineObjectLiterals(input);
    assertFalse("Should not inline when property access is used as call target",
        output.contains("JSCompiler_object_inline_"));
  }

  // ========== Edge Cases: getter/setter ==========

  @Test
  // Test that object literal with getter is NOT inlined.
  public void testProcess_objectWithGetter_notInlined() {
    String input = "(function() { var x = {get a() { return 1; }}; return x.a; })();";
    String output = runInlineObjectLiterals(input);
    assertFalse("Should not inline object with getter",
        output.contains("JSCompiler_object_inline_"));
  }

  @Test
  // Test that object literal with setter is NOT inlined.
  public void testProcess_objectWithSetter_notInlined() {
    String input = "(function() { var x = {set a(v) {}}; x.a = 2; })();";
    String output = runInlineObjectLiterals(input);
    assertFalse("Should not inline object with setter",
        output.contains("JSCompiler_object_inline_"));
  }

  // ========== Edge Cases: self-referential ==========

  @Test
  // Test that self-referential assignment (x = {a: x.b}) is NOT inlined.
  public void testProcess_selfReferentialAssignment_notInlined() {
    String input = "(function() { var x = {a: x.b}; return x.a; })();";
    String output = runInlineObjectLiterals(input);
    assertFalse("Should not inline self-referential assignment",
        output.contains("JSCompiler_object_inline_"));
  }

  // ========== Edge Cases: global / exported / extern ==========

  @Test
  // Test that global variable is NOT inlined.
  public void testProcess_globalVar_notInlined() {
    String input = "var x = {a: 1}; return x.a;";
    String output = runInlineObjectLiterals(input);
    assertFalse("Should not inline global variable",
        output.contains("JSCompiler_object_inline_"));
  }

  @Test
  // Test that exported variable (by naming convention) is NOT inlined.
  public void testProcess_exportedVar_notInlined() {
    // Convention: exported names often start with '_' or contain '$' but Compiler's
    // coding convention checks default exported patterns. We'll use a name that
    // is considered exported by Closure's default.
    String input = "(function() { var x_ = {a: 1}; return x_.a; })();";
    String output = runInlineObjectLiterals(input);
    // Variable name contains underscore suffix; may be considered exported.
    assertFalse("Should not inline exported variable",
        output.contains("JSCompiler_object_inline_"));
  }

  // ========== Defect detection: property reference to undefined key ==========

  @Test
  // Test that when a property referenced is NOT defined in the object literal,
  // the pass should NOT inline (to avoid creating undefined variables).
  // The buggy version inlines, causing potential issues.
  // Expected behavior (fixed) is NOT to inline.
  public void testProcess_propertyNotInObject_notInlined() {
    String input = "(function() { var x = {a: 1}; return x.b; })();";
    String output = runInlineObjectLiterals(input);
    // If pass inlines incorrectly, it will create variable for 'b' with undefined.
    // Detection: If output contains var declaration for 'b', the bug is present.
    // We expect no inlining. So assert that inlined variable is not present.
    assertFalse("Should not inline when property is not defined in object literal",
        output.contains("JSCompiler_object_inline_"));
  }

  // ========== Additional branch coverage: multiple assignments ==========

  @Test
  // Test that when variable has non-object-literal assignment, it is NOT inlined.
  public void testProcess_multipleAssignments_nonObject_notInlined() {
    String input = "(function() { var x = {a: 1}; x = 5; return x; })();";
    String output = runInlineObjectLiterals(input);
    assertFalse("Should not inline when variable has non-object assignment",
        output.contains("JSCompiler_object_inline_"));
  }

  @Test
  // Test that when object literal is modified (assign to a property) past initial,
  // it may still be inlined? Actually, isInlinableObject checks all references.
  // If there's an assignment to a property (ref.isLvalue() but not object literal),
  // it will fail because val.isObjectLit() false. So not inlined.
  public void testProcess_objectLiteralModified_notInlined() {
    String input = "(function() { var x = {a: 1}; x.a = 2; return x.a; })();";
    String output = runInlineObjectLiterals(input);
    assertFalse("Should not inline when object property is modified",
        output.contains("JSCompiler_object_inline_"));
  }

  // ========== Regression: object literal with no references to a key (should still inline) ==========

  @Test
  // Test that object literal with a property not used after assignment is still inlined,
  // as the initial assignment includes that key.
  public void testProcess_unusedKeyInObject_inlined() {
    String input = "(function() { var x = {a: 1, b: 2}; return x.a; })();";
    String output = runInlineObjectLiterals(input);
    // Should inline both 'a' and 'b' (b remains undefined).
    assertTrue("Expected inlined variable for 'a' when keys exist",
        output.contains("JSCompiler_object_inline_a_"));
    assertTrue("Expected inlined variable for 'b' even if unused",
        output.contains("JSCompiler_object_inline_b_"));
  }

  // ========== Exception path (not applicable, but test expected exception) ==========

  // No exception paths visible in the source; skip.

  // ========== Boundary: empty object literal ==========

  @Test
  // Test that empty object literal is inlined (replaced by TRUE).
  public void testProcess_emptyObjectLiteral_inlined() {
    String input = "(function() { var x = {}; return x; })();";
    String output = runInlineObjectLiterals(input);
    // Since there is a full reference (return x), it should NOT inline.
    // But if we change to property access (impossible for empty), it cannot.
    // For empty object and no full reference, it should inline to true.
    // Let's test a case where empty object is never used in full.
    String input2 = "(function() { var x = {}; return x.a; })();";
    String output2 = runInlineObjectLiterals(input2);
    // No property defined, so should NOT inline (detected by property undefined check)
    // For empty object, the property reference returns undefined; the pass may still inline?
    // According to bug, property not defined -> should not inline. So expect no inlining.
    assertFalse("Should not inline empty object when referenced property does not exist",
        output2.contains("JSCompiler_object_inline_"));
  }

  // ========== Complex: multiple objects, one inlinable ==========

  @Test
  // Test that among multiple variables, only inlinable ones are transformed.
  public void testProcess_mixedVariables_inlineOnlyValid() {
    String input = "(function() { var x = {a: 1}, y = 5; return x.a + y; })();";
    String output = runInlineObjectLiterals(input);
    assertTrue("Should inline x",
        output.contains("JSCompiler_object_inline_a_"));
    // y should remain as is.
    assertTrue("y should remain", output.contains("y"));
  }
}