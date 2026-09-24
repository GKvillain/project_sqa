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

  // Tests removal of redundant return at end of function
  @Test
  public void testProcess_redundantReturnAtFunctionEnd_removesReturn() {
    test("function f() { x(); return; }",
         "function f() { x(); }");
  }

  // Tests return with value is preserved
  @Test
  public void testProcess_returnWithValue_preservesReturn() {
    testSame("function f() { return x; }");
  }

  // Tests moving statements following an if-return into else block
  @Test
  public void testProcess_ifWithReturn_movesFollowingStatementsToElse() {
    test("function f() { if (x) return; foo(); bar(); }",
         "function f() { if (x); else { foo(); bar(); } }");
  }

  // Tests if-else where true block has return
  @Test
  public void testProcess_ifElseWithReturnInTrueBlock_movesFollowingStatements() {
    test("function f() { if (x) return; else foo(); bar(); }",
         "function f() { if (x); else { foo(); bar(); } }");
  }

  // Tests if-else where false block has return
  @Test
  public void testProcess_ifElseWithReturnInFalseBlock_movesFollowingStatements() {
    test("function f() { if (x) foo(); else return; bar(); }",
         "function f() { if (x) { foo(); bar(); } else; }");
  }

  // Tests minimization inside while loops with continue
  @Test
  public void testProcess_whileLoopWithContinue_movesFollowingStatements() {
    test("while (x) { if (y) continue; foo(); }",
         "while (x) { if (y); else { foo(); } }");
  }

  // Tests minimization inside for loops with continue
  @Test
  public void testProcess_forLoopWithContinue_movesFollowingStatements() {
    test("for (;x;) { if (y) continue; foo(); }",
         "for (;x;) { if (y); else { foo(); } }");
  }

  // Tests do-while with false condition optimizing breaks
  @Test
  public void testProcess_doWhileFalseConditionWithBreak_optimizesBreak() {
    test("do { if (x) break; foo(); } while (false);",
         "do { if (x); else { foo(); } } while (false);");
  }

  // Tests labeled break statement optimization
  @Test
  public void testProcess_labeledBlockWithBreak_optimizesLabeledBreak() {
    test("LABEL: { if (x) break LABEL; foo(); }",
         "LABEL: { if (x); else { foo(); } }");
  }

  // Tests labeled break with non-matching label is not removed
  @Test
  public void testProcess_mismatchedLabelBreak_preservesBreak() {
    testSame("OTHER: { LABEL: { if (x) break OTHER; foo(); } }");
  }

  // Tests minimization inside try and catch blocks
  @Test
  public void testProcess_tryCatchBlocks_minimizesTryAndCatchExits() {
    test("function f() { try { if (x) return; foo(); } catch (e) { if (y) return; bar(); } }",
         "function f() { try { if (x); else foo(); } catch (e) { if (y); else bar(); } }");
  }

  // Tests finally block exits are not minimized to preserve completion type (Defects4J Closure-126)
  @Test
  public void testProcess_finallyBlockWithReturn_doesNotMinimizeFinallyExit() {
    testSame("function f() { try { foo(); } finally { if (x) return; bar(); } }");
  }

  // Tests function declarations following if are hoisted to front of destination block
  @Test
  public void testProcess_functionDeclarationSibling_hoistedToFront() {
    test("function f() { if (x) return; foo(); function g() {} }",
         "function f() { if (x); else { function g() {} foo(); } }");
  }

  // Tests multiple sequential if exits conversion
  @Test
  public void testProcess_multipleSequentialIfExits_nestsElseBlocks() {
    test("function f() { if (x) return; if (y) return; foo(); }",
         "function f() { if (x); else { if (y); else { foo(); } } }");
  }

  // Tests empty block or function remains unchanged
  @Test
  public void testProcess_emptyFunction_noChange() {
    testSame("function f() {}");
  }
}