package com.fasterxml.jackson.databind.introspect;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import org.junit.Test;

public class JacksonAnnotationIntrospectorTest {

    // Tests findEnumValue with @JsonProperty annotation on enum field
    @Test
    public void testFindEnumValue_withJsonPropertyAnnotation_returnsExplicitName() {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        assertEquals("explicit", intr.findEnumValue(MyEnum.VALUE_A));
    }

    // Tests findEnumValue without @JsonProperty annotation
    @Test
    public void testFindEnumValue_withoutJsonPropertyAnnotation_returnsEnumName() {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        MyEnum e = MyEnum.VALUE_B;
        assertEquals(e.name(), intr.findEnumValue(e));
    }

    // Tests findEnumValues with @JsonProperty on some enum constants
    @Test
    public void testFindEnumValues_withExplicitNames_returnsUpdatedNames() {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        Enum<?>[] values = MyEnum.values();
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].name();
        }
        String[] result = intr.findEnumValues(MyEnum.class, values, names);
        assertEquals("explicit", result[0]);
        assertEquals("VALUE_B", result[1]);
    }

    // Tests isAnnotationBundle with an annotation that has @JacksonAnnotationsInside
    @Test
    public void testIsAnnotationBundle_withMetaAnnotation_returnsTrue() {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        assertTrue(intr.isAnnotationBundle(new DummyBundleAnnotation() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DummyBundleAnnotation.class;
            }
        }));
    }

    // Tests isAnnotationBundle with an annotation lacking meta-annotation
    @Test
    public void testIsAnnotationBundle_withoutMetaAnnotation_returnsFalse() {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        assertFalse(intr.isAnnotationBundle(new DummyRegularAnnotation() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DummyRegularAnnotation.class;
            }
        }));
    }

    // Tests findRootName with @JsonRootName annotation
    @Test
    public void testFindRootName_withJsonRootName_returnsPropertyName() {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(AnnotatedWithRootName.class, null, null);
        PropertyName name = intr.findRootName(ac);
        assertNotNull(name);
        assertEquals("customRoot", name.getSimpleName());
    }

    // Tests findRootName without annotation returns null
    @Test
    public void testFindRootName_withoutAnnotation_returnsNull() {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(SimpleBean.class, null, null);
        assertNull(intr.findRootName(ac));
    }

    // Tests findPropertiesToIgnore with @JsonIgnoreProperties for serialization
    @Test
    public void testFindPropertiesToIgnore_forSerializationWithAllowGetters_returnsNull() {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        Annotated ac = AnnotatedClass.construct(AllowGettersBean.class, null, null);
        assertNull(intr.findPropertiesToIgnore(ac, true));
    }

    // Tests findPropertiesToIgnore with @JsonIgnoreProperties for deserialization
    @Test
    public void testFindPropertiesToIgnore_forDeserializationWithoutAllowSetters_returnsIgnoreList() {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        Annotated ac = AnnotatedClass.construct(IgnorePropertiesBean.class, null, null);
        String[] result = intr.findPropertiesToIgnore(ac, false);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("prop1", result[0]);
    }

    // Tests findIgnoreUnknownProperties with @JsonIgnoreProperties(ignoreUnknown=true)
    @Test
    public void testFindIgnoreUnknownProperties_whenTrue_returnsTrue() {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(IgnoreUnknownBean.class, null, null);
        assertEquals(Boolean.TRUE, intr.findIgnoreUnknownProperties(ac));
    }

    // Tests isIgnorableType with @JsonIgnoreType
    @Test
    public void testIsIgnorableType_withJsonIgnoreType_returnsTrue() {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(IgnorableTypeBean.class, null, null);
        assertEquals(Boolean.TRUE, intr.isIgnorableType(ac));
    }

    // Tests hasIgnoreMarker with @JsonIgnore on a member
    @Test
    public void testHasIgnoreMarker_withJsonIgnore_returnsTrue() throws Exception {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        Field field = SimpleBean.class.getDeclaredField("ignoredField");
        AnnotatedField af = new AnnotatedField(null, field, null);
        assertTrue(intr.hasIgnoreMarker(af));
    }

    // Tests hasIgnoreMarker without @JsonIgnore
    @Test
    public void testHasIgnoreMarker_withoutJsonIgnore_returnsFalse() throws Exception {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        Field field = SimpleBean.class.getDeclaredField("value");
        AnnotatedField af = new AnnotatedField(null, field, null);
        assertFalse(intr.hasIgnoreMarker(af));
    }

    // Tests hasRequiredMarker with @JsonProperty(required=true)
    @Test
    public void testHasRequiredMarker_withRequiredTrue_returnsTrue() throws Exception {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        Field field = SimpleBean.class.getDeclaredField("requiredField");
        AnnotatedField af = new AnnotatedField(null, field, null);
        assertEquals(Boolean.TRUE, intr.hasRequiredMarker(af));
    }

    // Tests hasRequiredMarker without @JsonProperty
    @Test
    public void testHasRequiredMarker_withoutAnnotation_returnsNull() throws Exception {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        Field field = SimpleBean.class.getDeclaredField("value");
        AnnotatedField af = new AnnotatedField(null, field, null);
        assertNull(intr.hasRequiredMarker(af));
    }

    // Tests findFormat with @JsonFormat annotation
    @Test
    public void testFindFormat_withJsonFormat_returnsFormatValue() throws Exception {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        Field field = SimpleBean.class.getDeclaredField("formattedField");
        AnnotatedField af = new AnnotatedField(null, field, null);
        JsonFormat.Value format = intr.findFormat(af);
        assertNotNull(format);
        assertEquals("yyyy-MM-dd", format.getPattern());
    }

    // Tests findFormat without @JsonFormat returns null
    @Test
    public void testFindFormat_withoutAnnotation_returnsNull() throws Exception {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        Field field = SimpleBean.class.getDeclaredField("value");
        AnnotatedField af = new AnnotatedField(null, field, null);
        assertNull(intr.findFormat(af));
    }

    // Tests findPropertyAccess with @JsonProperty(access=READ_ONLY)
    @Test
    public void testFindPropertyAccess_withReadOnly_returnsReadOnly() throws Exception {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        Field field = SimpleBean.class.getDeclaredField("readOnlyField");
        AnnotatedField af = new AnnotatedField(null, field, null);
        assertEquals(JsonProperty.Access.READ_ONLY, intr.findPropertyAccess(af));
    }

    // Tests findPropertyDescription with @JsonPropertyDescription
    @Test
    public void testFindPropertyDescription_withAnnotation_returnsDescription() throws Exception {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        Field field = SimpleBean.class.getDeclaredField("describedField");
        AnnotatedField af = new AnnotatedField(null, field, null);
        assertEquals("A description", intr.findPropertyDescription(af));
    }

    // Tests findPropertyIndex with @JsonProperty(index = 5)
    @Test
    public void testFindPropertyIndex_withPositiveIndex_returnsIndex() throws Exception {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        Field field = SimpleBean.class.getDeclaredField("indexedField");
        AnnotatedField af = new AnnotatedField(null, field, null);
        assertEquals(Integer.valueOf(5), intr.findPropertyIndex(af));
    }

    // Tests findPropertyDefaultValue with @JsonProperty(defaultValue = "defaultVal")
    @Test
    public void testFindPropertyDefaultValue_withNonEmptyDefault_returnsDefaultValue() throws Exception {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        Field field = SimpleBean.class.getDeclaredField("defaultValueField");
        AnnotatedField af = new AnnotatedField(null, field, null);
        assertEquals("defaultVal", intr.findPropertyDefaultValue(af));
    }

    // Tests findView with @JsonView annotation
    @Test
    public void testFindViews_withJsonView_returnsViewClasses() throws Exception {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        Field field = SimpleBean.class.getDeclaredField("viewField");
        AnnotatedField af = new AnnotatedField(null, field, null);
        Class<?>[] views = intr.findViews(af);
        assertNotNull(views);
        assertEquals(1, views.length);
        assertEquals(Object.class, views[0]);
    }

    // Tests findEnumValue with null Field (simulate inaccessible field)
    @Test
    public void testFindEnumValue_fieldNotFound_returnsEnumName() {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        // Making a call on a hidden enum class to skip field lookup
        Enum<?> e = HiddenEnum.A;
        assertEquals("A", intr.findEnumValue(e));
    }
}

// --- Helper enums and classes for testing ---

enum MyEnum {
    VALUE_A,
    VALUE_B
}

@com.fasterxml.jackson.annotation.JacksonAnnotationsInside
@interface DummyBundleAnnotation {
}

@interface DummyRegularAnnotation {
}

@JsonRootName("customRoot")
class AnnotatedWithRootName {
    public String value;
}

class SimpleBean {
    @JsonProperty
    public String value;

    @JsonIgnore
    public String ignoredField;

    @JsonProperty(required = true)
    public String requiredField;

    @JsonFormat(pattern = "yyyy-MM-dd")
    public String formattedField;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String readOnlyField;

    @JsonPropertyDescription("A description")
    public String describedField;

    @JsonProperty(index = 5)
    public String indexedField;

    @JsonProperty(defaultValue = "defaultVal")
    public String defaultValueField;

    @JsonView(Object.class)
    public String viewField;
}

@JsonIgnoreProperties(allowGetters = true)
class AllowGettersBean {
    public String prop1;
}

@JsonIgnoreProperties({"prop1"})
class IgnorePropertiesBean {
    public String prop1;
    public String prop2;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class IgnoreUnknownBean {
}

@JsonIgnoreType
class IgnorableTypeBean {
}

enum HiddenEnum {
    A,
    B
}