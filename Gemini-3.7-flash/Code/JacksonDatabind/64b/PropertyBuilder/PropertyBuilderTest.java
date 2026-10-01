package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PropertyBuilderTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // Helper classes for testing various inclusion scenarios

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    static class NonDefaultClassBean {
        public int x = 10;
        public int y = 20;
        public String text = "default";
        public String other = "init";
    }

    static class NonDefaultPropertyBean {
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public int num = 0;

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public String str = "";

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public List<String> list = new ArrayList<String>();

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public int nonDefaultNum = 42;
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    static class ArrayBean {
        public int[] numbers = new int[]{1, 2, 3};
    }

    static class NonEmptyBean {
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String emptyStr = "";

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String nonEmptyStr = "hello";

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public List<String> emptyList = Collections.emptyList();

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public int[] emptyArray = new int[0];
    }

    static class NonNullBean {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String nullField = null;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String nonNullField = "value";
    }

    static class NonAbsentBean {
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        public AtomicReference<String> absentRef = new AtomicReference<String>(null);

        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        public AtomicReference<String> presentRef = new AtomicReference<String>("content");

        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        public String nullField = null;
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    static class NoDefaultConstructorBean {
        public int x;

        public NoDefaultConstructorBean(int x) {
            this.x = x;
        }
    }

    static class StaticTypingBean {
        @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
        public Object val = "stringVal";
    }

    static class UnwrappedChild {
        public String inner = "child";
    }

    static class UnwrappedParent {
        @JsonUnwrapped
        public UnwrappedChild child = new UnwrappedChild();
    }

    static class CustomNullSerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString("CUSTOM_NULL");
        }
    }

    static class CustomNullBean {
        @JsonSerialize(nullsUsing = CustomNullSerializer.class)
        public String nullValue = null;
    }

    static class ContainerBean {
        public List<String> items = new ArrayList<String>();
    }

    // Tests class-level NON_DEFAULT inclusion with matching and non-matching defaults
    @Test
    public void testBuildWriter_classNonDefault_suppressesMatchingDefaults() throws Exception {
        NonDefaultClassBean bean = new NonDefaultClassBean();
        bean.y = 99; // default is 20
        bean.other = "changed"; // default is "init"

        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"y\":99"));
        assertTrue(json.contains("\"other\":\"changed\""));
        assertTrue(!json.contains("\"x\""));
        assertTrue(!json.contains("\"text\""));
    }

    // Tests property-level NON_DEFAULT inclusion
    @Test
    public void testBuildWriter_propertyNonDefault_suppressesTypeDefaults() throws Exception {
        NonDefaultPropertyBean bean = new NonDefaultPropertyBean();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"nonDefaultNum\":42}", json);

        bean.num = 5;
        bean.str = "test";
        bean.nonDefaultNum = 0;
        json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"num\":5"));
        assertTrue(json.contains("\"str\":\"test\""));
        assertTrue(!json.contains("\"nonDefaultNum\""));
    }

    // Tests NON_DEFAULT with array properties comparing elements
    @Test
    public void testBuildWriter_arrayNonDefault_suppressesEqualArray() throws Exception {
        ArrayBean bean = new ArrayBean();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{}", json);

        bean.numbers = new int[]{1, 2, 4};
        json = mapper.writeValueAsString(bean);
        assertEquals("{\"numbers\":[1,2,4]}", json);
    }

    // Tests NON_EMPTY inclusion suppressing empty strings, lists, arrays
    @Test
    public void testBuildWriter_nonEmpty_suppressesEmptyValues() throws Exception {
        NonEmptyBean bean = new NonEmptyBean();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"nonEmptyStr\":\"hello\"}", json);
    }

    // Tests NON_NULL inclusion suppressing null values
    @Test
    public void testBuildWriter_nonNull_suppressesNullProperties() throws Exception {
        NonNullBean bean = new NonNullBean();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"nonNullField\":\"value\"}", json);
    }

    // Tests NON_ABSENT inclusion suppressing empty reference types and nulls
    @Test
    public void testBuildWriter_nonAbsent_suppressesNullAndEmptyReference() throws Exception {
        NonAbsentBean bean = new NonAbsentBean();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"presentRef\":\"content\"}", json);
    }

    // Tests NON_DEFAULT when class has no default constructor
    @Test
    public void testBuildWriter_noDefaultConstructor_fallsBackToTypeDefaults() throws Exception {
        NoDefaultConstructorBean bean = new NoDefaultConstructorBean(0);
        String json = mapper.writeValueAsString(bean);
        assertEquals("{}", json);

        bean = new NoDefaultConstructorBean(15);
        json = mapper.writeValueAsString(bean);
        assertEquals("{\"x\":15}", json);
    }

    // Tests static typing via annotation
    @Test
    public void testBuildWriter_staticTyping_serializesProperly() throws Exception {
        StaticTypingBean bean = new StaticTypingBean();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"val\":\"stringVal\"}", json);
    }

    // Tests unwrapped property handling
    @Test
    public void testBuildWriter_unwrappedProperty_unwrapsProperties() throws Exception {
        UnwrappedParent parent = new UnwrappedParent();
        String json = mapper.writeValueAsString(parent);
        assertEquals("{\"inner\":\"child\"}", json);
    }

    // Tests custom null serializer assignment
    @Test
    public void testBuildWriter_customNullSerializer_usesCustomSerializer() throws Exception {
        CustomNullBean bean = new CustomNullBean();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"nullValue\":\"CUSTOM_NULL\"}", json);
    }

    // Tests disabling WRITE_EMPTY_JSON_ARRAYS suppresses empty containers
    @Test
    public void testBuildWriter_disableWriteEmptyArrays_suppressesEmptyContainer() throws Exception {
        mapper.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        ContainerBean bean = new ContainerBean();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{}", json);

        bean.items.add("item");
        json = mapper.writeValueAsString(bean);
        assertEquals("{\"items\":[\"item\"]}", json);
    }

    // Tests config override for default inclusion per type
    @Test
    public void testBuildWriter_configOverrideNonDefault_suppressesDefaults() throws Exception {
        mapper.configOverride(NonDefaultClassBean.class)
                .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_DEFAULT, JsonInclude.Include.NON_DEFAULT));

        NonDefaultClassBean bean = new NonDefaultClassBean();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{}", json);
    }

    // Tests direct PropertyBuilder helper methods via subclass
    @Test
    public void testPropertyBuilder_getDefaultValue_returnsExpectedPrimitiveAndContainerDefaults() {
        PropertyBuilder pb = new PropertyBuilder(
                mapper.getSerializationConfig(),
                mapper.getSerializationConfig().introspect(mapper.constructType(NonDefaultClassBean.class))
        );

        JavaType intType = TypeFactory.defaultInstance().constructType(int.class);
        JavaType stringType = TypeFactory.defaultInstance().constructType(String.class);
        JavaType listType = TypeFactory.defaultInstance().constructType(List.class);
        JavaType objType = TypeFactory.defaultInstance().constructType(Object.class);

        assertEquals(Integer.valueOf(0), pb.getDefaultValue(intType));
        assertEquals("", pb.getDefaultValue(stringType));
        assertEquals(JsonInclude.Include.NON_EMPTY, pb.getDefaultValue(listType));
        assertNull(pb.getDefaultValue(objType));
    }

    // Tests getClassAnnotations accessor
    @Test
    public void testPropertyBuilder_getClassAnnotations_returnsNonNullAnnotations() {
        PropertyBuilder pb = new PropertyBuilder(
                mapper.getSerializationConfig(),
                mapper.getSerializationConfig().introspect(mapper.constructType(NonDefaultClassBean.class))
        );
        assertNotNull(pb.getClassAnnotations());
    }

    // Tests getDefaultBean returns cached instance
    @Test
    public void testPropertyBuilder_getDefaultBean_returnsCachedBean() {
        PropertyBuilder pb = new PropertyBuilder(
                mapper.getSerializationConfig(),
                mapper.getSerializationConfig().introspect(mapper.constructType(NonDefaultClassBean.class))
        );
        Object bean1 = pb.getDefaultBean();
        Object bean2 = pb.getDefaultBean();
        assertNotNull(bean1);
        assertTrue(bean1 instanceof NonDefaultClassBean);
        assertTrue(bean1 == bean2);
    }
}