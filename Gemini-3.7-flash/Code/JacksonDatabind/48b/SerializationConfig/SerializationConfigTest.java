package com.fasterxml.jackson.databind;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class SerializationConfigTest {

    private ObjectMapper _mapper;
    private SerializationConfig _config;

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _config = _mapper.getSerializationConfig();
    }

    // Tests enabling and disabling MapperFeatures via with/without
    @Test
    public void testWithMapperFeatures_enableAndDisable_togglesFeature() {
        SerializationConfig cfg = _config.without(MapperFeature.USE_ANNOTATIONS);
        assertFalse(cfg.isEnabled(MapperFeature.USE_ANNOTATIONS));

        cfg = cfg.with(MapperFeature.USE_ANNOTATIONS);
        assertTrue(cfg.isEnabled(MapperFeature.USE_ANNOTATIONS));

        // Testing no-op branch when already enabled
        SerializationConfig sameCfg = cfg.with(MapperFeature.USE_ANNOTATIONS);
        assertSame(cfg, sameCfg);

        // Testing no-op branch when already disabled
        cfg = cfg.without(MapperFeature.USE_ANNOTATIONS);
        sameCfg = cfg.without(MapperFeature.USE_ANNOTATIONS);
        assertSame(cfg, sameCfg);

        // Varargs with / without
        cfg = cfg.with(MapperFeature.USE_ANNOTATIONS, MapperFeature.AUTO_DETECT_FIELDS);
        assertTrue(cfg.isEnabled(MapperFeature.USE_ANNOTATIONS));
        assertTrue(cfg.isEnabled(MapperFeature.AUTO_DETECT_FIELDS));

        cfg = cfg.without(MapperFeature.USE_ANNOTATIONS, MapperFeature.AUTO_DETECT_FIELDS);
        assertFalse(cfg.isEnabled(MapperFeature.USE_ANNOTATIONS));
        assertFalse(cfg.isEnabled(MapperFeature.AUTO_DETECT_FIELDS));
    }

    // Tests with(MapperFeature, boolean) state toggle
    @Test
    public void testWithMapperFeatureBoolean_enableAndDisable_togglesFeature() {
        SerializationConfig cfg = _config.with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        assertTrue(cfg.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));

        SerializationConfig sameCfg = cfg.with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        assertSame(cfg, sameCfg);

        cfg = cfg.with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false);
        assertFalse(cfg.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));

        sameCfg = cfg.with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false);
        assertSame(cfg, sameCfg);
    }

    // Tests SerializationFeature with single, multiple, and varargs
    @Test
    public void testWithSerializationFeature_singleAndMultiple_modifiesFeatures() {
        SerializationConfig cfg = _config.without(SerializationFeature.INDENT_OUTPUT)
                .without(SerializationFeature.WRAP_ROOT_VALUE);
        assertFalse(cfg.isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertFalse(cfg.isEnabled(SerializationFeature.WRAP_ROOT_VALUE));

        cfg = cfg.with(SerializationFeature.INDENT_OUTPUT);
        assertTrue(cfg.isEnabled(SerializationFeature.INDENT_OUTPUT));

        SerializationConfig sameCfg = cfg.with(SerializationFeature.INDENT_OUTPUT);
        assertSame(cfg, sameCfg);

        cfg = cfg.with(SerializationFeature.WRAP_ROOT_VALUE, SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        assertTrue(cfg.isEnabled(SerializationFeature.WRAP_ROOT_VALUE));
        assertTrue(cfg.isEnabled(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS));

        cfg = _config.without(SerializationFeature.CLOSE_CLOSEABLE);
        cfg = cfg.withFeatures(SerializationFeature.CLOSE_CLOSEABLE, SerializationFeature.EAGER_SERIALIZER_INITIALIZATION);
        assertTrue(cfg.isEnabled(SerializationFeature.CLOSE_CLOSEABLE));
        assertTrue(cfg.isEnabled(SerializationFeature.EAGER_SERIALIZER_INITIALIZATION));
    }

    // Tests SerializationFeature without single, multiple, and varargs
    @Test
    public void testWithoutSerializationFeature_singleAndMultiple_removesFeatures() {
        SerializationConfig cfg = _config.with(SerializationFeature.INDENT_OUTPUT)
                .with(SerializationFeature.WRAP_ROOT_VALUE);
        assertTrue(cfg.isEnabled(SerializationFeature.INDENT_OUTPUT));

        cfg = cfg.without(SerializationFeature.INDENT_OUTPUT);
        assertFalse(cfg.isEnabled(SerializationFeature.INDENT_OUTPUT));

        SerializationConfig sameCfg = cfg.without(SerializationFeature.INDENT_OUTPUT);
        assertSame(cfg, sameCfg);

        cfg = cfg.with(SerializationFeature.INDENT_OUTPUT, SerializationFeature.FAIL_ON_EMPTY_BEANS);
        cfg = cfg.without(SerializationFeature.INDENT_OUTPUT, SerializationFeature.FAIL_ON_EMPTY_BEANS);
        assertFalse(cfg.isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertFalse(cfg.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));

        cfg = cfg.with(SerializationFeature.INDENT_OUTPUT);
        cfg = cfg.withoutFeatures(SerializationFeature.INDENT_OUTPUT);
        assertFalse(cfg.isEnabled(SerializationFeature.INDENT_OUTPUT));
    }

    // Tests DateFormat configuration toggling WRITE_DATES_AS_TIMESTAMPS
    @Test
    public void testWithDateFormat_nonNullAndNull_togglesWriteDatesAsTimestamps() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SerializationConfig cfg = _config.with(df);
        assertFalse(cfg.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        assertEquals(df, cfg.getDateFormat());

        cfg = cfg.with((DateFormat) null);
        assertTrue(cfg.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    // Tests root name configuration including null, string, and identity branches
    @Test
    public void testWithRootName_variousInputs_handlesCorrectly() {
        SerializationConfig cfg = _config.withRootName((PropertyName) null);
        assertSame(_config, cfg);

        PropertyName name = new PropertyName("customRoot");
        cfg = _config.withRootName(name);
        assertEquals("customRoot", cfg.getRootName().getSimpleName());

        SerializationConfig sameCfg = cfg.withRootName(name);
        assertSame(cfg, sameCfg);

        SerializationConfig resetCfg = cfg.withRootName((PropertyName) null);
        assertNull(resetCfg.getRootName());

        // String overload tests
        SerializationConfig strCfg = _config.withRootName("strRoot");
        assertEquals("strRoot", strCfg.getRootName().getSimpleName());

        SerializationConfig strNullCfg = strCfg.withRootName((String) null);
        assertNull(strNullCfg.getRootName());

        SerializationConfig strEmptyCfg = _config.withRootName("");
        assertSame(PropertyName.EMPTY, strEmptyCfg.getRootName());

        SerializationConfig strSameCfg = strEmptyCfg.withRootName("");
        assertSame(strEmptyCfg, strSameCfg);
    }

    // Tests useRootWrapping with explicit rootName and feature flag
    @Test
    public void testUseRootWrapping_withAndWithoutRootName_returnsExpected() {
        SerializationConfig cfg = _config.withRootName((PropertyName) null)
                .without(SerializationFeature.WRAP_ROOT_VALUE);
        assertFalse(cfg.useRootWrapping());

        cfg = cfg.with(SerializationFeature.WRAP_ROOT_VALUE);
        assertTrue(cfg.useRootWrapping());

        cfg = cfg.withRootName(new PropertyName("root"));
        assertTrue(cfg.useRootWrapping());

        cfg = cfg.withRootName(new PropertyName(""));
        assertFalse(cfg.useRootWrapping());
    }

    // Tests view configuration
    @Test
    public void testWithView_differentAndSameViews_handlesCorrectly() {
        assertNull(_config.getActiveView());

        SerializationConfig cfg = _config.withView(String.class);
        assertEquals(String.class, cfg.getActiveView());

        SerializationConfig sameCfg = cfg.withView(String.class);
        assertSame(cfg, sameCfg);

        SerializationConfig clearView = cfg.withView(null);
        assertNull(clearView.getActiveView());
    }

    // Tests filter provider configuration
    @Test
    public void testWithFilters_differentFilterProvider_updatesConfig() {
        assertNull(_config.getFilterProvider());

        SimpleFilterProvider filters = new SimpleFilterProvider();
        SerializationConfig cfg = _config.withFilters(filters);
        assertSame(filters, cfg.getFilterProvider());

        SerializationConfig sameCfg = cfg.withFilters(filters);
        assertSame(cfg, sameCfg);
    }

    // Tests property inclusion configuration and legacy accessor
    @Test
    public void testWithPropertyInclusion_updatesInclusion() {
        JsonInclude.Value incl = JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.ALWAYS);
        SerializationConfig cfg = _config.withPropertyInclusion(incl);
        assertEquals(incl, cfg.getDefaultPropertyInclusion());
        assertEquals(incl, cfg.getDefaultPropertyInclusion(String.class));
        assertEquals(incl, cfg.getDefaultPropertyInclusion(String.class, incl));
        assertEquals(JsonInclude.Include.NON_NULL, cfg.getSerializationInclusion());

        SerializationConfig sameCfg = cfg.withPropertyInclusion(incl);
        assertSame(cfg, sameCfg);

        @SuppressWarnings("deprecation")
        SerializationConfig legacyCfg = _config.withSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        assertEquals(JsonInclude.Include.NON_EMPTY, legacyCfg.getSerializationInclusion());
    }

    // Tests JsonGenerator.Feature configuration with single, varargs, and without
    @Test
    public void testWithJsonGeneratorFeatures_setsAndChecksFeature() {
        SerializationConfig cfg = _config.with(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        SerializationConfig sameCfg = cfg.with(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        assertSame(cfg, sameCfg);

        JsonFactory factory = new JsonFactory();
        assertTrue(cfg.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, factory));

        cfg = cfg.without(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        assertFalse(cfg.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, factory));

        sameCfg = cfg.without(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        assertSame(cfg, sameCfg);

        cfg = cfg.withFeatures(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, JsonGenerator.Feature.ESCAPE_NON_ASCII);
        assertTrue(cfg.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII, factory));

        cfg = cfg.withoutFeatures(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, JsonGenerator.Feature.ESCAPE_NON_ASCII);
        assertFalse(cfg.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, factory));
    }

    // Tests isEnabled with JsonFactory fallback
    @Test
    public void testIsEnabledJsonGeneratorFeatureWithFactory_fallbackAndOverride_worksCorrectly() {
        JsonFactory factory = new JsonFactory();
        factory.enable(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);

        assertTrue(_config.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION, factory));

        SerializationConfig cfg = _config.without(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
        assertFalse(cfg.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION, factory));
    }

    // Tests pretty printer construction and blueprint getter
    @Test
    public void testConstructDefaultPrettyPrinter_defaultAndCustom_returnsInstance() {
        PrettyPrinter pp = _config.getDefaultPrettyPrinter();
        assertNotNull(pp);

        PrettyPrinter instance = _config.constructDefaultPrettyPrinter();
        assertNotNull(instance);

        PrettyPrinter customPP = new DefaultPrettyPrinter();
        SerializationConfig cfg = _config.withDefaultPrettyPrinter(customPP);
        assertSame(customPP, cfg.getDefaultPrettyPrinter());

        SerializationConfig sameCfg = cfg.withDefaultPrettyPrinter(customPP);
        assertSame(cfg, sameCfg);

        SerializationConfig nullPPCfg = _config.withDefaultPrettyPrinter(null);
        assertNull(nullPPCfg.getDefaultPrettyPrinter());
        assertNull(nullPPCfg.constructDefaultPrettyPrinter());
    }

    // Tests initialize JsonGenerator methods
    @Test
    public void testInitializeJsonGenerator_configuresGenerator() throws Exception {
        JsonFactory factory = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator gen = factory.createGenerator(writer);

        SerializationConfig cfg = _config.with(SerializationFeature.INDENT_OUTPUT);
        cfg.initialize(gen);
        assertNotNull(gen.getPrettyPrinter());
        gen.close();

        StringWriter writer2 = new StringWriter();
        JsonGenerator gen2 = factory.createGenerator(writer2);
        PrettyPrinter customPP = new DefaultPrettyPrinter();
        _config.initialize(gen2, customPP);
        assertSame(customPP, gen2.getPrettyPrinter());
        gen2.close();
    }

    // Tests AnnotationIntrospector when USE_ANNOTATIONS is enabled vs disabled
    @Test
    public void testGetAnnotationIntrospector_withUseAnnotationsEnabledAndDisabled_returnsIntrospectorOrNop() {
        SerializationConfig cfg = _config.with(MapperFeature.USE_ANNOTATIONS);
        assertNotNull(cfg.getAnnotationIntrospector());
        assertNotSame(AnnotationIntrospector.nopInstance(), cfg.getAnnotationIntrospector());

        cfg = _config.without(MapperFeature.USE_ANNOTATIONS);
        assertSame(AnnotationIntrospector.nopInstance(), cfg.getAnnotationIntrospector());
    }

    // Tests getDefaultVisibilityChecker with auto-detect flags disabled
    @Test
    public void testGetDefaultVisibilityChecker_withAutoDetectFlagsDisabled_adjustsVisibility() {
        SerializationConfig cfg = _config.without(MapperFeature.AUTO_DETECT_GETTERS)
                .without(MapperFeature.AUTO_DETECT_IS_GETTERS)
                .without(MapperFeature.AUTO_DETECT_FIELDS);

        VisibilityChecker<?> vc = cfg.getDefaultVisibilityChecker();
        assertNotNull(vc);
    }

    // Tests hasSerializationFeatures bitmask check and getSerializationFeatures
    @Test
    public void testHasSerializationFeatures_checksBitmask_returnsCorrectly() {
        int mask = SerializationFeature.INDENT_OUTPUT.getMask() | SerializationFeature.FAIL_ON_EMPTY_BEANS.getMask();
        SerializationConfig cfg = _config.with(SerializationFeature.INDENT_OUTPUT, SerializationFeature.FAIL_ON_EMPTY_BEANS);

        assertTrue(cfg.hasSerializationFeatures(mask));
        assertEquals(cfg.getSerializationFeatures(), cfg.getSerializationFeatures() | mask);

        cfg = cfg.without(SerializationFeature.INDENT_OUTPUT);
        assertFalse(cfg.hasSerializationFeatures(mask));
    }

    // Tests context attributes and base settings fluent methods
    @Test
    public void testWithContextAttributesAndBaseSettings_updatesConfigCorrectly() {
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute("key", "val");
        SerializationConfig cfg = _config.with(attrs);
        assertEquals("val", cfg.getAttributes().getAttribute("key"));

        SerializationConfig sameCfg = cfg.with(attrs);
        assertSame(cfg, sameCfg);

        cfg = _config.with(Locale.GERMANY);
        assertEquals(Locale.GERMANY, cfg.getLocale());

        cfg = _config.with(TimeZone.getTimeZone("GMT+2"));
        assertEquals(TimeZone.getTimeZone("GMT+2"), cfg.getTimeZone());

        cfg = _config.with(Base64Variants.MIME);
        assertEquals(Base64Variants.MIME, cfg.getBase64Variant());

        cfg = _config.with(new StdSubtypeResolver());
        assertNotNull(cfg.getSubtypeResolver());

        cfg = _config.withVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        assertNotNull(cfg);
    }

    // Tests introspect methods and default format
    @Test
    public void testIntrospect_returnsBeanDescription() {
        JavaType type = _config.constructType(String.class);
        BeanDescription desc = _config.introspect(type);
        assertNotNull(desc);
        assertEquals(String.class, desc.getBeanClass());

        BeanDescription classDesc = _config.introspectClassAnnotations(type);
        assertNotNull(classDesc);

        BeanDescription directDesc = _config.introspectDirectClassAnnotations(type);
        assertNotNull(directDesc);

        BeanDescription classDescFromClass = _config.introspectClassAnnotations(String.class);
        assertNotNull(classDescFromClass);

        BeanDescription directDescFromClass = _config.introspectDirectClassAnnotations(String.class);
        assertNotNull(directDescFromClass);

        assertNotNull(_config.getDefaultPropertyFormat(String.class));
    }

    // Tests toString method contains flags
    @Test
    public void testToString_returnsNonNullStringContainingFlags() {
        String str = _config.toString();
        assertNotNull(str);
        assertTrue(str.startsWith("[SerializationConfig: flags=0x"));
    }
}