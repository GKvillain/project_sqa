package org.jsoup.select;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Tag;
import java.util.Arrays;
import java.util.Collections;

public class CombiningEvaluatorTest {

    private Element root = new Element(Tag.valueOf("div"), "");
    private Element node = new Element(Tag.valueOf("span"), "");

    private Evaluator alwaysTrue() {
        return new Evaluator() {
            @Override
            public boolean matches(Element root, Element node) {
                return true;
            }
            @Override
            public String toString() {
                return "T";
            }
        };
    }

    private Evaluator alwaysFalse() {
        return new Evaluator() {
            @Override
            public boolean matches(Element root, Element node) {
                return false;
            }
            @Override
            public String toString() {
                return "F";
            }
        };
    }

    @Test
    public void testAndMatches_allTrue_returnsTrue() {
        CombiningEvaluator.And and = new CombiningEvaluator.And(Arrays.asList(alwaysTrue(), alwaysTrue()));
        assertTrue(and.matches(root, node));
    }

    @Test
    public void testAndMatches_oneFalse_returnsFalse() {
        CombiningEvaluator.And and = new CombiningEvaluator.And(Arrays.asList(alwaysTrue(), alwaysFalse()));
        assertFalse(and.matches(root, node));
    }

    @Test
    public void testAndMatches_emptyEvaluators_returnsTrue() {
        CombiningEvaluator.And and = new CombiningEvaluator.And(Collections.<Evaluator>emptyList());
        assertTrue(and.matches(root, node));
    }

    @Test
    public void testOrMatches_allFalse_returnsFalse() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Arrays.asList(alwaysFalse(), alwaysFalse()));
        assertFalse(or.matches(root, node));
    }

    @Test
    public void testOrMatches_oneTrue_returnsTrue() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Arrays.asList(alwaysFalse(), alwaysTrue()));
        assertTrue(or.matches(root, node));
    }

    @Test
    public void testOrConstructor_withSingleEvaluator_returnsCorrectMatches() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Arrays.asList(alwaysTrue()));
        assertTrue(or.matches(root, node));
    }

    @Test
    public void testOrConstructor_withMultipleEvaluators_returnsAndMatches() {
        // With more than one evaluator, they are ANDed together as first clause
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Arrays.asList(alwaysTrue(), alwaysFalse()));
        assertFalse(or.matches(root, node));
    }

    @Test
    public void testOrConstructor_withEmptyCollection_returnsFalse() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Collections.<Evaluator>emptyList());
        assertFalse(or.matches(root, node));
    }

    @Test
    public void testOrAdd_evaluator_changesResult() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Arrays.asList(alwaysFalse()));
        assertFalse(or.matches(root, node));
        or.add(alwaysTrue());
        assertTrue(or.matches(root, node));
    }

    @Test
    public void testAndToString_empty() {
        CombiningEvaluator.And and = new CombiningEvaluator.And(Collections.<Evaluator>emptyList());
        assertEquals("", and.toString());
    }

    @Test
    public void testAndToString_nonEmpty() {
        CombiningEvaluator.And and = new CombiningEvaluator.And(Arrays.asList(alwaysTrue(), alwaysFalse()));
        assertEquals("T F", and.toString());
    }

    @Test
    public void testOrToString_empty() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Collections.<Evaluator>emptyList());
        assertEquals(":or[]", or.toString());
    }

    @Test
    public void testOrToString_nonEmpty() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Arrays.asList(alwaysTrue(), alwaysFalse()));
        // With 2 evaluators, constructor wraps them into one And -> evaluators list has size 1
        assertEquals(":or[T, F]", or.toString());
    }

    @Test(expected = NullPointerException.class)
    public void testAndConstructor_nullCollection_throwsNullPointerException() {
        new CombiningEvaluator.And((java.util.Collection<Evaluator>) null);
    }

    @Test(expected = NullPointerException.class)
    public void testOrConstructor_nullCollection_throwsNullPointerException() {
        new CombiningEvaluator.Or((java.util.Collection<Evaluator>) null);
    }
}