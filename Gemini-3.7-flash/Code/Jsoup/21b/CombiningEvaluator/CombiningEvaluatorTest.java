package org.jsoup.select;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class CombiningEvaluatorTest {

    private Element root;
    private Element node;

    private Evaluator trueEval;
    private Evaluator falseEval;

    @Before
    public void setUp() {
        root = new Element(Tag.valueOf("div"), "");
        node = new Element(Tag.valueOf("p"), "");

        trueEval = new Evaluator() {
            @Override
            public boolean matches(Element r, Element n) {
                return true;
            }

            @Override
            public String toString() {
                return "true";
            }
        };

        falseEval = new Evaluator() {
            @Override
            public boolean matches(Element r, Element n) {
                return false;
            }

            @Override
            public String toString() {
                return "false";
            }
        };
    }

    // Tests And evaluator with all evaluators returning true
    @Test
    public void testAndMatches_allTrue_returnsTrue() {
        CombiningEvaluator.And and = new CombiningEvaluator.And(trueEval, trueEval);
        assertTrue(and.matches(root, node));
    }

    // Tests And evaluator when one evaluator returns false
    @Test
    public void testAndMatches_oneFalse_returnsFalse() {
        CombiningEvaluator.And and = new CombiningEvaluator.And(trueEval, falseEval);
        assertFalse(and.matches(root, node));
    }

    // Tests And evaluator when first evaluator returns false to verify short-circuit
    @Test
    public void testAndMatches_firstFalse_returnsFalse() {
        CombiningEvaluator.And and = new CombiningEvaluator.And(falseEval, trueEval);
        assertFalse(and.matches(root, node));
    }

    // Tests And evaluator with empty evaluator collection
    @Test
    public void testAndMatches_emptyEvaluators_returnsTrue() {
        CombiningEvaluator.And and = new CombiningEvaluator.And(Collections.<Evaluator>emptyList());
        assertTrue(and.matches(root, node));
    }

    // Tests And toString joining evaluators with space
    @Test
    public void testAndToString_multipleEvaluators_returnsJoinedString() {
        CombiningEvaluator.And and = new CombiningEvaluator.And(trueEval, falseEval);
        assertEquals("true false", and.toString());
    }

    // Tests Or constructor with empty list
    @Test
    public void testOrConstructor_emptyCollection_evaluatorsEmpty() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Collections.<Evaluator>emptyList());
        assertEquals(0, or.evaluators.size());
        assertFalse(or.matches(root, node));
    }

    // Tests Or constructor with single evaluator
    @Test
    public void testOrConstructor_singleEvaluator_evaluatorsAddedDirectly() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Collections.singletonList(trueEval));
        assertEquals(1, or.evaluators.size());
        assertTrue(or.matches(root, node));
    }

    // Tests Or constructor with multiple evaluators wrapped in an And evaluator
    @Test
    public void testOrConstructor_multipleEvaluators_wrapsInAnd() {
        List<Evaluator> list = Arrays.asList(trueEval, falseEval);
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(list);
        assertEquals(1, or.evaluators.size());
        assertTrue(or.evaluators.get(0) instanceof CombiningEvaluator.And);
        assertFalse(or.matches(root, node));
    }

    // Tests Or add method adding subsequent evaluator clauses
    @Test
    public void testOrAdd_addEvaluator_addsToEvaluatorList() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Collections.singletonList(falseEval));
        or.add(trueEval);
        assertEquals(2, or.evaluators.size());
        assertTrue(or.matches(root, node));
    }

    // Tests Or matches when all evaluators return false
    @Test
    public void testOrMatches_allFalse_returnsFalse() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Collections.singletonList(falseEval));
        or.add(falseEval);
        assertFalse(or.matches(root, node));
    }

    // Tests Or toString formatting
    @Test
    public void testOrToString_formatsCorrectly() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Collections.singletonList(trueEval));
        assertEquals(":or[true]", or.toString());
    }

    // Tests custom CombiningEvaluator subclass default constructor
    @Test
    public void testCombiningEvaluator_defaultConstructor_initializesEmptyList() {
        CombiningEvaluator custom = new CombiningEvaluator() {
            @Override
            public boolean matches(Element r, Element n) {
                return false;
            }
        };
        assertNotNull(custom.evaluators);
        assertEquals(0, custom.evaluators.size());
    }

    // Tests custom CombiningEvaluator subclass collection constructor
    @Test
    public void testCombiningEvaluator_collectionConstructor_initializesWithCollection() {
        List<Evaluator> list = new ArrayList<Evaluator>();
        list.add(trueEval);
        CombiningEvaluator custom = new CombiningEvaluator(list) {
            @Override
            public boolean matches(Element r, Element n) {
                return false;
            }
        };
        assertEquals(1, custom.evaluators.size());
        assertEquals(trueEval, custom.evaluators.get(0));
    }

    // Tests Or default no-arg constructor
    @Test
    public void testOr_defaultConstructor_emptyEvaluators() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or();
        assertEquals(0, or.evaluators.size());
        assertFalse(or.matches(root, node));
    }

    // Tests rightMostEvaluator when empty
    @Test
    public void testRightMostEvaluator_emptyEvaluators_returnsNull() {
        CombiningEvaluator.And and = new CombiningEvaluator.And(Collections.<Evaluator>emptyList());
        assertNull(and.rightMostEvaluator());
    }

    // Tests rightMostEvaluator when evaluators present
    @Test
    public void testRightMostEvaluator_withEvaluators_returnsLastEvaluator() {
        CombiningEvaluator.And and = new CombiningEvaluator.And(trueEval, falseEval);
        assertSame(falseEval, and.rightMostEvaluator());
    }

    // Tests replaceRightMostEvaluator
    @Test
    public void testReplaceRightMostEvaluator_replacesLastEvaluator() {
        CombiningEvaluator.And and = new CombiningEvaluator.And(trueEval, falseEval);
        assertSame(falseEval, and.rightMostEvaluator());
        and.replaceRightMostEvaluator(trueEval);
        assertSame(trueEval, and.rightMostEvaluator());
        assertTrue(and.matches(root, node));
    }

    // Tests Or toString with multiple evaluators
    @Test
    public void testOrToString_multipleEvaluators_formatsWithCommaSeparated() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Collections.singletonList(trueEval));
        or.add(falseEval);
        assertEquals(":or[true, false]", or.toString());
    }
}