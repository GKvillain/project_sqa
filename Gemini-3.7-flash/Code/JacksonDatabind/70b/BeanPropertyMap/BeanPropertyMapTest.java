package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class BeanPropertyMapTest {

    private SettableBeanProperty createProp(String name) {
        SettableBeanProperty prop = Mockito.mock(SettableBeanProperty.class);
        Mockito.when(prop.getName()).thenReturn(name);
        return prop;
    }

    private SettableBeanProperty createProp(String name, int index) {
        SettableBeanProperty prop = Mockito.mock(SettableBeanProperty.class);
        Mockito.when(prop.getName()).thenReturn(name);
        Mockito.when(prop.getPropertyIndex()).thenReturn(index);
        return prop;
    }

    // Tests Defect 70b: removing a mixed-case property from a case-insensitive map
    @Test
    public void testRemove_caseInsensitiveMixedCase_removesSuccessfully() {
        SettableBeanProperty prop = createProp("fooBar");
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop), true);
        assertEquals(1, map.size());

        map.remove(prop);
        assertEquals(0, map.size());
        assertNull(map.find("fooBar"));
        assertNull(map.find("foobar"));
    }

    // Tests removing an existing property in case-sensitive mode
    @Test
    public void testRemove_caseSensitive_removesSuccessfully() {
        SettableBeanProperty prop1 = createProp("prop1");
        SettableBeanProperty prop2 = createProp("prop2");
        BeanPropertyMap map = BeanPropertyMap.construct(Arrays.asList(prop1, prop2), false);

        map.remove(prop1);
        assertEquals(1, map.size());
        assertNull(map.find("prop1"));
        assertNotNull(map.find("prop2"));
    }

    // Tests removing a non-existent property throws NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testRemove_nonExistentProperty_throwsNoSuchElementException() {
        SettableBeanProperty prop1 = createProp("prop1");
        SettableBeanProperty prop2 = createProp("prop2");
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop1), false);

        map.remove(prop2);
    }

    // Tests finding property by name in case-insensitive mode
    @Test
    public void testFind_caseInsensitive_findsRegardlessOfCase() {
        SettableBeanProperty prop = createProp("myField");
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop), true);

        assertSame(prop, map.find("myfield"));
        assertSame(prop, map.find("MYFIELD"));
        assertSame(prop, map.find("myField"));
        assertNull(map.find("unknown"));
    }

    // Tests finding property by name in case-sensitive mode
    @Test
    public void testFind_caseSensitive_matchesExactCaseOnly() {
        SettableBeanProperty prop = createProp("myField");
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop), false);

        assertSame(prop, map.find("myField"));
        assertNull(map.find("MYFIELD"));
        assertNull(map.find("myfield"));
    }

    // Tests finding with null key throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFind_nullKey_throwsIllegalArgumentException() {
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false);
        map.find((String) null);
    }

    // Tests finding property by propertyIndex
    @Test
    public void testFind_byIndex_returnsMatchingProperty() {
        SettableBeanProperty prop1 = createProp("a", 10);
        SettableBeanProperty prop2 = createProp("b", 20);
        BeanPropertyMap map = BeanPropertyMap.construct(Arrays.asList(prop1, prop2), false);

        assertSame(prop1, map.find(10));
        assertSame(prop2, map.find(20));
        assertNull(map.find(99));
    }

    // Tests withProperty replacing an existing property
    @Test
    public void testWithProperty_replaceExisting_updatesProperty() {
        SettableBeanProperty original = createProp("prop");
        SettableBeanProperty replacement = createProp("prop");
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(original), false);

        BeanPropertyMap updated = map.withProperty(replacement);
        assertSame(replacement, updated.find("prop"));
        assertEquals(1, updated.size());
    }

    // Tests withProperty adding a new property
    @Test
    public void testWithProperty_addNew_addsProperty() {
        SettableBeanProperty prop1 = createProp("prop1");
        SettableBeanProperty prop2 = createProp("prop2");
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop1), false);

        BeanPropertyMap updated = map.withProperty(prop2);
        assertSame(prop1, updated.find("prop1"));
        assertSame(prop2, updated.find("prop2"));
        assertEquals(2, updated.getPropertiesInInsertionOrder().length);
    }

    // Tests replace method when property exists
    @Test
    public void testReplace_existingProperty_replacesSuccessfully() {
        SettableBeanProperty prop1 = createProp("target");
        SettableBeanProperty replacement = createProp("target");
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop1), false);

        map.replace(replacement);
        assertSame(replacement, map.find("target"));
    }

    // Tests replace method when property does not exist throws NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testReplace_nonExistentProperty_throwsNoSuchElementException() {
        SettableBeanProperty prop1 = createProp("prop1");
        SettableBeanProperty nonExistent = createProp("unknown");
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop1), false);

        map.replace(nonExistent);
    }

    // Tests withoutProperties filtering
    @Test
    public void testWithoutProperties_validExclusions_returnsFilteredMap() {
        SettableBeanProperty prop1 = createProp("keep");
        SettableBeanProperty prop2 = createProp("remove");
        BeanPropertyMap map = BeanPropertyMap.construct(Arrays.asList(prop1, prop2), false);

        BeanPropertyMap filtered = map.withoutProperties(Collections.singleton("remove"));
        assertEquals(1, filtered.size());
        assertSame(prop1, filtered.find("keep"));
        assertNull(filtered.find("remove"));

        BeanPropertyMap emptyFilter = map.withoutProperties(Collections.<String>emptyList());
        assertSame(map, emptyFilter);
    }

    // Tests withCaseInsensitivity mutant factory
    @Test
    public void testWithCaseInsensitivity_toggleState_returnsNewInstanceWhenChanged() {
        SettableBeanProperty prop = createProp("Prop");
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop), false);

        assertSame(map, map.withCaseInsensitivity(false));

        BeanPropertyMap caseInsensitiveMap = map.withCaseInsensitivity(true);
        assertNotSame(map, caseInsensitiveMap);
        assertSame(prop, caseInsensitiveMap.find("prop"));
    }

    // Tests assignIndexes assigns index to each property
    @Test
    public void testAssignIndexes_callsAssignIndexOnAllProperties() {
        SettableBeanProperty prop1 = createProp("a");
        SettableBeanProperty prop2 = createProp("b");
        BeanPropertyMap map = BeanPropertyMap.construct(Arrays.asList(prop1, prop2), false);

        map.assignIndexes();
        Mockito.verify(prop1).assignIndex(Mockito.anyInt());
        Mockito.verify(prop2).assignIndex(Mockito.anyInt());
    }

    // Tests renameAll with NOP transformer
    @Test
    public void testRenameAll_nopTransformer_returnsSameInstance() {
        SettableBeanProperty prop = createProp("prop");
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop), false);

        assertSame(map, map.renameAll(null));
        assertSame(map, map.renameAll(NameTransformer.NOP));
    }

    // Tests iterator and properties traversal
    @Test
    public void testIterator_traversesAllProperties() {
        SettableBeanProperty prop1 = createProp("p1");
        SettableBeanProperty prop2 = createProp("p2");
        BeanPropertyMap map = BeanPropertyMap.construct(Arrays.asList(prop1, prop2), false);

        List<SettableBeanProperty> list = new ArrayList<SettableBeanProperty>();
        for (SettableBeanProperty p : map) {
            list.add(p);
        }
        assertEquals(2, list.size());
        assertTrue(list.contains(prop1));
        assertTrue(list.contains(prop2));
    }

    // Tests findDeserializeAndSet returns false when property is missing
    @Test
    public void testFindDeserializeAndSet_missingProperty_returnsFalse() throws IOException {
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false);
        JsonParser p = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        boolean result = map.findDeserializeAndSet(p, ctxt, new Object(), "missing");
        assertFalse(result);
    }

    // Tests findDeserializeAndSet calls deserializeAndSet when property exists
    @Test
    public void testFindDeserializeAndSet_existingProperty_returnsTrue() throws IOException {
        SettableBeanProperty prop = createProp("target");
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop), false);
        JsonParser p = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        Object bean = new Object();

        boolean result = map.findDeserializeAndSet(p, ctxt, bean, "target");
        assertTrue(result);
        Mockito.verify(prop).deserializeAndSet(p, ctxt, bean);
    }

    // Tests toString formatting
    @Test
    public void testToString_formatsProperties() {
        SettableBeanProperty prop = createProp("foo");
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop), false);

        String str = map.toString();
        assertTrue(str.startsWith("Properties=["));
        assertTrue(str.contains("foo"));
    }

    // Tests renameAll with active transformer
    @Test
    public void testRenameAll_activeTransformer_renamesProperties() {
        SettableBeanProperty prop = createProp("original");
        SettableBeanProperty renamedProp = createProp("prefix_original");
        Mockito.when(prop.withSimpleName("prefix_original")).thenReturn(renamedProp);

        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop), false);
        NameTransformer transformer = new NameTransformer() {
            @Override
            public String transform(String name) {
                return "prefix_" + name;
            }
            @Override
            public String reverse(String transformed) {
                return transformed.startsWith("prefix_") ? transformed.substring(7) : null;
            }
        };

        BeanPropertyMap renamedMap = map.renameAll(transformer);
        assertNotSame(map, renamedMap);
        assertNull(renamedMap.find("original"));
        assertSame(renamedProp, renamedMap.find("prefix_original"));
    }

    // Tests withoutProperties with null argument
    @Test
    public void testWithoutProperties_nullCollection_returnsSameInstance() {
        SettableBeanProperty prop = createProp("field");
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop), false);

        assertSame(map, map.withoutProperties(null));
    }

    // Tests construct with alias mapping and hasAliases check
    @Test
    public void testConstruct_withAliasMap_findsByAliasesAndChecksHasAliases() {
        SettableBeanProperty prop = createProp("actualName");
        Map<String, List<PropertyName>> aliasMap = new HashMap<String, List<PropertyName>>();
        aliasMap.put("actualName", Arrays.asList(PropertyName.construct("alias1"), PropertyName.construct("alias2")));

        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop), false, aliasMap);

        assertTrue(map.hasAliases());
        assertSame(prop, map.find("actualName"));
        assertSame(prop, map.find("alias1"));
        assertSame(prop, map.find("alias2"));
        assertNull(map.find("unknown"));
    }

    // Tests construct without aliases hasAliases returns false
    @Test
    public void testHasAliases_noAliases_returnsFalse() {
        SettableBeanProperty prop = createProp("field");
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop), false);

        assertFalse(map.hasAliases());
    }

    // Tests hash collisions with multiple properties triggering secondary and spillover buckets
    @Test
    public void testHashBuckets_largeNumberOfProperties_supportsFindReplaceRemove() {
        List<SettableBeanProperty> props = new ArrayList<SettableBeanProperty>();
        for (int i = 0; i < 35; i++) {
            props.add(createProp("prop_" + i, i));
        }
        BeanPropertyMap map = BeanPropertyMap.construct(props, false);
        assertEquals(35, map.size());

        for (int i = 0; i < 35; i++) {
            assertNotNull(map.find("prop_" + i));
            assertNotNull(map.find(i));
        }

        // Test replacing a property in multi-element map
        SettableBeanProperty propToReplace = props.get(10);
        SettableBeanProperty replacement = createProp("prop_10", 10);
        map.replace(replacement);
        assertSame(replacement, map.find("prop_10"));

        // Test removing a property in multi-element map
        SettableBeanProperty propToRemove = props.get(20);
        map.remove(propToRemove);
        assertEquals(34, map.size());
        assertNull(map.find("prop_20"));
        assertNotNull(map.find("prop_0"));
        assertNotNull(map.find("prop_34"));
    }

    // Tests withProperty in case-insensitive mode with existing property in different case
    @Test
    public void testWithProperty_caseInsensitive_replacesExistingPropertyIgnoreCase() {
        SettableBeanProperty original = createProp("fooBar");
        SettableBeanProperty replacement = createProp("FOOBAR");
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(original), true);

        BeanPropertyMap updated = map.withProperty(replacement);
        assertEquals(1, updated.size());
        assertSame(replacement, updated.find("foobar"));
        assertSame(replacement, updated.find("foobar"));
    }
}