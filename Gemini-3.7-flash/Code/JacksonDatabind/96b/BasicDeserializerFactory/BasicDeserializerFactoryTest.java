package com.fasterxml.jackson.databind.deser;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BasicDeserializerFactoryTest {

    private ObjectMapper mapper;
    private BasicDeserializerFactory factory;
    private DeserializationContext ctxt;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        factory = BeanDeserializerFactory.instance;
        ctxt = mapper.getDeserializationContext();
    }

    // Static test helper classes
    static class SingleArgCreatorPOJO {
        final String name;

        @JsonCreator
        public SingleArgCreatorPOJO(@JsonProperty("name") String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    static class DelegatingCreatorPOJO {
        final int value;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public DelegatingCreatorPOJO(int value) {
            this.value = value;
        }
    }

    static class FactoryMethodCreatorPOJO {
        final String first;
        final int second;

        private FactoryMethodCreatorPOJO(String first, int second) {
            this.first = first;
            this.second = second;
        }

        @JsonCreator
        public static FactoryMethodCreatorPOJO create(@JsonProperty("first") String first,
                                                      @JsonProperty("second") int second) {
            return new FactoryMethodCreatorPOJO(first, second);
        }
    }

    static class InjectedCreatorPOJO {
        final String name;
        final String injected;

        @JsonCreator
        public InjectedCreatorPOJO(@JsonProperty("name") String name,
                                   @JacksonInject("injectKey") String injected) {
            this.name = name;
            this.injected = injected;
        }
    }

    enum TestEnum {
        A, B, C;

        @JsonCreator
        public static TestEnum fromString(String val) {
            for (TestEnum e : values()) {
                if (e.name().equalsIgnoreCase(val)) {
                    return e;
                }
            }
            return null;
        }
    }

    interface AbstractModel {
        String getValue();
    }

    static class ConcreteModel implements AbstractModel {
        private String value;

        public ConcreteModel() {}

        public ConcreteModel(String v) {
            this.value = v;
        }

        @Override
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    // Tests fluent configuration methods
    @Test
    public void testWithConfig_modifications_createsNewInstances() {
        DeserializerFactoryConfig originalConfig = factory.getFactoryConfig();
        assertNotNull(originalConfig);

        DeserializerFactory f1 = factory.withAdditionalDeserializers(new Deserializers.Base());
        assertNotSame(factory, f1);

        DeserializerFactory f2 = factory.withAdditionalKeyDeserializers(new KeyDeserializers() {
            @Override
            public KeyDeserializer findKeyDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) {
                return null;
            }
        });
        assertNotSame(factory, f2);

        DeserializerFactory f3 = factory.withDeserializerModifier(new BeanDeserializerModifier());
        assertNotSame(factory, f3);

        DeserializerFactory f4 = factory.withAbstractTypeResolver(new SimpleAbstractTypeResolver());
        assertNotSame(factory, f4);

        DeserializerFactory f5 = factory.withValueInstantiators(new ValueInstantiators.Base());
        assertNotSame(factory, f5);
    }

    // Tests mapAbstractType with registered resolver
    @Test
    public void testMapAbstractType_withResolver_resolvesConcreteSubtype() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(AbstractModel.class, ConcreteModel.class);

        DeserializerFactory customFactory = factory.withAbstractTypeResolver(resolver);
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType abstractType = mapper.constructType(AbstractModel.class);

        JavaType concreteType = customFactory.mapAbstractType(config, abstractType);
        assertNotNull(concreteType);
        assertEquals(ConcreteModel.class, concreteType.getRawClass());
    }

    // Tests mapAbstractType validation failure for unrelated mapping
    @Test(expected = IllegalArgumentException.class)
    public void testMapAbstractType_invalidMapping_throwsException() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(AbstractModel.class, (Class) String.class);

        DeserializerFactory customFactory = factory.withAbstractTypeResolver(resolver);
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType abstractType = mapper.constructType(AbstractModel.class);

        customFactory.mapAbstractType(config, abstractType);
    }

    // Tests standard ValueInstantiator resolution for built-in empty collections
    @Test
    public void testFindValueInstantiator_standardTypes_returnsInstantiators() throws Exception {
        DeserializationConfig config = mapper.getDeserializationConfig();

        JavaType locationType = mapper.constructType(JsonLocation.class);
        BeanDescription locDesc = config.introspect(locationType);
        ValueInstantiator locInst = factory.findValueInstantiator(ctxt, locDesc);
        assertNotNull(locInst);

        JavaType emptyListType = mapper.constructType(Collections.EMPTY_LIST.getClass());
        BeanDescription listDesc = config.introspect(emptyListType);
        ValueInstantiator listInst = factory.findValueInstantiator(ctxt, listDesc);
        assertNotNull(listInst);
        assertTrue(listInst.canCreateUsingDefault());

        JavaType emptyMapType = mapper.constructType(Collections.EMPTY_MAP.getClass());
        BeanDescription mapDesc = config.introspect(emptyMapType);
        ValueInstantiator mapInst = factory.findValueInstantiator(ctxt, mapDesc);
        assertNotNull(mapInst);
        assertTrue(mapInst.canCreateUsingDefault());
    }

    // Tests deserializing array of primitives and objects
    @Test
    public void testCreateArrayDeserializer_primitiveAndObjectArrays_deserializesCorrectly() throws Exception {
        int[] intArray = mapper.readValue("[1, 2, 3]", int[].class);
        assertArrayEquals(new int[]{1, 2, 3}, intArray);

        String[] strArray = mapper.readValue("[\"a\", \"b\"]", String[].class);
        assertArrayEquals(new String[]{"a", "b"}, strArray);
    }

    // Tests deserializing standard Collection fallbacks (List, Set, Queue, ArrayBlockingQueue)
    @Test
    public void testCreateCollectionDeserializer_abstractAndConcreteCollections_deserializesCorrectly() throws Exception {
        List<String> list = mapper.readValue("[\"x\", \"y\"]", new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
        assertTrue(list instanceof ArrayList);
        assertEquals(2, list.size());

        Set<String> set = mapper.readValue("[\"x\"]", new com.fasterxml.jackson.core.type.TypeReference<Set<String>>() {});
        assertTrue(set instanceof HashSet);
        assertTrue(set.contains("x"));

        ArrayBlockingQueue<Integer> queue = mapper.readValue("[10, 20]", new com.fasterxml.jackson.core.type.TypeReference<ArrayBlockingQueue<Integer>>() {});
        assertEquals(2, queue.size());
        assertEquals(Integer.valueOf(10), queue.peek());
    }

    // Tests deserializing standard Map interfaces to fallback implementations
    @Test
    public void testCreateMapDeserializer_mapFallbacks_instantiatesExpectedTypes() throws Exception {
        Map<String, Object> map = mapper.readValue("{\"key\":\"value\"}", new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        assertTrue(map instanceof LinkedHashMap);
        assertEquals("value", map.get("key"));

        ConcurrentMap<String, String> concMap = mapper.readValue("{\"k\":\"v\"}", new com.fasterxml.jackson.core.type.TypeReference<ConcurrentMap<String, String>>() {});
        assertTrue(concMap instanceof ConcurrentHashMap);
        assertEquals("v", concMap.get("k"));
    }

    // Tests Enum deserializer with creator method
    @Test
    public void testCreateEnumDeserializer_withCreatorMethod_deserializesMatchingConstant() throws Exception {
        TestEnum result = mapper.readValue("\"b\"", TestEnum.class);
        assertEquals(TestEnum.B, result);
    }

    // Tests Enum key deserializer
    @Test
    public void testCreateKeyDeserializer_enumKey_deserializesMapKey() throws Exception {
        Map<TestEnum, String> map = mapper.readValue("{\"A\":\"val\"}", new com.fasterxml.jackson.core.type.TypeReference<Map<TestEnum, String>>() {});
        assertTrue(map.containsKey(TestEnum.A));
        assertEquals("val", map.get(TestEnum.A));
    }

    // Tests createReferenceDeserializer with AtomicReference
    @Test
    public void testCreateReferenceDeserializer_atomicReference_deserializesValue() throws Exception {
        AtomicReference<String> ref = mapper.readValue("\"inner\"", new com.fasterxml.jackson.core.type.TypeReference<AtomicReference<String>>() {});
        assertNotNull(ref);
        assertEquals("inner", ref.get());
    }

    // Tests createTreeDeserializer for JsonNode
    @Test
    public void testCreateTreeDeserializer_jsonNode_parsesJsonTree() throws Exception {
        JsonNode node = mapper.readTree("{\"field\":\"val\"}");
        assertNotNull(node);
        assertTrue(node.isObject());
        assertEquals("val", node.get("field").asText());
    }

    // Tests single-arg creator constructor
    @Test
    public void testSingleArgCreator_propertyBased_createsObject() throws Exception {
        SingleArgCreatorPOJO pojo = mapper.readValue("{\"name\":\"testName\"}", SingleArgCreatorPOJO.class);
        assertNotNull(pojo);
        assertEquals("testName", pojo.getName());
    }

    // Tests delegating creator constructor
    @Test
    public void testDelegatingCreator_primitiveValue_createsObject() throws Exception {
        DelegatingCreatorPOJO pojo = mapper.readValue("123", DelegatingCreatorPOJO.class);
        assertNotNull(pojo);
        assertEquals(123, pojo.value);
    }

    // Tests multi-argument static factory creator
    @Test
    public void testFactoryMethodCreator_namedProperties_createsObject() throws Exception {
        FactoryMethodCreatorPOJO pojo = mapper.readValue("{\"first\":\"hello\",\"second\":42}", FactoryMethodCreatorPOJO.class);
        assertNotNull(pojo);
        assertEquals("hello", pojo.first);
        assertEquals(42, pojo.second);
    }

    // Tests creator with @JacksonInject annotation
    @Test
    public void testInjectedCreator_withInjectedValue_populatesInjectedParameter() throws Exception {
        InjectableValues inject = new InjectableValues.Std().addValue("injectKey", "injectedVal");
        InjectedCreatorPOJO pojo = mapper.reader(inject)
                .forType(InjectedCreatorPOJO.class)
                .readValue("{\"name\":\"explicitName\"}");
        assertNotNull(pojo);
        assertEquals("explicitName", pojo.name);
        assertEquals("injectedVal", pojo.injected);
    }

    // Tests findDefaultDeserializer for basic Java types
    @Test
    public void testFindDefaultDeserializer_basicTypes_returnsNonNull() throws Exception {
        DeserializationConfig config = mapper.getDeserializationConfig();

        JavaType stringType = mapper.constructType(String.class);
        BeanDescription strDesc = config.introspect(stringType);
        JsonDeserializer<?> strDeser = factory.findDefaultDeserializer(ctxt, stringType, strDesc);
        assertNotNull(strDeser);

        JavaType dateType = mapper.constructType(Date.class);
        BeanDescription dateDesc = config.introspect(dateType);
        JsonDeserializer<?> dateDeser = factory.findDefaultDeserializer(ctxt, dateType, dateDesc);
        assertNotNull(dateDeser);
    }

    // Tests custom Deserializers registration and resolution
    @Test
    public void testCustomDeserializers_registeredViaModule_usedByFactory() throws Exception {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ConcreteModel.class, new JsonDeserializer<ConcreteModel>() {
            @Override
            public ConcreteModel deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) {
                return new ConcreteModel("custom");
            }
        });
        mapper.registerModule(module);

        ConcreteModel model = mapper.readValue("{}", ConcreteModel.class);
        assertNotNull(model);
        assertEquals("custom", model.getValue());
    }
}