package org.jsoup.nodes;

import org.junit.Before;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class AttributesTest {
    private Attributes attrs;

    @Before
    public void setUp() {
        attrs = new Attributes();
    }

    @Test
    public void testGet_variousStates() {
        assertEquals("", attrs.get("nonexistent"));
        attrs.put("key", "value");
        assertEquals("value", attrs.get("key"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGet_nullKey_throwsException() {
        attrs.get(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGet_emptyKey_throwsException() {
        attrs.get("");
    }

    @Test
    public void testGetIgnoreCase_variousStates() {
        assertEquals("", attrs.getIgnoreCase("any"));
        attrs.put("Key", "value");
        assertEquals("value", attrs.getIgnoreCase("key"));
        assertEquals("", attrs.getIgnoreCase("nonexistent"));
    }

    @Test
    public void testPut_keyValue_addsAttribute() {
        attrs.put("key", "value");
        assertTrue(attrs.hasKey("key"));
        assertEquals("value", attrs.get("key"));
    }

    @Test
    public void testPut_boolean_various() {
        attrs.put("disabled", true);
        assertTrue(attrs.hasKey("disabled"));
        attrs.put("disabled", false);
        assertFalse(attrs.hasKey("disabled"));
    }

    @Test
    public void testRemove_existingKey_removesAttribute() {
        attrs.put("key", "value");
        attrs.remove("key");
        assertFalse(attrs.hasKey("key"));
    }

    @Test
    public void testRemoveIgnoreCase_multipleMatches_removesAll() {
        attrs.put("Key", "val1");
        attrs.put("KEY", "val2");
        attrs.put("key", "val3");
        attrs.removeIgnoreCase("key");
        assertEquals(0, attrs.size());
    }

    @Test
    public void testRemoveIgnoreCase_emptyAttributes_noException() {
        attrs.removeIgnoreCase("somekey");
    }

    @Test
    public void testHasKey_variousStates() {
        assertFalse(attrs.hasKey("any"));
        attrs.put("key", "value");
        assertTrue(attrs.hasKey("key"));
        assertFalse(attrs.hasKey("nonexistent"));
    }

    @Test
    public void testHasKeyIgnoreCase_variousStates() {
        assertFalse(attrs.hasKeyIgnoreCase("any"));
        attrs.put("Key", "value");
        assertTrue(attrs.hasKeyIgnoreCase("key"));
    }

    @Test
    public void testSize_variousStates() {
        assertEquals(0, attrs.size());
        attrs.put("a", "1");
        assertEquals(1, attrs.size());
        attrs.put("b", "2");
        assertEquals(2, attrs.size());
        attrs.remove("a");
        assertEquals(1, attrs.size());
    }

    @Test
    public void testAddAll_mergesAttributes() {
        Attributes other = new Attributes();
        other.put("key1", "value1");
        other.put("key2", "value2");
        attrs.addAll(other);
        assertEquals(2, attrs.size());
        assertEquals("value1", attrs.get("key1"));
        assertEquals("value2", attrs.get("key2"));
    }

    @Test
    public void testClone_createsIndependentCopy() {
        attrs.put("key", "value");
        Attributes clone = attrs.clone();
        assertEquals("value", clone.get("key"));
        clone.put("key", "modified");
        assertEquals("value", attrs.get("key"));
        clone.put("newKey", "newVal");
        assertFalse(attrs.hasKey("newKey"));
    }

    @Test
    public void testClone_empty_returnsNewAttributes() {
        Attributes clone = attrs.clone();
        assertNotNull(clone);
        assertEquals(0, clone.size());
    }

    @Test
    public void testEquals_variousScenarios() {
        Attributes other = new Attributes();
        attrs.put("k", "v");
        other.put("k", "v");
        assertTrue(attrs.equals(other));
        other.put("k", "diff");
        assertFalse(attrs.equals(other));
        // both empty
        Attributes empty1 = new Attributes();
        Attributes empty2 = new Attributes();
        assertTrue(empty1.equals(empty2));
    }

    @Test
    public void testDataset_putAndGet() {
        attrs.dataset().put("custom", "dataValue");
        assertTrue(attrs.hasKey("data-custom"));
        assertEquals("dataValue", attrs.get("data-custom"));
        assertEquals("dataValue", attrs.dataset().get("custom"));
    }

    @Test
    public void testIterator_variousStates() {
        assertFalse(attrs.iterator().hasNext());
        attrs.put("a", "1");
        attrs.put("b", "2");
        int count = 0;
        for (Attribute attr : attrs) {
            count++;
            assertNotNull(attr);
        }
        assertEquals(2, count);
    }

    @Test
    public void testAsList_variousStates() {
        assertTrue(attrs.asList().isEmpty());
        attrs.put("k", "v");
        List<Attribute> list = attrs.asList();
        assertEquals(1, list.size());
        assertEquals("k", list.get(0).getKey());
        assertEquals("v", list.get(0).getValue());
    }

    @Test
    public void testHtml_outputsAttributes() {
        attrs.put("class", "foo");
        attrs.put("id", "bar");
        String html = attrs.html();
        assertTrue(html.contains("class=\"foo\""));
        assertTrue(html.contains("id=\"bar\""));
    }

    @Test
    public void testHtml_emptyAttributes_returnsEmptyHtml() {
        assertEquals("", attrs.html());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPut_nullAttribute_throwsException() {
        attrs.put((Attribute) null);
    }
}