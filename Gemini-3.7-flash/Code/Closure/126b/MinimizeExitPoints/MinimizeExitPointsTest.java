package com.google.javascript.jscomp;

import org.junit.Test;

public class MinimizeExitPointsTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new MinimizeExitPoints(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests defect 126: finally block exits should not be removed
  @Test
  public void testTryMinimizeExits_finallyBlockWithReturn_doesNotRemoveReturn() {
    testSame("function f() { try { throw 'error'; } finally { return; } }");
  }

  // Tests defect 126: if-return inside finally block must not be transformed
  @Test
  public void testTryMinimizeExits_finallyBlockWithIfReturn_doesNotMinimize() {
    testSame("function f() { try { throw 9; } finally { if (x) return; foo(); } }");
  }

  // Tests removal of redundant return at the end of a function
  @Test
  public void testTryMinimizeExits_functionTrailingReturn_removesRedundantReturn() {
    test("function f() { foo(); return; }", "function f() { foo(); }");
  }

  // Tests that returns with expression values are preserved
  @Test
  public void testTryMinimizeExits_returnWithValue_preservesReturn() {
    testSame("function f() { if (x) return 1; foo(); }");
  }

  // Tests minimization of if-return inside a function
  @Test
  public void testTryMinimizeExits_functionIfReturn_movesFollowingCodeToElse() {
    test("function f() { if (x) return; foo(); }",
         "function f() { if (x); else { foo(); } }");
  }

  // Tests minimization with existing if-else branches
  @Test
  public void testTryMinimizeExits_ifElseWithReturn_movesFollowingCodeToElse() {
    test("function f() { if (x) return; else bar(); foo(); }",
         "function f() { if (x); else { bar(); foo(); } }");
  }

  // Tests while loop with continue exit
  @Test
  public void testTryMinimizeExits_whileLoopContinue_minimizesLoopBody() {
    test("while (x) { if (y) continue; foo(); }",
         "while (x) { if (y); else { foo(); } }");
  }

  // Tests for loop with continue exit
  @Test
  public void testTryMinimizeExits_forLoopContinue_minimizesLoopBody() {
    test("for (var i = 0; i < 10; i++) { if (x) continue; foo(); }",
         "for (var i = 0; i < 10; i++) { if (x); else { foo(); } }");
  }

  // Tests do-while loop with false condition allowing break minimization
  @Test
  public void testTryMinimizeExits_doWhileFalseConditionBreak_minimizesBreak() {
    test("do { if (x) break; foo(); } while (false);",
         "do { if (x); else { foo(); } } while (false);");
  }

  // Tests do-while loop continue exit
  @Test
  public void testTryMinimizeExits_doWhileContinue_minimizesLoopBody() {
    test("do { if (x) continue; foo(); } while (true);",
         "do { if (x); else { foo(); } } while (true);");
  }

  // Tests labeled block with matching break exit
  @Test
  public void testTryMinimizeExits_labeledBlockBreak_minimizesLabeledBlock() {
    test("a: { if (x) break a; foo(); }",
         "a: { if (x); else { foo(); } }");
  }

  // Tests labeled block with non-matching break label
  @Test
  public void testTryMinimizeExits_labeledBlockMismatchedBreak_preservesBreak() {
    testSame("a: { b: { if (x) break a; foo(); } }");
  }

  // Tests try and catch blocks exit minimization
  @Test
  public void testTryMinimizeExits_tryCatchBlocks_minimizesTryAndCatch() {
    test("function f() { try { if (x) return; foo(); } catch (e) { if (y) return; bar(); } }",
         "function f() { try { if (x); else { foo(); } } catch (e) { if (y); else { bar(); } } }");
  }

  // Tests function declaration hoisting when moving following siblings
  @Test
  public void testTryMinimizeExits_functionDeclaration_hoistsToFrontOfBlock() {
    test("function f() { if (x) return; foo(); function g() {} }",
         "function f() { if (x); else { function g() {} foo(); } }");
  }

  // Tests multiple consecutive if-exits in a single pass
  @Test
  public void testTryMinimizeExits_multipleIfExits_nestsElseBlocks() {
    test("function f() { if (x) return; if (y) return; foo(); }",
         "function f() { if (x); else { if (y); else { foo(); } } }");
  }

  // Tests removal of a lone return statement in a function
  @Test
  public void testTryMinimizeExits_functionOnlyReturn_removesReturn() {
    test("function f() { return; }", "function f() {}");
  }

  // Tests removal of trailing return inside if-statement at the end of a function
  @Test
  public void testTryMinimizeExits_trailingReturnInIfBranch_removesReturn() {
    test("function f() { if (x) { foo(); return; } }",
         "function f() { if (x) { foo(); } }");
  }

  // Tests removal of trailing returns from both if and else branches at the end of a function
  @Test
  public void testTryMinimizeExits_trailingReturnsInIfAndElseBranches_removesBothReturns() {
    test("function f() { if (x) { foo(); return; } else { bar(); return; } }",
         "function f() { if (x) { foo(); } else { bar(); } }");
  }

  // Tests if-else exit where else contains return, moving following code into then-branch
  @Test
  public void testTryMinimizeExits_ifElseWithElseReturn_movesFollowingCodeToThen() {
    test("function f() { if (x) {} else return; foo(); }",
         "function f() { if (x) { foo(); } }");
  }

  // Tests variable declaration hoisting when moving statements to else branch
  @Test
  public void testTryMinimizeExits_varDeclaration_hoistsVarDeclaration() {
    test("function f() { if (x) return; var a = 1; foo(a); }",
         "function f() { var a; if (x); else { a = 1; foo(a); } }");
  }

  // Tests removal of redundant trailing continue in a while loop
  @Test
  public void testTryMinimizeExits_whileLoopTrailingContinue_removesContinue() {
    test("while (x) { foo(); continue; }", "while (x) { foo(); }");
  }

  // Tests removal of redundant trailing continue in a for loop
  @Test
  public void testTryMinimizeExits_forLoopTrailingContinue_removesContinue() {
    test("for (var i = 0; i < 10; i++) { foo(); continue; }",
         "for (var i = 0; i < 10; i++) { foo(); }");
  }

  // Tests removal of redundant trailing continue in a do-while loop
  @Test
  public void testTryMinimizeExits_doWhileTrailingContinue_removesContinue() {
    test("do { foo(); continue; } while (x);", "do { foo(); } while (x);");
  }

  // Tests removal of redundant trailing break in a labeled block
  @Test
  public void testTryMinimizeExits_labeledBlockTrailingBreak_removesBreak() {
    test("a: { foo(); break a; }", "a: { foo(); }");
  }

  // Tests removal of return as a direct child of if statement without block at function end
  @Test
  public void testTryMinimizeExits_ifReturnNoBlockAtFunctionEnd_removesReturn() {
    test("function f() { if (x) return; }", "function f() { if (x); }");
  }
}