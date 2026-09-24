package com.fasterxml.jackson.databind;

import java.io.StringWriter;
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
import com.fasterxml.jackson.databind.type.TypeFactory;

public class SerializationConfigTest {

    private ObjectMapper _mapper;
    private SerializationConfig _config;

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _config = _mapper.getSerializationConfig();
    }

    // Tests default feature states and flags initialization
    @Test
    public void testDefaults_initializedConfig_correctInitialStates() {
        assertNotNull(_config);
        assertTrue(_config.isEnabled(MapperFeature.USE_ANNOTATIONS));
        assertTrue(_config.isEnabled(MapperFeature.AUTO_DETECT_GETTERS));
        assertTrue(_config.isEnabled(MapperFeature.AUTO_DETECT_IS_GETTERS));
        assertTrue(_config.isEnabled(MapperFeature.AUTO_DETECT_FIELDS));
        assertNull(_config.getFilterProvider());
        assertNotNull(_config.getDefaultPrettyPrinter());
        assertNotNull(_config.getDefaultPropertyInclusion());
        assertEquals(JsonInclude.Include.ALWAYS, _config.getSerializationInclusion());
        assertNotNull(_config.toString());
    }

    // Tests enabling and disabling MapperFeature flags
    @Test
    public void testWithMapperFeatures_toggleFeatures_returnsUpdatedInstance() {
        SerializationConfig cfg1 = _config.with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        assertTrue(cfg1.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));

        // Idempotent with
        assertSame(cfg1, cfg1.with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));

        SerializationConfig cfg2 = cfg1.without(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        assertFalse(cfg2.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));

        // Idempotent without
        assertSame(cfg2, cfg2.without(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));

        SerializationConfig cfg3 = _config.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        assertTrue(cfg3.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));

        SerializationConfig cfg4 = cfg3.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, false);
        assertFalse(cfg4.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
    }

    // Tests enabling and disabling SerializationFeature flags
    @Test
    public void testWithSerializationFeatures_toggleFeatures_returnsUpdatedInstance() {
        SerializationConfig cfg = _config.with(SerializationFeature.INDENT_OUTPUT);
        assertTrue(cfg.isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertTrue(cfg.hasSerializationFeatures(SerializationFeature.INDENT_OUTPUT.getMask()));

        // Idempotent with
        assertSame(cfg, cfg.with(SerializationFeature.INDENT_OUTPUT));

        SerializationConfig cfgWithout = cfg.without(SerializationFeature.INDENT_OUTPUT);
        assertFalse(cfgWithout.isEnabled(SerializationFeature.INDENT_OUTPUT));

        // Multi-feature with / without
        SerializationConfig cfgMulti = _config.with(SerializationFeature.INDENT_OUTPUT, SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        assertTrue(cfgMulti.isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertTrue(cfgMulti.isEnabled(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS));

        SerializationConfig cfgMultiWithout = cfgMulti.without(SerializationFeature.INDENT_OUTPUT, SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        assertFalse(cfgMultiWithout.isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertFalse(cfgMultiWithout.isEnabled(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS));

        SerializationConfig cfgWithFeatures = _config.withFeatures(SerializationFeature.WRAP_ROOT_VALUE);
        assertTrue(cfgWithFeatures.isEnabled(SerializationFeature.WRAP_ROOT_VALUE));

        SerializationConfig cfgWithoutFeatures = cfgWithFeatures.withoutFeatures(SerializationFeature.WRAP_ROOT_VALUE);
        assertFalse(cfgWithoutFeatures.isEnabled(SerializationFeature.WRAP_ROOT_VALUE));
    }

    // Tests with and without JsonGenerator.Feature settings
    @Test
    public void testWithJsonGeneratorFeatures_toggleFeatures_updatesMaskAndState() {
        JsonFactory factory = new JsonFactory();

        SerializationConfig cfg = _config.with(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertTrue(cfg.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES, factory));
        assertSame(cfg, cfg.with(JsonGenerator.Feature.QUOTE_FIELD_NAMES));

        SerializationConfig cfgWithout = cfg.without(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertFalse(cfgWithout.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES, factory));
        assertSame(cfgWithout, cfgWithout.without(JsonGenerator.Feature.QUOTE_FIELD_NAMES));

        SerializationConfig cfgMulti = _config.withFeatures(JsonGenerator.Feature.ESCAPE_NON_ASCII, JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
        assertTrue(cfgMulti.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII, factory));
        assertTrue(cfgMulti.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION, factory));

        SerializationConfig cfgMultiWithout = cfgMulti.withoutFeatures(JsonGenerator.Feature.ESCAPE_NON_ASCII, JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
        assertFalse(cfgMultiWithout.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII, factory));
        assertFalse(cfgMultiWithout.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION, factory));
    }

    // Tests DateFormat configuration and automatic WRITE_DATES_AS_TIMESTAMPS toggle
    @Test
    public void testWithDateFormat_nullAndNonNull_togglesWriteDatesAsTimestamps() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SerializationConfig cfgWithDf = _config.with(df);
        assertFalse(cfgWithDf.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));

        SerializationConfig cfgNullDf = cfgWithDf.with((java.text.DateFormat) null);
        assertTrue(cfgNullDf.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    // Tests root name setting and useRootWrapping branches
    @Test
    public void testWithRootName_variousInputs_handlesWrappingCorrectly() {
        assertFalse(_config.useRootWrapping());

        SerializationConfig cfgSame = _config.withRootName((PropertyName) null);
        assertSame(_config, cfgSame);

        PropertyName rootName = PropertyName.construct("customRoot");
        SerializationConfig cfgWithRoot = _config.withRootName(rootName);
        assertTrue(cfgWithRoot.useRootWrapping());
        assertSame(cfgWithRoot, cfgWithRoot.withRootName(rootName));

        PropertyName emptyRoot = PropertyName.construct("");
        SerializationConfig cfgEmptyRoot = _config.withRootName(emptyRoot);
        assertFalse(cfgEmptyRoot.useRootWrapping());

        SerializationConfig cfgWrapRoot = _config.with(SerializationFeature.WRAP_ROOT_VALUE);
        assertTrue(cfgWrapRoot.useRootWrapping());
    }

    // Tests AnnotationIntrospector getter when USE_ANNOTATIONS is enabled vs disabled
    @Test
    public void testGetAnnotationIntrospector_withAndWithoutUseAnnotations_returnsIntrospectorOrNop() {
        AnnotationIntrospector ai = _config.getAnnotationIntrospector();
        assertNotNull(ai);
        assertNotSame(AnnotationIntrospector.nopInstance(), ai);

        SerializationConfig cfgNoAnn = _config.without(MapperFeature.USE_ANNOTATIONS);
        assertSame(AnnotationIntrospector.nopInstance(), cfgNoAnn.getAnnotationIntrospector());
    }

    // Tests getDefaultVisibilityChecker when auto-detect features are disabled
    @Test
    public void testGetDefaultVisibilityChecker_disabledAutoDetect_returnsRestrictedChecker() {
        SerializationConfig cfg = _config
                .without(MapperFeature.AUTO_DETECT_GETTERS)
                .without(MapperFeature.AUTO_DETECT_IS_GETTERS)
                .without(MapperFeature.AUTO_DETECT_FIELDS);

        VisibilityChecker<?> vc = cfg.getDefaultVisibilityChecker();
        assertNotNull(vc);

        SerializationConfig cfgDefault = _config;
        VisibilityChecker<?> vcDefault = cfgDefault.getDefaultVisibilityChecker();
        assertNotNull(vcDefault);
    }

    // Tests withFilters and getFilterProvider
    @Test
    public void testWithFilters_customFilterProvider_setsAndReturnsProvider() {
        SimpleFilterProvider provider = new SimpleFilterProvider();
        SerializationConfig cfg = _config.withFilters(provider);
        assertSame(provider, cfg.getFilterProvider());
        assertSame(cfg, cfg.withFilters(provider));
    }

    // Tests property inclusion methods and overrides
    @Test
    public void testPropertyInclusion_variousInclusions_returnsExpectedValues() {
        JsonInclude.Value incl = JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_EMPTY);
        SerializationConfig cfg = _config.withPropertyInclusion(incl);
        assertEquals(incl, cfg.getDefaultPropertyInclusion());
        assertEquals(incl, cfg.getDefaultPropertyInclusion(String.class));
        assertSame(cfg, cfg.withPropertyInclusion(incl));

        SerializationConfig cfgDeprecated = _config.withSerializationInclusion(JsonInclude.Include.NON_NULL);
        assertEquals(JsonInclude.Include.NON_NULL, cfgDeprecated.getSerializationInclusion());
    }

    // Tests pretty printer configuration and instantiation
    @Test
    public void testDefaultPrettyPrinter_customPrinter_configuresCorrectly() {
        PrettyPrinter pp = new DefaultPrettyPrinter();
        SerializationConfig cfg = _config.withDefaultPrettyPrinter(pp);
        assertSame(pp, cfg.getDefaultPrettyPrinter());
        assertSame(cfg, cfg.withDefaultPrettyPrinter(pp));

        PrettyPrinter constructed = cfg.constructDefaultPrettyPrinter();
        assertNotNull(constructed);
    }

    // Tests generator initialization logic with INDENT_OUTPUT and WRITE_BIGDECIMAL_AS_PLAIN
    @Test
    public void testInitialize_generatorSettings_appliesConfigurations() throws Exception {
        JsonFactory f = new JsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator g = f.createGenerator(sw);

        SerializationConfig cfg = _config.with(SerializationFeature.INDENT_OUTPUT)
                .with(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN)
                .with(JsonGenerator.Feature.ESCAPE_NON_ASCII);

        cfg.initialize(g);
        assertNotNull(g.getPrettyPrinter());
        g.close();
    }

    // Tests miscellaneous fluent "with" methods for base settings
    @Test
    public void testWithBaseSettings_variousSettings_returnsUpdatedConfig() {
        AnnotationIntrospector ai = AnnotationIntrospector.nopInstance();
        assertNotNull(_config.with(ai));
        assertNotNull(_config.withAppendedAnnotationIntrospector(ai));
        assertNotNull(_config.withInsertedAnnotationIntrospector(ai));
        assertNotNull(_config.with(_config.getClassIntrospector()));
        assertNotNull(_config.with(PropertyNamingStrategy.UPPER_CAMEL_CASE));
        assertNotNull(_config.with(new StdSubtypeResolver()));
        assertNotNull(_config.with(TypeFactory.defaultInstance()));
        assertNotNull(_config.withView(String.class));
        assertSame(_config.withView(String.class), _config.withView(String.class).withView(String.class));
        assertNotNull(_config.with(Locale.GERMANY));
        assertNotNull(_config.with(TimeZone.getTimeZone("GMT")));
        assertNotNull(_config.with(Base64Variants.MIME_NO_LINEFEEDS));
        assertNotNull(_config.with(ContextAttributes.getEmpty()));
        assertNotNull(_config.withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY));
    }

    // Tests introspection helper methods
    @Test
    public void testIntrospect_validJavaType_returnsBeanDescription() {
        JavaType type = _config.constructType(String.class);
        BeanDescription desc = _config.introspect(type);
        assertNotNull(desc);
        assertEquals(String.class, desc.getBeanClass());

        BeanDescription classDesc = _config.introspectClassAnnotations(type);
        assertNotNull(classDesc);

        BeanDescription directDesc = _config.introspectDirectClassAnnotations(type);
        assertNotNull(directDesc);
    }
}