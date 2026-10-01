package com.fasterxml.jackson.databind.deser;

import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class CreatorPropertyTest {

    private CreatorProperty createBasicProperty(String name, JavaType type, int index, Object injectId) {
        return new CreatorProperty(
                PropertyName.construct(name),
                type,
                null,
                null,
                null,
                null,
                index,
                injectId,
                PropertyMetadata.STD_REQUIRED
        );
    }

    // Tests standard construction and basic getter values
    @Test
    public void testConstruction_validArguments_propertiesSetCorrectly() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("testProp", type, 2, "injectId_1");

        assertEquals("testProp", prop.getName());
        assertEquals(type, prop.getType());
        assertEquals(2, prop.getCreatorIndex());
        assertEquals("injectId_1", prop.getInjectableValueId());
        assertNull(prop.getMember());
        assertNull(prop.getAnnotation(Override.class));
        assertFalse(prop.isIgnorable());
    }

    // Tests construction with JacksonInject.Value
    @Test
    public void testJacksonInjectValueConstructor() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        JacksonInject.Value inject = JacksonInject.Value.forId("inj_id");
        CreatorProperty prop = new CreatorProperty(
                PropertyName.construct("test"),
                type,
                null,
                null,
                null,
                null,
                1,
                inject,
                PropertyMetadata.STD_REQUIRED
        );
        assertEquals("inj_id", prop.getInjectableValueId());
        assertEquals(1, prop.getCreatorIndex());
    }

    // Tests withName method creating a new instance with the updated name
    @Test
    public void testWithName_newName_returnsNewInstanceWithNewName() {
        JavaType type = TypeFactory.defaultInstance().constructType(Integer.class);
        CreatorProperty prop = createBasicProperty("oldName", type, 0, null);
        SettableBeanProperty renamed = prop.withName(PropertyName.construct("newName"));

        assertNotNull(renamed);
        assertNotSame(prop, renamed);
        assertEquals("newName", renamed.getName());
        assertEquals(0, renamed.getCreatorIndex());
        assertEquals(type, renamed.getType());
    }

    // Tests withName method returning the exact same instance when name is unchanged
    @Test
    public void testWithName_sameName_returnsSameInstance() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("prop", type, 0, null);
        SettableBeanProperty same = prop.withName(PropertyName.construct("prop"));
        assertSame(prop, same);
    }

    // Tests withValueDeserializer returning the exact same instance when deserializer is identical
    @Test
    public void testWithValueDeserializer_sameDeserializer_returnsSameInstance() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("prop", type, 0, null);

        SettableBeanProperty result = prop.withValueDeserializer(prop.getValueDeserializer());
        assertSame(prop, result);
    }

    // Tests withValueDeserializer returning a new instance when deserializer changes
    @Test
    public void testWithValueDeserializer_differentDeserializer_returnsNewInstance() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("prop", type, 0, null);
        JsonDeserializer<?> dummyDeser = new SettableBeanPropertyTestHelper.DummyDeserializer();

        SettableBeanProperty result = prop.withValueDeserializer(dummyDeser);
        assertNotNull(result);
        assertNotSame(prop, result);
        assertSame(dummyDeser, result.getValueDeserializer());
    }

    // Tests withNullProvider returning the exact same instance when NullValueProvider is identical
    @Test
    public void testWithNullProvider_sameNullProvider_returnsSameInstance() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("prop", type, 0, null);

        SettableBeanProperty same = prop.withNullProvider(prop.getNullValueProvider());
        assertSame(prop, same);
    }

    // Tests withNullProvider returning a new instance with updated NullValueProvider
    @Test
    public void testWithNullProvider_customNullProvider_returnsNewInstanceWithNullProvider() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("prop", type, 0, null);
        NullValueProvider nvp = NullsConstantProvider.nuller();

        SettableBeanProperty result = prop.withNullProvider(nvp);
        assertNotNull(result);
        assertNotSame(prop, result);
        assertSame(nvp, result.getNullValueProvider());
    }

    // Tests markAsIgnorable sets the ignorable flag to true
    @Test
    public void testMarkAsIgnorable_flagChanged_isIgnorableReturnsTrue() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("prop", type, 0, null);

        assertFalse(prop.isIgnorable());
        prop.markAsIgnorable();
        assertTrue(prop.isIgnorable());
    }

    // Tests isInjectionOnly default returns false
    @Test
    public void testIsInjectionOnly_default_returnsFalse() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("prop", type, 0, null);
        assertFalse(prop.isInjectionOnly());
    }

    // Tests toString format contains property name and injection id
    @Test
    public void testToString_validProperty_containsNameAndInjectId() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("myField", type, 1, "myInjectId");

        String str = prop.toString();
        assertTrue(str.contains("myField"));
        assertTrue(str.contains("myInjectId"));
    }

    // Tests toString format when injection id is null
    @Test
    public void testToString_nullInjectId_containsName() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("propNoInject", type, 0, null);

        String str = prop.toString();
        assertTrue(str.contains("propNoInject"));
    }

    // Tests fixAccess when fallback setter is null does not throw exception
    @Test
    public void testFixAccess_nullFallbackSetter_noExceptionThrown() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("prop", type, 0, null);

        prop.fixAccess(null);
    }

    // Tests delegation to fallback setter for set, setAndReturn, deserializeAndSet, deserializeSetAndReturn, and fixAccess
    @Test
    public void testFallbackSetter_delegatesOperationsCorrectly() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("prop", type, 0, null);
        DummySettableProperty dummy = new DummySettableProperty(PropertyName.construct("prop"), type);

        prop.setFallbackSetter(dummy);

        prop.fixAccess(null);
        assertTrue(dummy.fixAccessCalled);

        Object target = new Object();
        prop.set(target, "val1");
        assertSame(target, dummy.setInstance);
        assertEquals("val1", dummy.setValue);

        Object res = prop.setAndReturn(target, "val2");
        assertSame(target, res);
        assertEquals("val2", dummy.setValue);

        prop.deserializeAndSet(null, null, target);
        assertEquals("deserSet", dummy.setValue);

        Object res2 = prop.deserializeSetAndReturn(null, null, target);
        assertSame(target, res2);
        assertEquals("deserSetReturn", dummy.setValue);
    }

    // Tests set method without fallback setter throws InvalidDefinitionException
    @Test(expected = InvalidDefinitionException.class)
    public void testSet_noFallbackSetter_throwsInvalidDefinitionException() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("prop", type, 0, null);

        prop.set(new Object(), "val");
    }

    // Tests setAndReturn method without fallback setter throws InvalidDefinitionException
    @Test(expected = InvalidDefinitionException.class)
    public void testSetAndReturn_noFallbackSetter_throwsInvalidDefinitionException() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("prop", type, 0, null);

        prop.setAndReturn(new Object(), "val");
    }

    // Tests deserializeAndSet method without fallback setter throws InvalidDefinitionException
    @Test(expected = InvalidDefinitionException.class)
    public void testDeserializeAndSet_noFallbackSetter_throwsInvalidDefinitionException() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("prop", type, 0, null);

        prop.deserializeAndSet(null, null, new Object());
    }

    // Tests deserializeSetAndReturn method without fallback setter throws InvalidDefinitionException
    @Test(expected = InvalidDefinitionException.class)
    public void testDeserializeSetAndReturn_noFallbackSetter_throwsInvalidDefinitionException() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("prop", type, 0, null);

        prop.deserializeSetAndReturn(null, null, new Object());
    }

    // Helper dummy deserializer for testing
    private static class SettableBeanPropertyTestHelper {
        static class DummyDeserializer extends JsonDeserializer<Object> {
            @Override
            public Object deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        }
    }

    // Helper dummy SettableBeanProperty for fallback setter testing
    private static class DummySettableProperty extends SettableBeanProperty {
        Object setInstance;
        Object setValue;
        boolean fixAccessCalled;

        public DummySettableProperty(PropertyName name, JavaType type) {
            super(name, type, null, null, null, PropertyMetadata.STD_REQUIRED);
        }

        protected DummySettableProperty(DummySettableProperty src, PropertyName newName) {
            super(src, newName);
        }

        @Override
        public SettableBeanProperty withName(PropertyName newName) {
            return new DummySettableProperty(this, newName);
        }

        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            return this;
        }

        @Override
        public SettableBeanProperty withNullProvider(NullValueProvider nvalP) {
            return this;
        }

        @Override
        public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> acls) {
            return null;
        }

        @Override
        public AnnotatedMember getMember() {
            return null;
        }

        @Override
        public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            this.setInstance = instance;
            this.setValue = "deserSet";
        }

        @Override
        public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            this.setInstance = instance;
            this.setValue = "deserSetReturn";
            return instance;
        }

        @Override
        public void set(Object instance, Object value) throws IOException {
            this.setInstance = instance;
            this.setValue = value;
        }

        @Override
        public Object setAndReturn(Object instance, Object value) throws IOException {
            this.setInstance = instance;
            this.setValue = value;
            return instance;
        }

        @Override
        public void fixAccess(DeserializationConfig config) {
            this.fixAccessCalled = true;
        }
    }
}