package org.jsoup.select;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CombiningEvaluatorTest {

    private Element root;
    private Element element;

    private static class DummyEvaluator extends Evaluator {
        private final boolean matchResult;
        private final String name;

        DummyEvaluator(boolean matchResult, String name) {
            this.matchResult = matchResult;
            this.name = name;
        }

        @Override
        public boolean matches(Element root, Element element) {
            return matchResult;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Before
    public void setUp() {
        root = new Element(Tag.valueOf("div"), "");
        element = new Element(Tag.valueOf("p"), "");
        root.appendChild(element);
    }

    // Tests And matching when all sub-evaluators return true
    @Test
    public void testMatches_andAllTrue_returnsTrue() {
        Evaluator eval1 = new DummyEvaluator(true, "eval1");
        Evaluator eval2 = new DummyEvaluator(true, "eval2");
        CombiningEvaluator.And andEval = new CombiningEvaluator.And(eval1, eval2);

        assertTrue(andEval.matches(root, element));
    }

    // Tests And matching when at least one sub-evaluator returns false
    @Test
    public void testMatches_andOneFalse_returnsFalse() {
        Evaluator eval1 = new DummyEvaluator(true, "eval1");
        Evaluator eval2 = new DummyEvaluator(false, "eval2");
        CombiningEvaluator.And andEval = new CombiningEvaluator.And(eval1, eval2);

        assertFalse(andEval.matches(root, element));
    }

    // Tests And matching when evaluators list is empty
    @Test
    public void testMatches_andEmptyList_returnsTrue() {
        CombiningEvaluator.And andEval = new CombiningEvaluator.And(Collections.<Evaluator>emptyList());

        assertTrue(andEval.matches(root, element));
    }

    // Tests And toString joining evaluators with space
    @Test
    public void testToString_andMultipleEvaluators_returnsJoinedString() {
        Evaluator eval1 = new DummyEvaluator(true, "div");
        Evaluator eval2 = new DummyEvaluator(true, ".class");
        CombiningEvaluator.And andEval = new CombiningEvaluator.And(eval1, eval2);

        assertEquals("div .class", andEval.toString());
    }

    // Tests And constructor with varargs array
    @Test
    public void testConstructor_andVarargs_populatesEvaluatorsList() {
        Evaluator eval1 = new DummyEvaluator(true, "e1");
        Evaluator eval2 = new DummyEvaluator(true, "e2");
        CombiningEvaluator.And andEval = new CombiningEvaluator.And(eval1, eval2);

        assertEquals(2, andEval.evaluators.size());
        assertEquals(eval1, andEval.evaluators.get(0));
        assertEquals(eval2, andEval.evaluators.get(1));
    }

    // Tests Or constructor when given empty collection
    @Test
    public void testConstructor_orEmptyCollection_evaluatorsListIsEmpty() {
        CombiningEvaluator.Or orEval = new CombiningEvaluator.Or(Collections.<Evaluator>emptyList());

        assertEquals(0, orEval.evaluators.size());
        assertFalse(orEval.matches(root, element));
    }

    // Tests Or constructor when given a single evaluator
    @Test
    public void testConstructor_orSingleEvaluator_addsDirectlyWithoutWrapping() {
        Evaluator eval1 = new DummyEvaluator(true, "e1");
        CombiningEvaluator.Or orEval = new CombiningEvaluator.Or(Collections.singletonList(eval1));

        assertEquals(1, orEval.evaluators.size());
        assertEquals(eval1, orEval.evaluators.get(0));
        assertTrue(orEval.matches(root, element));
    }

    // Tests Or constructor when given multiple evaluators wraps them into And evaluator
    @Test
    public void testConstructor_orMultipleEvaluators_wrapsInAndEvaluator() {
        Evaluator eval1 = new DummyEvaluator(true, "e1");
        Evaluator eval2 = new DummyEvaluator(true, "e2");
        CombiningEvaluator.Or orEval = new CombiningEvaluator.Or(Arrays.asList(eval1, eval2));

        assertEquals(1, orEval.evaluators.size());
        assertTrue(orEval.evaluators.get(0) instanceof CombiningEvaluator.And);
        assertTrue(orEval.matches(root, element));
    }

    // Tests Or matches returns false when wrapped initial And evaluator fails
    @Test
    public void testMatches_orWrappedInitialAndEvaluatorFails_returnsFalse() {
        Evaluator eval1 = new DummyEvaluator(true, "e1");
        Evaluator eval2 = new DummyEvaluator(false, "e2");
        CombiningEvaluator.Or orEval = new CombiningEvaluator.Or(Arrays.asList(eval1, eval2));

        assertFalse(orEval.matches(root, element));
    }

    // Tests Or add method adds new clause
    @Test
    public void testAdd_orAddEvaluator_increasesSizeAndMatches() {
        Evaluator eval1 = new DummyEvaluator(false, "e1");
        CombiningEvaluator.Or orEval = new CombiningEvaluator.Or(Collections.singletonList(eval1));

        assertFalse(orEval.matches(root, element));

        Evaluator eval2 = new DummyEvaluator(true, "e2");
        orEval.add(eval2);

        assertEquals(2, orEval.evaluators.size());
        assertTrue(orEval.matches(root, element));
    }

    // Tests Or matches returns false when all clauses return false
    @Test
    public void testMatches_orAllClausesFalse_returnsFalse() {
        Evaluator eval1 = new DummyEvaluator(false, "e1");
        Evaluator eval2 = new DummyEvaluator(false, "e2");
        CombiningEvaluator.Or orEval = new CombiningEvaluator.Or(Collections.singletonList(eval1));
        orEval.add(eval2);

        assertFalse(orEval.matches(root, element));
    }

    // Tests Or toString format
    @Test
    public void testToString_orEvaluators_returnsFormattedString() {
        Evaluator eval1 = new DummyEvaluator(true, "div");
        Evaluator eval2 = new DummyEvaluator(true, "p");
        CombiningEvaluator.Or orEval = new CombiningEvaluator.Or(Collections.singletonList(eval1));
        orEval.add(eval2);

        assertEquals(":or[div, p]", orEval.toString());
    }

    // Tests rightMostEvaluator returns last evaluator when non-empty
    @Test
    public void testRightMostEvaluator_withEvaluators_returnsLastEvaluator() {
        Evaluator eval1 = new DummyEvaluator(true, "e1");
        Evaluator eval2 = new DummyEvaluator(true, "e2");
        CombiningEvaluator.And andEval = new CombiningEvaluator.And(eval1, eval2);

        assertSame(eval2, andEval.rightMostEvaluator());
    }

    // Tests rightMostEvaluator returns null when evaluators list is empty
    @Test
    public void testRightMostEvaluator_emptyEvaluators_returnsNull() {
        CombiningEvaluator.And andEval = new CombiningEvaluator.And(Collections.<Evaluator>emptyList());

        assertNull(andEval.rightMostEvaluator());
    }

    // Tests replaceRightMostEvaluator replaces the last evaluator in list
    @Test
    public void testReplaceRightMostEvaluator_replacesLastEvaluator() {
        Evaluator eval1 = new DummyEvaluator(true, "e1");
        Evaluator eval2 = new DummyEvaluator(true, "e2");
        Evaluator replacement = new DummyEvaluator(false, "replacement");
        CombiningEvaluator.And andEval = new CombiningEvaluator.And(eval1, eval2);

        andEval.replaceRightMostEvaluator(replacement);

        assertEquals(2, andEval.evaluators.size());
        assertSame(eval1, andEval.evaluators.get(0));
        assertSame(replacement, andEval.evaluators.get(1));
        assertSame(replacement, andEval.rightMostEvaluator());
    }

    // Tests Or no-arg constructor creates empty evaluator
    @Test
    public void testConstructor_orNoArgs_isEmpty() {
        CombiningEvaluator.Or orEval = new CombiningEvaluator.Or();

        assertEquals(0, orEval.evaluators.size());
        assertNull(orEval.rightMostEvaluator());
        assertFalse(orEval.matches(root, element));
    }

    // Tests Or varargs constructor
    @Test
    public void testConstructor_orVarargs_populatesEvaluatorsList() {
        Evaluator eval1 = new DummyEvaluator(true, "e1");
        Evaluator eval2 = new DummyEvaluator(true, "e2");
        CombiningEvaluator.Or orEval = new CombiningEvaluator.Or(eval1, eval2);

        assertEquals(1, orEval.evaluators.size());
        assertTrue(orEval.evaluators.get(0) instanceof CombiningEvaluator.And);
        assertTrue(orEval.matches(root, element));
    }
}