package org.mockito.internal.matchers;

import static org.junit.Assert.*;

import org.hamcrest.Description;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

public class SameTest {

    // Tests matches() returns true when same object reference
    @Test
    public void testMatches_sameObjectReference_returnsTrue() {
        Object obj = new Object();
        Same same = new Same(obj);
        assertTrue(same.matches(obj));
    }

    // Tests matches() returns false when different object references
    @Test
    public void testMatches_differentObjectReferences_returnsFalse() {
        Object obj1 = new Object();
        Object obj2 = new Object();
        Same same = new Same(obj1);
        assertFalse(same.matches(obj2));
    }

    // Tests matches() with null wanted and null actual returns true
    @Test
    public void testMatches_nullWantedAndNullActual_returnsTrue() {
        Same same = new Same(null);
        assertTrue(same.matches(null));
    }

    // Tests matches() with null wanted and non-null actual returns false
    @Test
    public void testMatches_nullWantedAndNonNullActual_returnsFalse() {
        Same same = new Same(null);
        assertFalse(same.matches(new Object()));
    }

    // Tests matches() with non-null wanted and null actual returns false
    @Test
    public void testMatches_nonNullWantedAndNullActual_returnsFalse() {
        Same same = new Same(new Object());
        assertFalse(same.matches(null));
    }

    // Tests describeTo with String wanted - quotes around string value
    @Test
    public void testDescribeTo_stringWanted_appendsQuotedString() {
        Same same = new Same("hello");
        DescriptionStub description = new DescriptionStub();
        same.describeTo(description);
        assertEquals("same(\"hello\")", description.getText());
    }

    // Tests describeTo with Character wanted - quotes around char value
    @Test
    public void testDescribeTo_charWanted_appendsQuotedChar() {
        Same same = new Same('a');
        DescriptionStub description = new DescriptionStub();
        same.describeTo(description);
        assertEquals("same('a')", description.getText());
    }

    // Tests describeTo with non-String non-Character object - no quotes
    @Test
    public void testDescribeTo_nonStringNonCharWanted_noQuotes() {
        Same same = new Same(123);
        DescriptionStub description = new DescriptionStub();
        same.describeTo(description);
        assertEquals("same(123)", description.getText());
    }

    // Tests describeTo with null wanted - null.toString() will throw NullPointerException
    @Test(expected = NullPointerException.class)
    public void testDescribeTo_nullWanted_throwsNullPointerException() {
        Same same = new Same(null);
        DescriptionStub description = new DescriptionStub();
        same.describeTo(description);
    }

    // Tests matches() with Integer objects - same reference check
    @Test
    public void testMatches_integerSameReference_returnsTrue() {
        Integer val = 100;
        Same same = new Same(val);
        assertTrue(same.matches(val));
    }

    // Tests matches() with equal but different Integer objects - reference comparison
    @Test
    public void testMatches_integerEqualButDifferentReference_returnsFalse() {
        Same same = new Same(100);
        assertFalse(same.matches(100));
    }

    // Helper class to capture description text
    private static class DescriptionStub implements Description {
        private StringBuilder sb = new StringBuilder();

        @Override
        public Description appendText(String text) {
            sb.append(text);
            return this;
        }

        @Override
        public Description appendDescriptionOf(org.hamcrest.SelfDescribing selfDescribing) {
            if (selfDescribing != null) {
                selfDescribing.describeTo(this);
            }
            return this;
        }

        @Override
        public Description appendValue(Object value) {
            if (value == null) {
                sb.append("null");
            } else {
                sb.append(value.toString());
            }
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Description appendValueList(String start, String separator, String end, T... values) {
            return appendValueList(start, separator, end, java.util.Arrays.asList(values));
        }

        @Override
        public <T> Description appendValueList(String start, String separator, String end, Iterable<T> values) {
            sb.append(start);
            boolean first = true;
            for (T value : values) {
                if (!first) {
                    sb.append(separator);
                }
                appendValue(value);
                first = false;
            }
            sb.append(end);
            return this;
        }

        @Override
        public Description appendList(String start, String separator, String end, Iterable<? extends org.hamcrest.SelfDescribing> values) {
            sb.append(start);
            boolean first = true;
            for (org.hamcrest.SelfDescribing selfDescribing : values) {
                if (!first) {
                    sb.append(separator);
                }
                if (selfDescribing != null) {
                    selfDescribing.describeTo(this);
                }
                first = false;
            }
            sb.append(end);
            return this;
        }

        public String getText() {
            return sb.toString();
        }
    }
}