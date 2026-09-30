package com.google.javascript.jscomp;

import com.google.common.collect.Sets;
import com.google.javascript.rhino.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Unit tests for {@link RenameVars}.
 */
public class RenameVarsTest extends CompilerTestCase {

  private String prefix = "";
  private boolean localRenamingOnly = false;
  private boolean preserveAnonymousFunctionNames = false;
  private boolean generatePseudoNames = false;
  private VariableMap prevUsedRenameMap = null;
  private char[] reservedCharacters = null;
  private Set<String> reservedNames = null;

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new RenameVars(
        compiler,
        prefix,
        localRenamingOnly,
        preserveAnonymousFunctionNames,
        generatePseudoNames,
        prevUsedRenameMap,
        reservedCharacters,
        reservedNames);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    prefix = "";
    localRenamingOnly = false;
    preserveAnonymousFunctionNames = false;
    generatePseudoNames = false;
    prevUsedRenameMap = null;
    reservedCharacters = null;
    reservedNames = null;
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests basic global and local variable renaming
  public void testRenameSimple_globalAndLocalVars_renamedDeterministically() {
    test("var foo = 1; function bar(baz) { var qux = baz + foo; return qux; }",
         "var a = 1; function b(c) { var d = c + a; return d; }");
  }

  // Tests local renaming only flag
  public void testRename_localRenamingOnly_preservesGlobalNames() {
    localRenamingOnly = true;
    test("var foo = 1; function bar(baz) { var qux = baz + foo; return qux; }",
         "var foo = 1; function bar(a) { var b = a + foo; return b; }");
  }

  // Tests prefix for global variable renaming
  public void testRename_withPrefix_prependsPrefixToGlobalVars() {
    prefix = "L_";
    test("var foo = 1; foo++; function bar(baz) { return baz; }",
         "var L_a = 1; L_a++; function L_b(a) { return a; }");
  }

  // Tests pseudo names generation for debugging
  public void testRename_generatePseudoNames_generatesDebuggingNames() {
    generatePseudoNames = true;
    test("var foo = 1; function bar(baz) { return baz + foo; }",
         "var $foo$$ = 1; function $bar$$($baz$$) { return $baz$$ + $foo$$; }");
  }

  // Tests reserving specific variable names
  public void testRename_reservedNames_skipsReservedNames() {
    reservedNames = Sets.newHashSet("a", "b");
    test("var foo = 1; var bar = 2;",
         "var c = 1; var d = 2;");
  }

  // Tests reserving characters in variable names
  public void testRename_reservedCharacters_avoidsCharacters() {
    reservedCharacters = new char[]{'a', 'b'};
    test("var foo = 1; var bar = 2;",
         "var c = 1; var d = 2;");
  }

  // Tests reusing previously used variable map
  public void testRename_prevUsedRenameMap_reusesPreviousAssignments() {
    Map<String, String> prevMap = new HashMap<String, String>();
    prevMap.put("foo", "z");
    prevUsedRenameMap = new VariableMap(prevMap);

    test("var foo = 1; var bar = 2;",
         "var z = 1; var a = 2;");
  }

  // Tests preserving anonymous function names
  public void testRename_preserveAnonymousFunctionNames_preservesNames() {
    preserveAnonymousFunctionNames = true;
    test("var f = function foo() {};",
         "var a = function foo() {};");
  }

  // Tests externs conflict avoidance
  public void testRename_withExterns_avoidsExternCollision() {
    test("var extVar;",
         "var foo = 1; var extVar = 2;",
         "var a = 1; extVar = 2;");
  }

  // Tests frequency-based renaming prioritization
  public void testRename_frequencyPrioritization_assignsShorterNamesToFrequentVars() {
    test("var rare = 1; var freq = 2; freq++; freq++; freq++;",
         "var b = 1; var a = 2; a++; a++; a++;");
  }

  // Tests variable reuse across different local scopes
  public void testRename_multipleLocalScopes_reusesLocalNames() {
    test("function f1(a1, b1) { return a1 + b1; } function f2(a2, b2) { return a2 + b2; }",
         "function c(a, b) { return a + b; } function d(a, b) { return a + b; }");
  }

  // Tests interaction of localRenamingOnly and preserveAnonymousFunctionNames (Bug 136 regression)
  public void testRename_localRenamingOnlyAndPreserveAnonFuncs_behavesCorrectly() {
    localRenamingOnly = true;
    preserveAnonymousFunctionNames = true;
    test("var foo = function anon() { var bar = 1; return bar; };",
         "var foo = function anon() { var a = 1; return a; };");
  }

  // Tests bleeding function expressions
  public void testBleedingFunctionExpression() {
    test("var f = function foo() { return foo(); };",
         "var a = function b() { return b(); };");
  }

  // Tests shadowed variables across scopes
  public void testShadowedVariables() {
    test("var a = 1; function f() { var a = 2; return a; }",
         "var a = 1; function b() { var c = 2; return c; }");
  }

  // Tests catch block variables
  public void testCatchBlockVariables() {
    test("try { var x = 1; } catch (e) { var y = e; }",
         "try { var a = 1; } catch (b) { var c = b; }");
  }

  // Tests nested functions scoping
  public void testNestedFunctions() {
    test("function f1() { var a = 1; function f2() { var b = 2; return a + b; } return f2(); }",
         "function c() { var d = 1; function a() { var b = 2; return d + b; } return a(); }");
  }

  // Tests renaming with lots of variables to test multi-character generated names
  public void testLotsOfVariables() {
    StringBuilder js = new StringBuilder();
    for (int i = 0; i < 50; i++) {
      js.append("var var_").append(i).append(" = ").append(i).append(";");
    }
    testSame(js.toString());
  }

  // Tests pseudo names with prefix
  public void testPseudoNamesWithPrefix() {
    generatePseudoNames = true;
    prefix = "pre_";
    test("var foo = 1; function bar(baz) { return baz + foo; }",
         "var pre_$foo$$ = 1; function pre_$bar$$(pre_$baz$$) { return pre_$baz$$ + pre_$foo$$; }");
  }

  // Tests localRenamingOnly with prefix
  public void testLocalRenamingOnlyWithPrefix() {
    localRenamingOnly = true;
    prefix = "pre_";
    test("var foo = 1; function bar(baz) { var qux = 2; return baz + qux; }",
         "var foo = 1; function bar(pre_a) { var pre_b = 2; return pre_a + pre_b; }");
  }
}