package com.fasterxml.jackson.databind.deser;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.introspect.Annotated;

public class DefaultDeserializationContextTest {

    private DefaultDeserializationContext.Impl _context;
    private DeserializerFactory _factory;

    @Before
    public void setUp() {
        _factory = new BeanDeserializerFactory(new DeserializerFactoryConfig());
        _context = new DefaultDeserializationContext.Impl(_factory);
    }

    // Tests copy on default Impl returns a new instance
    @Test
    public void testCopy_implInstance_returnsNewInstance() {
        DefaultDeserializationContext copy = _context.copy();
        assertNotNull(copy);
        assertNotSame(_context, copy);
        assertTrue(copy instanceof DefaultDeserializationContext.Impl);
    }

    // Tests copy on custom subclass throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testCopy_customSubclass_throwsIllegalStateException() {
        DefaultDeserializationContext custom = new DefaultDeserializationContext(_factory, null) {
            private static final long serialVersionUID = 1L;

            @Override
            public DefaultDeserializationContext with(DeserializerFactory factory) {
                return this;
            }

            @Override
            public DefaultDeserializationContext createInstance(DeserializationConfig config,
                    com.fasterxml.jackson.core.JsonParser jp, InjectableValues values) {
                return this;
            }
        };
        custom.copy();
    }

    // Tests with factory returns new Impl instance with specified factory
    @Test
    public void testWith_newFactory_returnsNewInstanceWithFactory() {
        DeserializerFactory newFactory = new BeanDeserializerFactory(new DeserializerFactoryConfig());
        DefaultDeserializationContext result = _context.with(newFactory);
        assertNotNull(result);
        assertNotSame(_context, result);
        assertSame(newFactory, result.getFactory());
    }

    // Tests createInstance returns a new context instance
    @Test
    public void testCreateInstance_nullConfigAndParser_returnsInstance() {
        DefaultDeserializationContext instance = _context.createInstance(null, null, null);
        assertNotNull(instance);
        assertNotSame(_context, instance);
    }

    // Tests findObjectId returns valid ReadableObjectId and reuses entry for same key
    @Test
    public void testFindObjectId_sameKey_returnsSameReadableObjectId() {
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        ObjectIdResolver resolver = new SimpleObjectIdResolver();

        ReadableObjectId roid1 = _context.findObjectId(1, gen, resolver);
        assertNotNull(roid1);
        assertEquals(1, roid1.getKey().key);

        ReadableObjectId roid2 = _context.findObjectId(1, gen, resolver);
        assertSame(roid1, roid2);
    }

    // Tests findObjectId with different keys returns different ReadableObjectId entries
    @Test
    public void testFindObjectId_differentKeys_returnsDifferentEntries() {
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        ObjectIdResolver resolver = new SimpleObjectIdResolver();

        ReadableObjectId roid1 = _context.findObjectId(1, gen, resolver);
        ReadableObjectId roid2 = _context.findObjectId(2, gen, resolver);

        assertNotNull(roid1);
        assertNotNull(roid2);
        assertNotSame(roid1, roid2);
    }

    // Tests deprecated findObjectId method delegates properly
    @SuppressWarnings("deprecation")
    @Test
    public void testFindObjectId_deprecatedTwoArgMethod_returnsReadableObjectId() {
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();

        ReadableObjectId roid = _context.findObjectId(100, gen);
        assertNotNull(roid);
        assertEquals(100, roid.getKey().key);
    }

    // Tests checkUnresolvedObjectId does nothing when no object IDs registered
    @Test
    public void testCheckUnresolvedObjectId_noObjectIds_noException() throws Exception {
        _context.checkUnresolvedObjectId();
    }

    // Tests checkUnresolvedObjectId does not fail when no referring properties exist
    @Test
    public void testCheckUnresolvedObjectId_resolvedIds_noException() throws Exception {
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        _context.findObjectId(1, gen, new SimpleObjectIdResolver());

        _context.checkUnresolvedObjectId();
    }

    // Tests deserializerInstance with null input returns null
    @Test
    public void testDeserializerInstance_nullInput_returnsNull() throws JsonMappingException {
        JsonDeserializer<Object> deser = _context.deserializerInstance(null, null);
        assertNull(deser);
    }

    // Tests deserializerInstance with JsonDeserializer instance returns the same instance
    @Test
    public void testDeserializerInstance_deserializerInstance_returnsSame() throws JsonMappingException {
        JsonDeserializer<Object> dummyDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(com.fasterxml.jackson.core.JsonParser p,
                    DeserializationContext ctxt) {
                return null;
            }
        };

        JsonDeserializer<Object> result = _context.deserializerInstance(null, dummyDeser);
        assertSame(dummyDeser, result);
    }

    // Tests deserializerInstance with JsonDeserializer.None returns null
    @Test
    public void testDeserializerInstance_noneClass_returnsNull() throws JsonMappingException {
        JsonDeserializer<Object> result = _context.deserializerInstance(null, JsonDeserializer.None.class);
        assertNull(result);
    }

    // Tests deserializerInstance with non-class non-deserializer object throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testDeserializerInstance_invalidType_throwsIllegalStateException() throws JsonMappingException {
        _context.deserializerInstance(null, "invalidTypeString");
    }

    // Tests deserializerInstance with class not implementing JsonDeserializer throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testDeserializerInstance_nonDeserializerClass_throwsIllegalStateException() throws JsonMappingException {
        _context.deserializerInstance(null, String.class);
    }

    // Tests keyDeserializerInstance with null input returns null
    @Test
    public void testKeyDeserializerInstance_nullInput_returnsNull() throws JsonMappingException {
        KeyDeserializer deser = _context.keyDeserializerInstance(null, null);
        assertNull(deser);
    }

    // Tests keyDeserializerInstance with KeyDeserializer instance returns the same instance
    @Test
    public void testKeyDeserializerInstance_keyDeserializerInstance_returnsSame() throws JsonMappingException {
        KeyDeserializer dummyKeyDeser = new KeyDeserializer() {
            @Override
            public Object deserializeKey(String key, DeserializationContext ctxt) {
                return key;
            }
        };

        KeyDeserializer result = _context.keyDeserializerInstance(null, dummyKeyDeser);
        assertSame(dummyKeyDeser, result);
    }

    // Tests keyDeserializerInstance with KeyDeserializer.None returns null
    @Test
    public void testKeyDeserializerInstance_noneClass_returnsNull() throws JsonMappingException {
        KeyDeserializer result = _context.keyDeserializerInstance(null, KeyDeserializer.None.class);
        assertNull(result);
    }

    // Tests keyDeserializerInstance with non-class non-KeyDeserializer object throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testKeyDeserializerInstance_invalidType_throwsIllegalStateException() throws JsonMappingException {
        _context.keyDeserializerInstance(null, 12345);
    }

    // Tests keyDeserializerInstance with class not implementing KeyDeserializer throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testKeyDeserializerInstance_nonKeyDeserializerClass_throwsIllegalStateException() throws JsonMappingException {
        _context.keyDeserializerInstance(null, Integer.class);
    }
}