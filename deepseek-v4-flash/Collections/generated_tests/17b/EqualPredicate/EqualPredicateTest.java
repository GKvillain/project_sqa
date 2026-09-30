package org.apache.commons.collections.functors;

import static org.junit.Assert.*;

import org.apache.commons.collections.Predicate;
import org.junit.Test;

/**
 * JUnit 4 test class for EqualPredicate.
 * Targets Defects4J bug 17b.
 */
public class EqualPredicateTest {

    // ===== Factory method: equalPredicate(T) =====

    @Test
    // Tests factory with null input returns NullPredicate
    public void testEqualPredicate_factoryNull_returnsNullPredicate() {
        Predicate<String> pred = EqualPredicate.<String>equalPredicate(null);
        assertNotNull(pred);
        assertFalse(pred.evaluate("test"));
        assertFalse(pred.evaluate(null));
    }

    @Test
    // Tests factory with non-null input returns EqualPredicate and works correctly
    public void testEqualPredicate_factoryNonNull_returnsEqualPredicate() {
        Predicate<String> pred = EqualPredicate.equalPredicate("hello");
        assertNotNull(pred);
        assertTrue(pred instanceof EqualPredicate);
    }

    @Test
    // Tests evaluate true for equal objects using default equator (equals)
    public void testEvaluate_equalStrings_returnsTrue() {
        EqualPredicate<String> pred = new EqualPredicate<String>("test");
        assertTrue(pred.evaluate("test"));
    }

    @Test
    // Tests evaluate false for different objects using default equator
    public void testEvaluate_differentStrings_returnsFalse() {
        EqualPredicate<String> pred = new EqualPredicate<String>("test");
        assertFalse(pred.evaluate("other"));
    }

    @Test
    // Tests evaluate with null object and non-null value (default equator)
    public void testEvaluate_nullObjectWithNonNullValue_returnsFalse() {
        EqualPredicate<String> pred = new EqualPredicate<String>("test");
        assertFalse(pred.evaluate(null));
    }

    @Test
    // Tests evaluate with null value and non-null object (default equator) – null.equals(object) is false
    public void testEvaluate_nullValueWithNonNullObject_returnsFalse() {
        EqualPredicate<String> pred = new EqualPredicate<String>(null);
        assertFalse(pred.evaluate("test"));
    }

    @Test
    // Tests evaluate with both null (default equator: null.equals(null) is true)
    public void testEvaluate_bothNulls_returnsTrue() {
        EqualPredicate<String> pred = new EqualPredicate<String>(null);
        assertTrue(pred.evaluate(null));
    }

    @Test
    // Tests getValue returns the stored value
    public void testGetValue_returnsStoredValue() {
        EqualPredicate<String> pred = new EqualPredicate<String>("value");
        assertEquals("value", pred.getValue());
    }

    // ===== Factory method: equalPredicate(T, Equator) =====

    @Test
    // Tests factory with equator and null input returns NullPredicate
    public void testEqualPredicate_factoryWithEquatorNull_returnsNullPredicate() {
        Predicate<String> pred = EqualPredicate.<String>equalPredicate(null, new DefaultEquator<String>());
        assertNotNull(pred);
        assertFalse(pred.evaluate("test"));
    }

    @Test
    // Tests factory with equator and non-null input returns EqualPredicate
    public void testEqualPredicate_factoryWithEquatorNonNull_returnsEqualPredicate() {
        Predicate<String> pred = EqualPredicate.equalPredicate("hi", new DefaultEquator<String>());
        assertNotNull(pred);
        assertTrue(pred instanceof EqualPredicate);
    }

    @Test
    // Tests evaluate with custom equator using reference equality
    public void testEvaluate_customEquatorRefEq_returnsTrue() {
        String obj = new String("same");
        EqualPredicate<String> pred = new EqualPredicate<String>(obj, new Equator<String>() {
            @Override
            public boolean equate(String a, String b) {
                return a == b;  // reference equality
            }
        });
        assertTrue(pred.evaluate(obj));
    }

    @Test
    // Tests evaluate with custom equator returning false for equal content but different references
    public void testEvaluate_customEquatorRefEq_returnsFalse() {
        String obj1 = new String("same");
        String obj2 = new String("same");
        EqualPredicate<String> pred = new EqualPredicate<String>(obj1, new Equator<String>() {
            @Override
            public boolean equate(String a, String b) {
                return a == b;  // reference equality
            }
        });
        assertFalse(pred.evaluate(obj2));
    }

    @Test
    // Tests evaluate with custom equator and null values
    public void testEvaluate_customEquatorWithNull_returnsFalse() {
        EqualPredicate<String> pred = new EqualPredicate<String>(null, new Equator<String>() {
            @Override
            public boolean equate(String a, String b) {
                // simple: if both null -> true, else false
                return (a == null && b == null);
            }
        });
        // both null
        assertTrue(pred.evaluate(null));
        // non-null object with null value
        assertFalse(pred.evaluate("any"));
    }

    @Test
    // Tests evaluate with custom equator returning true always
    public void testEvaluate_customEquatorAlwaysTrue_returnsTrue() {
        EqualPredicate<String> pred = new EqualPredicate<String>("ignore", new Equator<String>() {
            @Override
            public boolean equate(String a, String b) {
                return true;
            }
        });
        assertTrue(pred.evaluate("anything"));
        assertTrue(pred.evaluate(null));
    }

    // ===== Additional tests for uncovered parts =====

    @Test
    // Tests evaluate with custom equator that does not handle null well (both nulls but equator fails)
    public void testEvaluate_customEquatorNullHandling_returnsFalse() {
        EqualPredicate<String> pred = new EqualPredicate<String>(null, new Equator<String>() {
            @Override
            public boolean equate(String a, String b) {
                if (a == null && b == null) {
                    return false; // deliberately treat both nulls as false
                }
                if (a == null || b == null) {
                    return false;
                }
                return a.equals(b);
            }
        });
        assertFalse(pred.evaluate(null));
    }

    @Test
    // Tests getValue returns null when stored value is null
    public void testGetValue_nullValue_returnsNull() {
        EqualPredicate<String> pred = new EqualPredicate<String>(null);
        assertNull(pred.getValue());
    }

    @Test
    // Tests evaluate with custom equator that uses hashCode-like logic
    public void testEvaluate_customEquatorBasedOnLength() {
        EqualPredicate<String> pred = new EqualPredicate<String>("abc", new Equator<String>() {
            @Override
            public boolean equate(String a, String b) {
                if (a == null && b == null) return true;
                if (a == null || b == null) return false;
                return a.length() == b.length();
            }
        });
        assertTrue(pred.evaluate("xyz")); // same length
        assertFalse(pred.evaluate("abcd")); // different length
    }

    @Test
    // Tests constructor with non-null value and custom equator that throws
    public void testEvaluate_customEquatorThrowsException() {
        EqualPredicate<String> pred = new EqualPredicate<String>("test", new Equator<String>() {
            @Override
            public boolean equate(String a, String b) {
                throw new RuntimeException("Custom equator exception");
            }
        });
        try {
            pred.evaluate("test");
            fail("Expected exception from custom equator");
        } catch (RuntimeException e) {
            assertEquals("Custom equator exception", e.getMessage());
        }
    }

    @Test
    // Tests factory with null equator should handle gracefully (assuming it returns EqualPredicate with default)
    public void testEqualPredicate_factoryWithNullEquator_returnsDefaultBehavior() {
        Predicate<String> pred = EqualPredicate.equalPredicate("test", null);
        assertNotNull(pred);
        assertTrue(pred instanceof EqualPredicate);
        EqualPredicate<String> eqPred = (EqualPredicate<String>) pred;
        assertTrue(eqPred.evaluate("test"));
        assertFalse(eqPred.evaluate("other"));
    }
}