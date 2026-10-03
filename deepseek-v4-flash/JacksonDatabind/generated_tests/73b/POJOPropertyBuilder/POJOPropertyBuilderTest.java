package com.fasterxml.jackson.databind.introspect;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.util.ClassUtil;

@RunWith(MockitoJUnitRunner.class)
public class POJOPropertyBuilderTest {

    @Mock
    private MapperConfig<?> config;

    @Mock
    private AnnotationIntrospector introspector;

    @Mock
    private AnnotatedMethod getter1, getter2, getter3;

    @Mock
    private AnnotatedMethod setter1, setter2;

    @Mock
    private AnnotatedField field1, field2;

    @Mock
    private AnnotatedParameter ctorParam1, ctorParam2;

    @Mock
    private AnnotatedConstructor constructor;

    private PropertyName name = new PropertyName("prop");
    private PropertyName internalName = new PropertyName("internal");

    private POJOPropertyBuilder builder;

    @Before
    public void setUp() {
        builder = new POJOPropertyBuilder(config, introspector, true, internalName, name);
    }

    // ---------- getter tests ----------

    // Tests that single getter returns that getter
    @Test
    public void testGetGetter_singleGetter_returnsGetter() {
        builder.addGetter(getter1, null, false, true, false);
        assertSame(getter1, builder.getGetter());
    }

    // Tests that multiple getters with different declaring classes and masking returns most specific
    @Test
    public void testGetGetter_multipleGettersMasking_returnsMostSpecific() {
        Class<?> superClass = Object.class;
        Class<?> subClass = String.class;
        when(getter1.getDeclaringClass()).thenReturn(superClass);
        when(getter2.getDeclaringClass()).thenReturn(subClass);
        when(superClass.isAssignableFrom(subClass)).thenReturn(true);
        when(subClass.isAssignableFrom(superClass)).thenReturn(false);

        // setter names to control priority: make getter1 "get" prefix, getter2 "is" prefix
        when(getter1.getName()).thenReturn("getValue");
        when(getter2.getName()).thenReturn("isValue");

        builder.addGetter(getter1, null, false, true, false);
        builder.addGetter(getter2, null, false, true, false);
        // link order: getter2 added first? Actually add adds to front, so getter2 is head.
        // Then getter1 is next. But we iterate from head, getter2 first.
        // Since getter2 is more specific (subclass), it should be selected.
        assertSame(getter2, builder.getGetter());
    }

    // Tests that multiple getters with same class and different priority chooses higher priority (lower number)
    @Test
    public void testGetGetter_multipleGettersDifferentPriority_returnsHigherPriority() {
        when(getter1.getDeclaringClass()).thenReturn(Object.class);
        when(getter2.getDeclaringClass()).thenReturn(Object.class);
        when(getter1.getName()).thenReturn("isValue"); // priority 2
        when(getter2.getName()).thenReturn("getValue"); // priority 1
        builder.addGetter(getter1, null, false, true, false);
        builder.addGetter(getter2, null, false, true, false);
        // head: getter2? Actually order: getter2 added last, so head is getter2 (priority1)
        // getter1 is next. Then curr starts at head getter2, next is getter1.
        // compare: getter2 priority 1 < getter1 priority 2, so curr remains getter2.
        assertSame(getter2, builder.getGetter());
    }

    // Tests that conflicting getters with same class and same priority throw exception
    @Test(expected = IllegalArgumentException.class)
    public void testGetGetter_conflictingGetters_throwsException() {
        when(getter1.getDeclaringClass()).thenReturn(Object.class);
        when(getter2.getDeclaringClass()).thenReturn(Object.class);
        when(getter1.getName()).thenReturn("getValue");
        when(getter2.getName()).thenReturn("getValue2"); // still "get" prefix, priority 1
        builder.addGetter(getter1, null, false, true, false);
        builder.addGetter(getter2, null, false, true, false);
        builder.getGetter();
    }

    // ---------- setter tests ----------

    // Tests single setter returns that setter
    @Test
    public void testGetSetter_singleSetter_returnsSetter() {
        builder.addSetter(setter1, null, false, true, false);
        assertSame(setter1, builder.getSetter());
    }

    // Tests that conflicting setters with same class and same priority throw exception
    @Test(expected = IllegalArgumentException.class)
    public void testGetSetter_conflictingSetters_throwsException() {
        when(setter1.getDeclaringClass()).thenReturn(Object.class);
        when(setter2.getDeclaringClass()).thenReturn(Object.class);
        when(setter1.getName()).thenReturn("setValue");
        when(setter2.getName()).thenReturn("setValue2");
        builder.addSetter(setter1, null, false, true, false);
        builder.addSetter(setter2, null, false, true, false);
        builder.getSetter();
    }

    // Tests that annotation introspector resolves conflict if provided
    @Test
    public void testGetSetter_conflictResolvedByIntrospector_returnsChosen() {
        when(setter1.getDeclaringClass()).thenReturn(Object.class);
        when(setter2.getDeclaringClass()).thenReturn(Object.class);
        when(setter1.getName()).thenReturn("setValue");
        when(setter2.getName()).thenReturn("setValue2");
        when(introspector.resolveSetterConflict(config, setter1, setter2)).thenReturn(setter2);
        builder.addSetter(setter1, null, false, true, false);
        builder.addSetter(setter2, null, false, true, false);
        assertSame(setter2, builder.getSetter());
    }

    // ---------- field tests ----------

    // Tests single field returns that field
    @Test
    public void testGetField_singleField_returnsField() {
        builder.addField(field1, null, false, true, false);
        assertSame(field1, builder.getField());
    }

    // Tests that conflicting fields throw exception
    @Test(expected = IllegalArgumentException.class)
    public void testGetField_conflictingFields_throwsException() {
        when(field1.getDeclaringClass()).thenReturn(Object.class);
        when(field2.getDeclaringClass()).thenReturn(Object.class);
        builder.addField(field1, null, false, true, false);
        builder.addField(field2, null, false, true, false);
        builder.getField();
    }

    // ---------- constructor parameter tests ----------

    // Tests getConstructorParameter returns first constructor parameter if available
    @Test
    public void testGetConstructorParameter_withConstructor_returnsConstructorParam() {
        when(ctorParam1.getOwner()).thenReturn(constructor);
        when(constructor instanceof AnnotatedConstructor).thenReturn(true);
        builder.addCtor(ctorParam1, null, false, true, false);
        assertSame(ctorParam1, builder.getConstructorParameter());
    }

    // Tests getConstructorParameter returns first parameter when no constructor owner
    @Test
    public void testGetConstructorParameter_noConstructorOwner_returnsFirst() {
        when(ctorParam1.getOwner()).thenReturn(mock(AnnotatedMember.class)); // not AnnotatedConstructor
        builder.addCtor(ctorParam1, null, false, true, false);
        assertSame(ctorParam1, builder.getConstructorParameter());
    }

    // ---------- compareTo tests ----------

    @Test
    public void testCompareTo_thisHasCtorParams_returnsNegative() {
        builder.addCtor(ctorParam1, null, false, true, false);
        POJOPropertyBuilder other = new POJOPropertyBuilder(config, introspector, true, internalName, new PropertyName("other"));
        assertTrue(builder.compareTo(other) < 0);
    }

    @Test
    public void testCompareTo_otherHasCtorParams_returnsPositive() {
        POJOPropertyBuilder other = new POJOPropertyBuilder(config, introspector, true, internalName, new PropertyName("other"));
        other.addCtor(ctorParam1, null, false, true, false);
        assertTrue(builder.compareTo(other) > 0);
    }

    @Test
    public void testCompareTo_noCtorParams_sortsByName() {
        POJOPropertyBuilder a = new POJOPropertyBuilder(config, introspector, true, internalName, new PropertyName("a"));
        POJOPropertyBuilder b = new POJOPropertyBuilder(config, introspector, true, internalName, new PropertyName("b"));
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
    }

    // ---------- isExplicitlyIncluded / isExplicitlyNamed ----------

    @Test
    public void testIsExplicitlyIncluded_withExplicitFieldName_returnsTrue() {
        builder.addField(field1, new PropertyName("explicit"), true, true, false);
        assertTrue(builder.isExplicitlyIncluded());
    }

    @Test
    public void testIsExplicitlyIncluded_withoutExplicit_returnsFalse() {
        builder.addField(field1, null, false, true, false);
        assertFalse(builder.isExplicitlyIncluded());
    }

    @Test
    public void testIsExplicitlyNamed_withExplicitName_returnsTrue() {
        builder.addGetter(getter1, new PropertyName("explicit"), true, true, false);
        assertTrue(builder.isExplicitlyNamed());
    }

    @Test
    public void testIsExplicitlyNamed_withoutExplicitName_returnsFalse() {
        builder.addGetter(getter1, null, false, true, false);
        assertFalse(builder.isExplicitlyNamed());
    }

    // ---------- removeNonVisible ----------

    @Test
    public void testRemoveNonVisible_readOnly_removesSettersAndCreators() {
        builder.addSetter(setter1, null, false, true, false);
        builder.addCtor(ctorParam1, null, false, true, false);
        // set access to READ_ONLY via introspection
        when(introspector.findPropertyAccess(any())).thenReturn(JsonProperty.Access.READ_ONLY);
        builder.removeNonVisible(false);
        assertNull(builder._setters);
        assertNull(builder._ctorParameters);
        // for serialization (true), fields are kept
        assertNotNull(builder._fields); // but we didn't add fields, so null: not tested
    }

    // ---------- trimByVisibility ----------

    @Test
    public void testTrimByVisibility_explicitNameBeatsImplicit() {
        when(getter1.getDeclaringClass()).thenReturn(Object.class);
        when(getter2.getDeclaringClass()).thenReturn(Object.class);
        builder.addGetter(getter1, null, false, true, false); // no explicit name
        builder.addGetter(getter2, new PropertyName("explicit"), true, true, false); // explicit name
        builder.trimByVisibility();
        // only getter2 should remain (explicit name)
        assertNotNull(builder._getters);
        assertEquals(getter2, builder._getters.value);
        assertNull(builder._getters.next);
    }

    // ---------- anyVisible / anyIgnorals ----------

    @Test
    public void testAnyVisible_withVisibleField_returnsTrue() {
        builder.addField(field1, null, false, true, false);
        assertTrue(builder.anyVisible());
    }

    @Test
    public void testAnyVisible_withNonVisible_returnsFalse() {
        builder.addField(field1, null, false, false, false);
        assertFalse(builder.anyVisible());
    }

    @Test
    public void testAnyIgnorals_withIgnoredField_returnsTrue() {
        builder.addField(field1, null, false, true, true);
        assertTrue(builder.anyIgnorals());
    }

    // ---------- getMetadata ----------

    @Test
    public void testGetMetadata_noAnnotations_returnsStdRequiredOrOptional() {
        when(introspector.hasRequiredMarker(any())).thenReturn(null);
        when(introspector.findPropertyDescription(any())).thenReturn(null);
        when(introspector.findPropertyIndex(any())).thenReturn(null);
        when(introspector.findPropertyDefaultValue(any())).thenReturn(null);
        builder.addGetter(getter1, null, false, true, false);
        PropertyMetadata meta = builder.getMetadata();
        assertEquals(PropertyMetadata.STD_REQUIRED_OR_OPTIONAL, meta);
    }

    // ---------- findExplicitNames ----------

    @Test
    public void testFindExplicitNames_singleExplicit_returnsSet() {
        builder.addGetter(getter1, new PropertyName("explicit"), true, true, false);
        Set<PropertyName> names = builder.findExplicitNames();
        assertEquals(1, names.size());
        assertTrue(names.contains(new PropertyName("explicit")));
    }

    // ---------- explode ----------

    @Test
    public void testExplode_twoExplicitNames_createsTwoProperties() {
        PropertyName nameA = new PropertyName("a");
        PropertyName nameB = new PropertyName("b");
        builder.addGetter(getter1, nameA, true, true, false);
        builder.addGetter(getter2, nameB, true, true, false);
        Collection<PropertyName> newNames = Arrays.asList(nameA, nameB);
        Collection<POJOPropertyBuilder> exploded = builder.explode(newNames);
        assertEquals(2, exploded.size());
        for (POJOPropertyBuilder p : exploded) {
            assertTrue(p.getName().equals("a") || p.getName().equals("b"));
        }
    }
}