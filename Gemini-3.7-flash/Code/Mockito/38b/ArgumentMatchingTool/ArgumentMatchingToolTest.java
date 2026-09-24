package org.mockito.internal.verification.argumentmatching;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Equals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ArgumentMatchingToolTest {

    private ArgumentMatchingTool tool;

    @Before
    public void setUp() {
        tool = new ArgumentMatchingTool();
    }

    // Tests when matchers size differs from arguments length
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_differentSizes_returnsEmptyArray() {
        List<Matcher> matchers = Arrays.<Matcher>asList(new Equals(10));
        Object[] arguments = new Object[] { 10, 20 };

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);

        assertArrayEquals(new Integer[0], suspicious);
    }

    // Tests when both matchers and arguments are empty
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_emptyInputs_returnsEmptyArray() {
        List<Matcher> matchers = Collections.emptyList();
        Object[] arguments = new Object[0];

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);

        assertArrayEquals(new Integer[0], suspicious);
    }

    // Tests matching arguments with identical values and types
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_matchingArgs_returnsEmptyArray() {
        List<Matcher> matchers = Arrays.<Matcher>asList(new Equals(10), new Equals("text"));
        Object[] arguments = new Object[] { 10, "text" };

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);

        assertArrayEquals(new Integer[0], suspicious);
    }

    // Tests suspicious argument mismatch where toString matches but types differ
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_sameToStringDifferentType_returnsSuspiciousIndex() {
        List<Matcher> matchers = Arrays.<Matcher>asList(new Equals(10));
        Object[] arguments = new Object[] { 10L };

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);

        assertArrayEquals(new Integer[] { 0 }, suspicious);
    }

    // Tests normal argument mismatch where toString does not match
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_differentToString_returnsEmptyArray() {
        List<Matcher> matchers = Arrays.<Matcher>asList(new Equals(10));
        Object[] arguments = new Object[] { 20 };

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);

        assertArrayEquals(new Integer[0], suspicious);
    }

    // Tests argument that is null to detect NPE defect
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_nullArgument_returnsEmptyArray() {
        List<Matcher> matchers = Arrays.<Matcher>asList(new Equals("someString"));
        Object[] arguments = new Object[] { null };

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);

        assertArrayEquals(new Integer[0], suspicious);
    }

    // Tests matcher that does not implement ContainsExtraTypeInformation
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_matcherWithoutExtraTypeInfo_returnsEmptyArray() {
        Matcher<Object> nonExtraInfoMatcher = new BaseMatcher<Object>() {
            public boolean matches(Object item) {
                return false;
            }

            public void describeTo(Description description) {
                description.appendText("10");
            }
        };

        List<Matcher> matchers = Arrays.<Matcher>asList(nonExtraInfoMatcher);
        Object[] arguments = new Object[] { 10L };

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);

        assertArrayEquals(new Integer[0], suspicious);
    }

    // Tests safelyMatches when matcher throws an exception during matching
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_matcherThrowsException_handlesGracefully() {
        Matcher<Object> throwingMatcher = new BaseMatcher<Object>() {
            public boolean matches(Object item) {
                throw new RuntimeException("Match error");
            }

            public void describeTo(Description description) {
                description.appendText("10");
            }
        };

        List<Matcher> matchers = Arrays.<Matcher>asList(throwingMatcher);
        Object[] arguments = new Object[] { 10 };

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);

        assertArrayEquals(new Integer[0], suspicious);
    }

    // Tests multiple arguments with mixed matching, non-matching, and suspicious arguments
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_multipleMixedArgs_returnsOnlySuspiciousIndexes() {
        List<Matcher> matchers = Arrays.<Matcher>asList(
                new Equals(10),
                new Equals("same"),
                new Equals(20)
        );
        Object[] arguments = new Object[] {
                10L,
                "same",
                30
        };

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);

        assertArrayEquals(new Integer[] { 0 }, suspicious);
    }

    // Tests multiple suspicious arguments returning multiple indices
    @Test
    public void testGetSuspiciouslyNotMatchingArgsIndexes_multipleSuspiciousArgs_returnsAllSuspiciousIndices() {
        List<Matcher> matchers = Arrays.<Matcher>asList(
                new Equals((byte) 1),
                new Equals((short) 2)
        );
        Object[] arguments = new Object[] {
                1,
                2
        };

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, arguments);

        assertEquals(2, suspicious.length);
        assertArrayEquals(new Integer[] { 0, 1 }, suspicious);
    }
}