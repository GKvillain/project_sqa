package org.mockito.internal.verification.argumentmatching;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.mockito.internal.matchers.ContainsExtraTypeInformation;

public class ArgumentMatchingToolTest {

    private ArgumentMatchingTool tool;

    @Before
    public void setUp() {
        tool = new ArgumentMatchingTool();
    }

    // Matcher that implements ContainsExtraTypeInformation
    private static class CustomMatcher extends BaseMatcher<Object> implements ContainsExtraTypeInformation {
        private final boolean matchesResult;
        private final String description;
        private final boolean typeMatchesResult;

        public CustomMatcher(boolean matchesResult, String description, boolean typeMatchesResult) {
            this.matchesResult = matchesResult;
            this.description = description;
            this.typeMatchesResult = typeMatchesResult;
        }

        @Override
        public boolean matches(Object actual) {
            return matchesResult;
        }

        @Override
        public void describeTo(Description desc) {
            desc.appendText(description);
        }

        @Override
        public boolean typeMatches(Object actual) {
            return typeMatchesResult;
        }
    }

    // Matcher that does NOT implement ContainsExtraTypeInformation
    private static class SimpleMatcher extends BaseMatcher<Object> {
        private final boolean matchesResult;
        private final String description;

        public SimpleMatcher(boolean matchesResult, String description) {
            this.matchesResult = matchesResult;
            this.description = description;
        }

        @Override
        public boolean matches(Object actual) {
            return matchesResult;
        }

        @Override
        public void describeTo(Description desc) {
            desc.appendText(description);
        }
    }

    // ------------------ Normal Cases ------------------

    // Tests: empty matchers and empty arguments
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_emptyMatchersAndArguments_returnsEmpty() {
        List<Matcher> matchers = new ArrayList<>();
        Object[] arguments = new Object[0];
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);
        assertEquals(0, result.length);
    }

    // Tests: different sizes – should return empty array
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_differentSizes_returnsEmpty() {
        List<Matcher> matchers = new ArrayList<>();
        matchers.add(new CustomMatcher(true, "abc", true));
        Object[] arguments = new Object[] { "abc", "def" };
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);
        assertEquals(0, result.length);
    }

    // Tests: all matchers match arguments
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_allMatch_returnsEmpty() {
        List<Matcher> matchers = new ArrayList<>();
        matchers.add(new CustomMatcher(true, "abc", true));
        matchers.add(new CustomMatcher(true, "def", true));
        Object[] arguments = new Object[] { "abc", "def" };
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);
        assertEquals(0, result.length);
    }

    // ------------------ Suspicious Detection Tests ------------------

    // Tests: suspicious found (matches false, toString equal, type not match)
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_suspiciousFound_returnsIndexOfSuspicious() {
        List<Matcher> matchers = new ArrayList<>();
        matchers.add(new CustomMatcher(false, "abc", false));
        Object[] arguments = new Object[] { "abc" };
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);
        assertEquals(1, result.length);
        assertEquals(0, result[0].intValue());
    }

    // Tests: not suspicious because type matches
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_notSuspiciousBecauseTypeMatch_returnsEmpty() {
        List<Matcher> matchers = new ArrayList<>();
        matchers.add(new CustomMatcher(false, "abc", true));
        Object[] arguments = new Object[] { "abc" };
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);
        assertEquals(0, result.length);
    }

    // Tests: not suspicious because toString differs
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_notSuspiciousBecauseToStringDifferent_returnsEmpty() {
        List<Matcher> matchers = new ArrayList<>();
        matchers.add(new CustomMatcher(false, "xyz", false));
        Object[] arguments = new Object[] { "abc" };
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);
        assertEquals(0, result.length);
    }

    // Tests: matcher not instance of ContainsExtraTypeInformation – no suspicious processing
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_matcherNotContainsExtraTypeInfo_returnsEmpty() {
        List<Matcher> matchers = new ArrayList<>();
        matchers.add(new SimpleMatcher(false, "abc"));
        Object[] arguments = new Object[] { "abc" };
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);
        assertEquals(0, result.length);
    }

    // ------------------ Multiple & Mixed Cases ------------------

    // Tests: multiple indices returned when several arguments are suspicious
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_multipleSuspiciousIndices_returnsArray() {
        List<Matcher> matchers = new ArrayList<>();
        matchers.add(new CustomMatcher(false, "abc", false));
        matchers.add(new CustomMatcher(true, "def", true));
        matchers.add(new CustomMatcher(false, "ghi", false));
        Object[] arguments = new Object[] { "abc", "def", "ghi" };
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);
        assertEquals(2, result.length);
        assertEquals(0, result[0].intValue());
        assertEquals(2, result[1].intValue());
    }

    // Tests: mixed matchers – some with ContainsExtraTypeInfo, some without
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_mixedMatchers_someSuspicious_returnsCorrectIndices() {
        List<Matcher> matchers = new ArrayList<>();
        matchers.add(new SimpleMatcher(false, "abc"));
        matchers.add(new CustomMatcher(false, "def", false));
        matchers.add(new SimpleMatcher(true, "ghi"));
        matchers.add(new CustomMatcher(false, "jkl", true));
        Object[] arguments = new Object[] { "abc", "def", "ghi", "jkl" };
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);
        assertEquals(1, result.length);
        assertEquals(1, result[0].intValue());
    }

    // ------------------ Null / Edge Cases ------------------

    // Tests: null argument element – in fixed version this should return empty (defect sensitive)
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_nullArgumentElement_returnsEmpty() {
        List<Matcher> matchers = new ArrayList<>();
        matchers.add(new CustomMatcher(false, "someText", false));
        Object[] arguments = new Object[] { null };
        // In buggy version this would throw NullPointerException; fixed version handles it.
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);
        assertEquals(0, result.length);
    }

    // Tests: null arguments array – fixed version returns empty
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_nullArgumentsArray_returnsEmpty() {
        List<Matcher> matchers = new ArrayList<>();
        // empty matchers; null arguments should lead to empty result in fixed version
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, null);
        assertEquals(0, result.length);
    }

    // Tests: matchers list contains null (should be allowed? but unlikely; still test edge)
    // Not required; skip.

    // Tests: argument array with non-String object – ensures toString works
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_integerArgumentToStringMatch_returnsSuspicious() {
        List<Matcher> matchers = new ArrayList<>();
        // matcher description "123" equals Integer(123).toString()
        matchers.add(new CustomMatcher(false, "123", false));
        Object[] arguments = new Object[] { 123 };
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);
        assertEquals(1, result.length);
        assertEquals(0, result[0].intValue());
    }
}