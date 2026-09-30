package com.fasterxml.jackson.databind.deser;

import java.io.Serializable;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.ThrowableDeserializer;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleValueInstantiators;

public class BeanDeserializerFactoryTest {

    private BeanDeserializerFactory factory;
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        factory = BeanDeserializerFactory.instance;
        mapper = new ObjectMapper();
    }

    // --- Helper POJOs ---

    static class SimpleBean {
        public String name;
        public int value;

        public SimpleBean() {}

        public SimpleBean(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    static class CustomException extends Exception {
        private static final long serialVersionUID = 1L;

        public CustomException() {
            super();
        }

        public CustomException(String msg) {
            super(msg);
        }
    }

    interface AbstractService {
        String serve();
    }

    static class ConcreteService implements AbstractService {
        public String data = "serviceData";

        @Override
        public String serve() {
            return data;
        }
    }

    @JsonDeserialize(builder = ValueClassBuilder.class)
    static class ValueClass {
        final int x;
        final String y;

        ValueClass(int x, String y) {
            this.x = x;
            this.y = y;
        }
    }

    @JsonPOJOBuilder(withPrefix = "with")
    static class ValueClassBuilder {
        private int x;
        private String y;

        public ValueClassBuilder withX(int x) {
            this.x = x;
            return this;
        }

        public ValueClassBuilder withY(String y) {
            this.y = y;
            return this;
        }

        public ValueClass build() {
            return new ValueClass(x, y);
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdBean {
        public int id;
        public String name;
    }

    @JsonIgnoreProperties({"ignoredField"})
    static class IgnoredBean {
        public String allowedField;
        public String ignoredField;
    }

    static class CustomSubtypeFactory extends BeanDeserializerFactory {
        private static final long serialVersionUID = 1L;

        public CustomSubtypeFactory(DeserializerFactoryConfig config) {
            super(config);
        }
    }

    // --- Additional Helper POJOs for New Tests ---

    static class AnySetterBean {
        public String title;
        private Map<String, Object> extra = new HashMap<String, Object>();

        @JsonAnySetter
        public void setExtra(String name, Object value) {
            extra.put(name, value);
        }

        public Map<String, Object> getExtra() {
            return extra;
        }
    }

    static class InjectedBean {
        @JacksonInject("injectedValue")
        public String injected;
        public String regular;
    }

    static class CreatorBean {
        private final String name;
        private final int age;

        @JsonCreator
        public CreatorBean(@JsonProperty("name") String name, @JsonProperty("age") int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public int getAge() { return age; }
    }

    static class Location {
        public String city;
        public String country;
    }

    static class UserWithUnwrapped {
        public String name;
        @JsonUnwrapped
        public Location location;
    }

    static class Parent {
        public String name;
        @JsonManagedReference
        public List<Child> children = new ArrayList<Child>();
    }

    static class Child {
        public String name;
        @JsonBackReference
        public Parent parent;
    }

    interface Views {
        interface Public {}
        interface Extended extends Views.Public {}
    }

    static class ViewBean {
        @JsonView(Views.Public.class)
        public String publicField;

        @JsonView(Views.Extended.class)
        public String extendedField;
    }

    @JsonDeserialize(builder = CustomMethodBuilder.class)
    static class CustomMethodBuiltBean {
        final String text;

        CustomMethodBuiltBean(String text) {
            this.text = text;
        }
    }

    @JsonPOJOBuilder(buildMethodName = "createInstance", withPrefix = "set")
    static class CustomMethodBuilder {
        private String text;

        public CustomMethodBuilder setText(String text) {
            this.text = text;
            return this;
        }

        public CustomMethodBuiltBean createInstance() {
            return new CustomMethodBuiltBean(text);
        }
    }

    // --- Tests ---

    // Tests withConfig returning same instance when config does not change
    @Test
    public void testWithConfig_sameConfig_returnsSameInstance() {
        DeserializerFactoryConfig config = factory.getFactoryConfig();
        DeserializerFactory result = factory.withConfig(config);
        assertSame(factory, result);
    }

    // Tests withConfig returning new instance when config changes
    @Test
    public void testWithConfig_newConfig_returnsNewInstance() {
        DeserializerFactoryConfig newConfig = new DeserializerFactoryConfig();
        DeserializerFactory result = factory.withConfig(newConfig);
        assertNotNull(result);
        assertNotSame(factory, result);
        assertEquals(BeanDeserializerFactory.class, result.getClass());
    }

    // Tests withConfig called on subtype without proper override throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testWithConfig_improperSubclass_throwsIllegalStateException() {
        CustomSubtypeFactory subFactory = new CustomSubtypeFactory(new DeserializerFactoryConfig());
        subFactory.withConfig(new DeserializerFactoryConfig());
    }

    // Tests createBeanDeserializer for a regular POJO type
    @Test
    public void testCreateBeanDeserializer_regularBean_returnsBeanDeserializer() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer || deser.isCachable());
    }

    // Tests createBeanDeserializer for Throwable types constructs ThrowableDeserializer
    @Test
    public void testCreateBeanDeserializer_throwableType_returnsThrowableDeserializer() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(CustomException.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser instanceof ThrowableDeserializer);
    }

    // Tests createBeanDeserializer with AbstractTypeResolver materializing interface/abstract class
    @Test
    public void testCreateBeanDeserializer_abstractTypeResolved_returnsConcreteDeserializer() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(AbstractService.class, ConcreteService.class);

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withAbstractTypeResolver(resolver);
        BeanDeserializerFactory customFactory = new BeanDeserializerFactory(config);

        ObjectMapper customMapper = new ObjectMapper();
        DeserializationContext ctxt = customMapper.getDeserializationContext();
        JavaType type = customMapper.constructType(AbstractService.class);
        BeanDescription beanDesc = customMapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = customFactory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);

        AbstractService result = customMapper.readValue("{\"data\":\"customVal\"}", AbstractService.class);
        assertNotNull(result);
        assertEquals("customVal", result.serve());
    }

    // Tests createBuilderBasedDeserializer constructs working builder deserializer
    @Test
    public void testCreateBuilderBasedDeserializer_validBuilder_deserializesCorrectly() throws Exception {
        String json = "{\"x\":42,\"y\":\"hello\"}";
        ValueClass value = mapper.readValue(json, ValueClass.class);
        assertNotNull(value);
        assertEquals(42, value.x);
        assertEquals("hello", value.y);
    }

    // Tests buildBeanDeserializer with custom BeanDeserializerModifier
    @Test
    public void testBuildBeanDeserializer_withModifier_appliesModifier() throws Exception {
        final boolean[] modifierCalled = new boolean[1];

        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                                                          BeanDescription beanDesc,
                                                          JsonDeserializer<?> deserializer) {
                if (beanDesc.getBeanClass() == SimpleBean.class) {
                    modifierCalled[0] = true;
                }
                return deserializer;
            }
        };

        ObjectMapper customMapper = new ObjectMapper();
        customMapper.registerModule(new SimpleModule().setDeserializerModifier(modifier));

        SimpleBean bean = customMapper.readValue("{\"name\":\"test\",\"value\":100}", SimpleBean.class);
        assertNotNull(bean);
        assertEquals("test", bean.name);
        assertEquals(100, bean.value);
        assertTrue(modifierCalled[0]);
    }

    // Tests deserializing bean with Property-based ObjectId reader
    @Test
    public void testBuildBeanDeserializer_withObjectId_handlesIdentity() throws Exception {
        String json = "{\"id\":123,\"name\":\"sample\"}";
        IdBean bean = mapper.readValue(json, IdBean.class);
        assertNotNull(bean);
        assertEquals(123, bean.id);
        assertEquals("sample", bean.name);
    }

    // Tests deserialization with ignored properties
    @Test
    public void testAddBeanProps_withIgnoredProperties_ignoresSpecifiedFields() throws Exception {
        String json = "{\"allowedField\":\"ok\",\"ignoredField\":\"skipped\"}";
        IgnoredBean bean = mapper.readValue(json, IgnoredBean.class);
        assertNotNull(bean);
        assertEquals("ok", bean.allowedField);
        assertNull(bean.ignoredField);
    }

    // Tests isPotentialBeanType throws IllegalArgumentException for primitives or arrays
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_primitiveType_throwsException() {
        factory.isPotentialBeanType(int.class);
    }

    // Tests isPotentialBeanType throws IllegalArgumentException for array types
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_arrayType_throwsException() {
        factory.isPotentialBeanType(String[].class);
    }

    // Tests isPotentialBeanType returns true for valid bean classes
    @Test
    public void testIsPotentialBeanType_validBeanClass_returnsTrue() {
        boolean isBean = factory.isPotentialBeanType(SimpleBean.class);
        assertTrue(isBean);
    }

    // Tests buildThrowableDeserializer captures cause and message correctly
    @Test
    public void testBuildThrowableDeserializer_deserializesExceptionWithCause() throws Exception {
        String json = "{\"message\":\"outer\",\"cause\":{\"message\":\"inner\"}}";
        CustomException ex = mapper.readValue(json, CustomException.class);
        assertNotNull(ex);
        assertEquals("outer", ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("inner", ex.getCause().getMessage());
    }

    // Tests custom deserializer override in createBeanDeserializer
    @Test
    public void testCreateBeanDeserializer_customDeserializer_usesCustom() throws Exception {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(SimpleBean.class, new StdDeserializer<SimpleBean>(SimpleBean.class) {
            private static final long serialVersionUID = 1L;

            @Override
            public SimpleBean deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) {
                return new SimpleBean("custom", 999);
            }
        });

        ObjectMapper customMapper = new ObjectMapper();
        customMapper.registerModule(module);

        SimpleBean bean = customMapper.readValue("{\"name\":\"ignored\",\"value\":1}", SimpleBean.class);
        assertNotNull(bean);
        assertEquals("custom", bean.name);
        assertEquals(999, bean.value);
    }

    // --- Newly Added Tests ---

    // Tests constructAnySetter and @JsonAnySetter property handling
    @Test
    public void testBuildBeanDeserializer_withAnySetter_populatesDynamicFields() throws Exception {
        String json = "{\"title\":\"MyTitle\",\"dynamicA\":\"valA\",\"dynamicB\":123}";
        AnySetterBean bean = mapper.readValue(json, AnySetterBean.class);
        assertNotNull(bean);
        assertEquals("MyTitle", bean.title);
        assertEquals("valA", bean.getExtra().get("dynamicA"));
        assertEquals(123, bean.getExtra().get("dynamicB"));
    }

    // Tests injectables resolution (@JacksonInject) in bean deserialization
    @Test
    public void testBuildBeanDeserializer_withInjectableValues_injectsValue() throws Exception {
        InjectableValues.Std injectables = new InjectableValues.Std();
        injectables.addValue("injectedValue", "injected_data");

        ObjectMapper injectingMapper = new ObjectMapper();
        injectingMapper.setInjectableValues(injectables);

        InjectedBean bean = injectingMapper.readValue("{\"regular\":\"regular_data\"}", InjectedBean.class);
        assertNotNull(bean);
        assertEquals("injected_data", bean.injected);
        assertEquals("regular_data", bean.regular);
    }

    // Tests creator-based bean deserialization with @JsonCreator
    @Test
    public void testBuildBeanDeserializer_withCreatorProperties_constructsViaCreator() throws Exception {
        String json = "{\"name\":\"Bob\",\"age\":30}";
        CreatorBean bean = mapper.readValue(json, CreatorBean.class);
        assertNotNull(bean);
        assertEquals("Bob", bean.getName());
        assertEquals(30, bean.getAge());
    }

    // Tests unwrapped property handling (@JsonUnwrapped)
    @Test
    public void testBuildBeanDeserializer_withUnwrappedProperty_populatesEmbeddedBean() throws Exception {
        String json = "{\"name\":\"Alice\",\"city\":\"Bangkok\",\"country\":\"Thailand\"}";
        UserWithUnwrapped user = mapper.readValue(json, UserWithUnwrapped.class);
        assertNotNull(user);
        assertEquals("Alice", user.name);
        assertNotNull(user.location);
        assertEquals("Bangkok", user.location.city);
        assertEquals("Thailand", user.location.country);
    }

    // Tests managed and back reference handling (@JsonManagedReference / @JsonBackReference)
    @Test
    public void testBuildBeanDeserializer_withManagedAndBackReference_bindsBidirectionalReferences() throws Exception {
        String json = "{\"name\":\"parent1\",\"children\":[{\"name\":\"child1\"},{\"name\":\"child2\"}]}";
        Parent parent = mapper.readValue(json, Parent.class);
        assertNotNull(parent);
        assertEquals("parent1", parent.name);
        assertEquals(2, parent.children.size());
        for (Child child : parent.children) {
            assertSame(parent, child.parent);
        }
    }

    // Tests deserialization with JSON Views (@JsonView)
    @Test
    public void testBuildBeanDeserializer_withViews_filtersPropertiesByView() throws Exception {
        String json = "{\"publicField\":\"publicVal\",\"extendedField\":\"extendedVal\"}";

        ViewBean bean = mapper.readerWithView(Views.Public.class)
                .forType(ViewBean.class)
                .readValue(json);

        assertNotNull(bean);
        assertEquals("publicVal", bean.publicField);
        assertNull(bean.extendedField);

        ViewBean extendedBean = mapper.readerWithView(Views.Extended.class)
                .forType(ViewBean.class)
                .readValue(json);

        assertNotNull(extendedBean);
        assertEquals("publicVal", extendedBean.publicField);
        assertEquals("extendedVal", extendedBean.extendedField);
    }

    // Tests builder-based deserializer with custom build method name and prefix
    @Test
    public void testCreateBuilderBasedDeserializer_withCustomBuildMethodAndPrefix_buildsCorrectly() throws Exception {
        String json = "{\"text\":\"custom_text\"}";
        CustomMethodBuiltBean bean = mapper.readValue(json, CustomMethodBuiltBean.class);
        assertNotNull(bean);
        assertEquals("custom_text", bean.text);
    }

    // Tests custom ValueInstantiators via DeserializerFactoryConfig
    @Test
    public void testFindValueInstantiator_customInstantiator_instantiatesProperly() throws Exception {
        SimpleValueInstantiators instantiators = new SimpleValueInstantiators();
        instantiators.addValueInstantiator(SimpleBean.class, new StdValueInstantiator(mapper.getDeserializationConfig(), SimpleBean.class) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean canCreateUsingDefault() {
                return true;
            }

            @Override
            public Object createUsingDefault(DeserializationContext ctxt) {
                return new SimpleBean("fromInstantiator", 777);
            }
        });

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withValueInstantiators(instantiators);
        BeanDeserializerFactory customFactory = new BeanDeserializerFactory(config);

        ObjectMapper customMapper = new ObjectMapper();
        customMapper.registerModule(new SimpleModule().setValueInstantiators(instantiators));

        SimpleBean bean = customMapper.readValue("{\"name\":\"overridden\"}", SimpleBean.class);
        assertNotNull(bean);
        assertEquals("overridden", bean.name);
        assertEquals(777, bean.value);
    }

    // Tests isPotentialBeanType throws IllegalArgumentException for local class
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_localClass_throwsException() {
        class LocalClass {
            public int x;
        }
        factory.isPotentialBeanType(LocalClass.class);
    }

    // Tests BeanDeserializerModifier updateBuilder and updateProperties hooks
    @Test
    public void testBeanDeserializerModifier_updatePropertiesAndBuilder_calledDuringConstruction() throws Exception {
        final boolean[] updatePropertiesCalled = new boolean[1];
        final boolean[] updateBuilderCalled = new boolean[1];

        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {
            @Override
            public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config,
                                                                 BeanDescription beanDesc,
                                                                 List<BeanPropertyDefinition> propDefs) {
                if (beanDesc.getBeanClass() == ValueClass.class || beanDesc.getBeanClass() == SimpleBean.class) {
                    updatePropertiesCalled[0] = true;
                }
                return propDefs;
            }

            @Override
            public BeanDeserializerBuilder updateBuilder(DeserializationConfig config,
                                                         BeanDescription beanDesc,
                                                         BeanDeserializerBuilder builder) {
                if (beanDesc.getBeanClass() == SimpleBean.class) {
                    updateBuilderCalled[0] = true;
                }
                return builder;
            }
        };

        ObjectMapper customMapper = new ObjectMapper();
        customMapper.registerModule(new SimpleModule().setDeserializerModifier(modifier));

        SimpleBean bean = customMapper.readValue("{\"name\":\"mod\",\"value\":50}", SimpleBean.class);
        assertNotNull(bean);
        assertTrue(updatePropertiesCalled[0]);
        assertTrue(updateBuilderCalled[0]);
    }
}