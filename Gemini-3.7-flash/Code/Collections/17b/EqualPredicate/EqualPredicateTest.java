package org.apache.commons.collections.functors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.collections.Predicate;
import org.junit.Test;

public class EqualPredicateTest {

    // Tests factory method with null argument returns NullPredicate
    @Test
    public void testEqualPredicate_nullObject_returnsNullPredicate() {
        Predicate<String> predicate = EqualPredicate.equalPredicate(null);
        assertNotNull(predicate);
        assertTrue(predicate instanceof NullPredicate);
        assertTrue(predicate.evaluate(null));
        assertFalse(predicate.evaluate("test"));
    }

    // Tests factory method with non-null object returns EqualPredicate
    @Test
    public void testEqualPredicate_nonNullObject_returnsEqualPredicate() {
        String value = "hello";
        Predicate<String> predicate = EqualPredicate.equalPredicate(value);
        assertNotNull(predicate);
        assertTrue(predicate instanceof EqualPredicate);
        assertTrue(predicate.evaluate("hello"));
        assertFalse(predicate.evaluate("world"));
    }

    // Tests factory method with null object and custom equator returns NullPredicate
    @Test
    public void testEqualPredicate_nullObjectWithEquator_returnsNullPredicate() {
        Equator<String> equator = new DefaultEquator<String>();
        Predicate<String> predicate = EqualPredicate.equalPredicate(null, equator);
        assertNotNull(predicate);
        assertTrue(predicate instanceof NullPredicate);
        assertTrue(predicate.evaluate(null));
    }

    // Tests factory method with non-null object and custom equator
    @Test
    public void testEqualPredicate_customEquator_usesEquatorLogic() {
        Equator<String> caseInsensitiveEquator = new Equator<String>() {
            public boolean equate(String o1, String o2) {
                if (o1 == null) {
                    return o2 == null;
                }
                return o1.equalsIgnoreCase(o2);
            }

            public int hash(String o) {
                return o == null ? 0 : o.toLowerCase().hashCode();
            }
        };

        Predicate<String> predicate = EqualPredicate.equalPredicate("HELLO", caseInsensitiveEquator);
        assertTrue(predicate.evaluate("hello"));
        assertTrue(predicate.evaluate("HELLO"));
        assertFalse(predicate.evaluate("other"));
    }

    // Tests single-argument constructor and getValue
    @Test
    public void testConstructor_singleArgument_getValueReturnsCorrectObject() {
        Integer value = Integer.valueOf(42);
        EqualPredicate<Integer> predicate = new EqualPredicate<Integer>(value);
        assertEquals(value, predicate.getValue());
    }

    // Tests evaluate with equal object returns true
    @Test
    public void testEvaluate_equalObject_returnsTrue() {
        String value = new String("testString");
        String sameValue = new String("testString");
        EqualPredicate<String> predicate = new EqualPredicate<String>(value);

        assertTrue(predicate.evaluate(sameValue));
    }

    // Tests evaluate with same instance returns true
    @Test
    public void testEvaluate_sameInstance_returnsTrue() {
        String value = "sameInstance";
        EqualPredicate<String> predicate = new EqualPredicate<String>(value);

        assertTrue(predicate.evaluate(value));
    }

    // Tests evaluate with unequal object returns false
    @Test
    public void testEvaluate_unequalObject_returnsFalse() {
        EqualPredicate<String> predicate = new EqualPredicate<String>("expected");

        assertFalse(predicate.evaluate("different"));
    }

    // Tests evaluate with null input when stored value is non-null returns false
    @Test
    public void testEvaluate_nullInputNonStoredValue_returnsFalse() {
        EqualPredicate<String> predicate = new EqualPredicate<String>("nonNull");

        assertFalse(predicate.evaluate(null));
    }

    // Tests evaluate when stored value is null and input is null
    @Test
    public void testEvaluate_storedNullWithNullInput_returnsTrue() {
        EqualPredicate<String> predicate = new EqualPredicate<String>(null);

        assertTrue(predicate.evaluate(null));
        assertFalse(predicate.evaluate("nonNull"));
    }

    // Tests two-argument constructor storing equator and object
    @Test
    public void testConstructor_withEquator_evaluatesCorrectly() {
        Equator<Integer> mod10Equator = new Equator<Integer>() {
            public boolean equate(Integer o1, Integer o2) {
                if (o1 == null || o2 == null) {
                    return o1 == o2;
                }
                return (o1 % 10) == (o2 % 10);
            }

            public int hash(Integer o) {
                return o == null ? 0 : o % 10;
            }
        };

        EqualPredicate<Integer> predicate = new EqualPredicate<Integer>(Integer.valueOf(15), mod10Equator);
        assertEquals(Integer.valueOf(15), predicate.getValue());
        assertTrue(predicate.evaluate(Integer.valueOf(25)));
        assertTrue(predicate.evaluate(Integer.valueOf(5)));
        assertFalse(predicate.evaluate(Integer.valueOf(16)));
    }

    // Tests serialization and deserialization of EqualPredicate
    @Test
    @SuppressWarnings("unchecked")
    public void testSerialization_standardUsage_deserializesCorrectly() throws Exception {
        EqualPredicate<String> predicate = new EqualPredicate<String>("serializableTest");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(predicate);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        EqualPredicate<String> deserialized = (EqualPredicate<String>) ois.readObject();
        ois.close();

        assertEquals(predicate.getValue(), deserialized.getValue());
        assertTrue(deserialized.evaluate("serializableTest"));
        assertFalse(deserialized.evaluate("other"));
    }
}