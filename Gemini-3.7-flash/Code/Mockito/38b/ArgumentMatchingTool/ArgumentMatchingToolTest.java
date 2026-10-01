package org.mockito.internal.verification.argumentmatching;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.ContainsExtraTypeInformation;
import org.mockito.internal.matchers.Equals;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ArgumentMatchingToolTest {

    private ArgumentMatchingTool tool;

    @Before
    public void setUp() {
        tool = new ArgumentMatchingTool();
    }

    // Tests boundary case where matcher count differs from argument count
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_differentSizes_returnsEmptyArray() {
        List<Matcher> matchers = Arrays.<Matcher>asList(new Equals(10), new Equals(20));
        Object[] arguments = new Object[] { 10 };

        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);

        assertEquals(0, result.length);
    }

    // Tests boundary case where matchers and arguments are both empty
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_emptyInputs_returnsEmptyArray() {
        List<Matcher> matchers = Collections.emptyList();
        Object[] arguments = new Object[0];

        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);

        assertEquals(0, result.length);
    }

    // Tests normal case where all arguments match successfully
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_allMatchingArguments_returnsEmptyArray() {
        List<Matcher> matchers = Arrays.<Matcher>asList(new Equals("hello"), new Equals(100));
        Object[] arguments = new Object[] { "hello", 100 };

        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);

        assertEquals(0, result.length);
    }

    // Tests branch where argument is suspicious (same toString representation, different types, does not match)
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_suspiciousMismatchDifferentTypesSameToString_returnsSuspiciousIndex() {
        // Integer 10 vs Long 10L: toString() is "10" for both, but typeMatches and equals are false
        List<Matcher> matchers = Arrays.<Matcher>asList(new Equals(10));
        Object[] arguments = new Object[] { 10L };

        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);

        assertArrayEquals(new Integer[] { 0 }, result);
    }

    // Tests false branch of (m instanceof ContainsExtraTypeInformation)
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_matcherNotContainsExtraTypeInformation_returnsEmptyArray() {
        Matcher simpleMatcher = new SimpleMatcher("value");
        List<Matcher> matchers = Arrays.asList(simpleMatcher);
        Object[] arguments = new Object[] { "differentValue" };

        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);

        assertEquals(0, result.length);
    }

    // Tests exception handling inside safelyMatches when matcher throws exception
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_matcherThrowsExceptionInMatches_handledGracefully() {
        Matcher throwingMatcher = new StubMatcher("10", false, false, true);
        List<Matcher> matchers = Arrays.asList(throwingMatcher);
        Object[] arguments = new Object[] { "10" };

        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);

        assertArrayEquals(new Integer[] { 0 }, result);
    }

    // Tests false branch of toStringEquals condition
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_differentToStringRepresentation_returnsEmptyArray() {
        List<Matcher> matchers = Arrays.<Matcher>asList(new Equals(10));
        Object[] arguments = new Object[] { 20 };

        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);

        assertEquals(0, result.length);
    }

    // Tests false branch of !typeMatches condition (type matches, but value does not match)
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_sameToStringAndTypeMatches_returnsEmptyArray() {
        StubMatcher matcher = new StubMatcher("sameString", false, true, false);
        List<Matcher> matchers = Arrays.<Matcher>asList(matcher);
        Object[] arguments = new Object[] { "sameString" };

        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);

        assertEquals(0, result.length);
    }

    // Tests multiple arguments with a mix of matching, non-suspicious mismatch, and suspicious mismatch
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_multipleArgumentsMixed_returnsOnlySuspiciousIndexes() {
        List<Matcher> matchers = Arrays.<Matcher>asList(
                new Equals("same"),
                new Equals(10),
                new Equals("different")
        );
        Object[] arguments = new Object[] {
                "same",
                10L,
                "other"
        };

        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);

        assertArrayEquals(new Integer[] { 1 }, result);
    }

    // Tests edge case with null argument (regression test for Defects4J Mockito 38b)
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_nullArgument_doesNotThrowNullPointerException() {
        List<Matcher> matchers = Arrays.<Matcher>asList(new Equals("someString"));
        Object[] arguments = new Object[] { null };

        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);

        assertEquals(0, result.length);
    }

    // Helper matcher without ContainsExtraTypeInformation
    private static class SimpleMatcher extends BaseMatcher<Object> {
        private final Object expected;

        SimpleMatcher(Object expected) {
            this.expected = expected;
        }

        public boolean matches(Object item) {
            return expected == null ? item == null : expected.equals(item);
        }

        public void describeTo(Description description) {
            description.appendText(expected == null ? "null" : expected.toString());
        }
    }

    // Helper matcher extending Equals to safely implement all ContainsExtraTypeInformation methods
    private static class StubMatcher extends Equals {
        private final String descriptionText;
        private final boolean matchesResult;
        private final boolean typeMatchesResult;
        private final boolean throwsException;

        StubMatcher(String descriptionText, boolean matchesResult, boolean typeMatchesResult, boolean throwsException) {
            super(descriptionText);
            this.descriptionText = descriptionText;
            this.matchesResult = matchesResult;
            this.typeMatchesResult = typeMatchesResult;
            this.throwsException = throwsException;
        }

        @Override
        public boolean matches(Object item) {
            if (throwsException) {
                throw new RuntimeException("Simulated matching error");
            }
            return matchesResult;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(descriptionText);
        }

        @Override
        public boolean typeMatches(Object target) {
            return typeMatchesResult;
        }
    }
}