package com.fasterxml.jackson.databind.deser;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId.Referring;
import com.fasterxml.jackson.databind.introspect.Annotated;

public class DefaultDeserializationContextTest {

    private DefaultDeserializationContext.Impl _context;
    private ObjectMapper _mapper;

    // Custom test deserializer for instance resolution tests
    public static class CustomDeserializer extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(com.fasterxml.jackson.core.JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt) {
            return null;
        }
    }

    // Custom test key deserializer for instance resolution tests
    public static class CustomKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, com.fasterxml.jackson.databind.DeserializationContext ctxt) {
            return key;
        }
    }

    // Custom context subclass to test copy() fallback
    private static class CustomContextSubclass extends DefaultDeserializationContext {
        private static final long serialVersionUID = 1L;

        protected CustomContextSubclass(DeserializerFactory df) {
            super(df, null);
        }

        @Override
        public DefaultDeserializationContext with(DeserializerFactory factory) {
            return this;
        }

        @Override
        public DefaultDeserializationContext createInstance(DeserializationConfig config,
                com.fasterxml.jackson.core.JsonParser jp, com.fasterxml.jackson.databind.InjectableValues values) {
            return this;
        }
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _context = new DefaultDeserializationContext.Impl(BeanDeserializerFactory.instance);
    }

    // Tests copy method creates a new Impl instance
    @Test
    public void testCopy_implInstance_returnsNewInstance() {
        DefaultDeserializationContext copy = _context.copy();
        assertNotNull(copy);
        assertTrue(copy instanceof DefaultDeserializationContext.Impl);
        assertNotSame(_context, copy);
    }

    // Tests copy method on a sub-class without copy override throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testCopy_subclassNotOverridingCopy_throwsIllegalStateException() {
        CustomContextSubclass customContext = new CustomContextSubclass(BeanDeserializerFactory.instance);
        customContext.copy();
    }

    // Tests with method returns new instance with specified DeserializerFactory
    @Test
    public void testWith_validFactory_returnsNewInstance() {
        DeserializerFactory customFactory = BeanDeserializerFactory.instance;
        DefaultDeserializationContext withContext = _context.with(customFactory);
        assertNotNull(withContext);
        assertNotSame(_context, withContext);
    }

    // Tests createInstance creates context with config, parser, and injectable values
    @Test
    public void testCreateInstance_validParameters_returnsInitializedInstance() {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        DefaultDeserializationContext instance = _context.createInstance(config, null, null);
        assertNotNull(instance);
        assertSame(config, instance.getConfig());
    }

    // Tests findObjectId creates and returns a ReadableObjectId
    @Test
    public void testFindObjectId_newId_createsAndReturnsReadableObjectId() {
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        ObjectIdResolver resolver = new SimpleObjectIdResolver();

        ReadableObjectId roid = _context.findObjectId("id1", gen, resolver);

        assertNotNull(roid);
        assertEquals("id1", roid.getKey().key);
    }

    // Tests findObjectId returns cached entry when called with same id and generator
    @Test
    public void testFindObjectId_sameIdAndGenerator_returnsSameInstance() {
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        ObjectIdResolver resolver = new SimpleObjectIdResolver();

        ReadableObjectId roid1 = _context.findObjectId("id1", gen, resolver);
        ReadableObjectId roid2 = _context.findObjectId("id1", gen, resolver);

        assertSame(roid1, roid2);
    }

    // Tests findObjectId with different ids creates distinct entries
    @Test
    public void testFindObjectId_differentIds_returnsDistinctInstances() {
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        ObjectIdResolver resolver = new SimpleObjectIdResolver();

        ReadableObjectId roid1 = _context.findObjectId("id1", gen, resolver);
        ReadableObjectId roid2 = _context.findObjectId("id2", gen, resolver);

        assertNotNull(roid1);
        assertNotNull(roid2);
        assertNotSame(roid1, roid2);
    }

    // Tests deprecated 2-argument findObjectId creates and caches ReadableObjectId
    @Test
    public void testFindObjectId_twoArguments_createsEntryWithDefaultResolver() {
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();

        ReadableObjectId roid = _context.findObjectId("id1", gen);

        assertNotNull(roid);
        assertEquals("id1", roid.getKey().key);
    }

    // Tests checkUnresolvedObjectId when no object ids have been registered
    @Test
    public void testCheckUnresolvedObjectId_noObjectIds_doesNotThrow() throws UnresolvedForwardReference {
        _context.checkUnresolvedObjectId();
    }

    // Tests checkUnresolvedObjectId when object ids have no referring properties
    @Test
    public void testCheckUnresolvedObjectId_noReferringProperties_doesNotThrow() throws UnresolvedForwardReference {
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        _context.findObjectId("id1", gen);

        _context.checkUnresolvedObjectId();
    }

    // Tests checkUnresolvedObjectId with unresolved references throws UnresolvedForwardReference
    @Test(expected = UnresolvedForwardReference.class)
    public void testCheckUnresolvedObjectId_withUnresolvedReferring_throwsException() throws UnresolvedForwardReference {
        DeserializationConfig config = _mapper.getDeserializationConfig()
                .with(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS);
        DefaultDeserializationContext context = _context.createInstance(config, null, null);

        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        ReadableObjectId roid = context.findObjectId("id1", gen);

        UnresolvedForwardReference ufr = new UnresolvedForwardReference("Unresolved reference", JsonLocation.NA, roid);
        roid.appendReferring(new Referring(ufr, String.class) {
            @Override
            public void handleResolvedForwardReference(Object id, Object value) {
            }
        });

        context.checkUnresolvedObjectId();
    }

    // Tests checkUnresolvedObjectId does not throw when FAIL_ON_UNRESOLVED_OBJECT_IDS is disabled
    @Test
    public void testCheckUnresolvedObjectId_featureDisabled_doesNotThrow() throws UnresolvedForwardReference {
        DeserializationConfig config = _mapper.getDeserializationConfig()
                .without(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS);
        DefaultDeserializationContext context = _context.createInstance(config, null, null);

        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        ReadableObjectId roid = context.findObjectId("id1", gen);

        UnresolvedForwardReference ufr = new UnresolvedForwardReference("Unresolved reference", JsonLocation.NA, roid);
        roid.appendReferring(new Referring(ufr, String.class) {
            @Override
            public void handleResolvedForwardReference(Object id, Object value) {
            }
        });

        context.checkUnresolvedObjectId();
    }

    // Tests deserializerInstance with null deserDef returns null
    @Test
    public void testDeserializerInstance_nullDeserDef_returnsNull() throws JsonMappingException {
        assertNull(_context.deserializerInstance(null, null));
    }

    // Tests deserializerInstance with an existing JsonDeserializer instance returns it directly
    @Test
    public void testDeserializerInstance_instanceProvided_returnsSameInstance() throws JsonMappingException {
        CustomDeserializer deser = new CustomDeserializer();
        JsonDeserializer<Object> result = _context.deserializerInstance(null, deser);
        assertSame(deser, result);
    }

    // Tests deserializerInstance with JsonDeserializer.None class returns null
    @Test
    public void testDeserializerInstance_noneClass_returnsNull() throws JsonMappingException {
        assertNull(_context.deserializerInstance(null, JsonDeserializer.None.class));
    }

    // Tests deserializerInstance with a valid Class instantiates deserializer
    @Test
    public void testDeserializerInstance_validClass_instantiatesDeserializer() throws JsonMappingException {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        DefaultDeserializationContext context = _context.createInstance(config, null, null);

        JsonDeserializer<Object> deser = context.deserializerInstance(null, CustomDeserializer.class);
        assertNotNull(deser);
        assertTrue(deser instanceof CustomDeserializer);
    }

    // Tests deserializerInstance with non-class non-deserializer object throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testDeserializerInstance_invalidType_throwsIllegalStateException() throws JsonMappingException {
        _context.deserializerInstance(null, "invalidObject");
    }

    // Tests deserializerInstance with a Class not implementing JsonDeserializer throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testDeserializerInstance_nonDeserializerClass_throwsIllegalStateException() throws JsonMappingException {
        _context.deserializerInstance(null, String.class);
    }

    // Tests keyDeserializerInstance with null deserDef returns null
    @Test
    public void testKeyDeserializerInstance_nullDeserDef_returnsNull() throws JsonMappingException {
        assertNull(_context.keyDeserializerInstance(null, null));
    }

    // Tests keyDeserializerInstance with an existing KeyDeserializer instance returns it directly
    @Test
    public void testKeyDeserializerInstance_instanceProvided_returnsSameInstance() throws JsonMappingException {
        CustomKeyDeserializer keyDeser = new CustomKeyDeserializer();
        KeyDeserializer result = _context.keyDeserializerInstance(null, keyDeser);
        assertSame(keyDeser, result);
    }

    // Tests keyDeserializerInstance with KeyDeserializer.None class returns null
    @Test
    public void testKeyDeserializerInstance_noneClass_returnsNull() throws JsonMappingException {
        assertNull(_context.keyDeserializerInstance(null, KeyDeserializer.None.class));
    }

    // Tests keyDeserializerInstance with a valid Class instantiates KeyDeserializer
    @Test
    public void testKeyDeserializerInstance_validClass_instantiatesKeyDeserializer() throws JsonMappingException {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        DefaultDeserializationContext context = _context.createInstance(config, null, null);

        KeyDeserializer keyDeser = context.keyDeserializerInstance(null, CustomKeyDeserializer.class);
        assertNotNull(keyDeser);
        assertTrue(keyDeser instanceof CustomKeyDeserializer);
    }

    // Tests keyDeserializerInstance with non-class non-keyDeserializer object throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testKeyDeserializerInstance_invalidType_throwsIllegalStateException() throws JsonMappingException {
        _context.keyDeserializerInstance(null, 12345);
    }

    // Tests keyDeserializerInstance with a Class not implementing KeyDeserializer throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testKeyDeserializerInstance_nonKeyDeserializerClass_throwsIllegalStateException() throws JsonMappingException {
        _context.keyDeserializerInstance(null, Integer.class);
    }
}