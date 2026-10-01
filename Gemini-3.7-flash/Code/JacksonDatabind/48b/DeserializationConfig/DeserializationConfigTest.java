package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class DeserializationConfigTest {

    private ObjectMapper mapper;
    private DeserializationConfig config;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        config = mapper.getDeserializationConfig();
    }

    // Tests enabling and disabling MapperFeatures via fluent factory methods
    @Test
    public void testWithAndWithoutMapperFeatures_singleAndMultiple_modifiesStateCorrectly() {
        assertFalse(config.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));

        // Test with(MapperFeature...)
        DeserializationConfig cfg2 = config.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        assertNotSame(config, cfg2);
        assertTrue(cfg2.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));

        // Test with identical feature returns same instance
        assertSame(cfg2, cfg2.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));

        // Test with(MapperFeature, boolean)
        DeserializationConfig cfg3 = cfg2.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, false);
        assertFalse(cfg3.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
        assertSame(cfg3, cfg3.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, false));

        // Test without(MapperFeature...)
        DeserializationConfig cfg4 = cfg2.without(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        assertFalse(cfg4.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
        assertSame(cfg4, cfg4.without(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
    }

    // Tests enabling and disabling DeserializationFeatures
    @Test
    public void testWithAndWithoutDeserializationFeatures_modifiesStateCorrectly() {
        assertTrue(config.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        // Test without(DeserializationFeature)
        DeserializationConfig cfg2 = config.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertNotSame(config, cfg2);
        assertFalse(cfg2.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertSame(cfg2, cfg2.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        // Test with(DeserializationFeature)
        DeserializationConfig cfg3 = cfg2.with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertTrue(cfg3.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertSame(cfg3, cfg3.with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        // Test with(DeserializationFeature, DeserializationFeature...)
        DeserializationConfig cfg4 = cfg2.with(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT
        );
        assertTrue(cfg4.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertTrue(cfg4.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));

        // Test without(DeserializationFeature, DeserializationFeature...)
        DeserializationConfig cfg5 = cfg4.without(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT
        );
        assertFalse(cfg5.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertFalse(cfg5.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));

        // Test withFeatures and withoutFeatures
        DeserializationConfig cfg6 = cfg5.withFeatures(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        assertTrue(cfg6.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
        DeserializationConfig cfg7 = cfg6.withoutFeatures(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        assertFalse(cfg7.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
    }

    // Tests checking bulk DeserializationFeature masks
    @Test
    public void testHasDeserializationFeatures_andHasSomeOfFeatures_evaluatesMaskCorrectly() {
        int mask = DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES.getMask()
                | DeserializationFeature.WRAP_EXCEPTIONS.getMask();
        assertTrue(config.hasDeserializationFeatures(mask));
        assertTrue(config.hasSomeOfFeatures(mask));

        DeserializationConfig cfg = config.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertFalse(cfg.hasDeserializationFeatures(mask));
        assertTrue(cfg.hasSomeOfFeatures(mask));

        assertTrue(cfg.getDeserializationFeatures() > 0);
    }

    // Tests JsonParser.Feature configuration and check with JsonFactory
    @Test
    public void testWithAndWithoutJsonParserFeatures_modifiesParserFeatures() {
        JsonFactory jf = new JsonFactory();
        assertFalse(config.isEnabled(JsonParser.Feature.ALLOW_COMMENTS, jf));

        DeserializationConfig cfg2 = config.with(JsonParser.Feature.ALLOW_COMMENTS);
        assertNotSame(config, cfg2);
        assertTrue(cfg2.isEnabled(JsonParser.Feature.ALLOW_COMMENTS, jf));
        assertSame(cfg2, cfg2.with(JsonParser.Feature.ALLOW_COMMENTS));

        DeserializationConfig cfg3 = cfg2.without(JsonParser.Feature.ALLOW_COMMENTS);
        assertFalse(cfg3.isEnabled(JsonParser.Feature.ALLOW_COMMENTS, jf));
        assertSame(cfg3, cfg3.without(JsonParser.Feature.ALLOW_COMMENTS));

        DeserializationConfig cfg4 = config.withFeatures(
                JsonParser.Feature.ALLOW_COMMENTS,
                JsonParser.Feature.ALLOW_SINGLE_QUOTES
        );
        assertTrue(cfg4.isEnabled(JsonParser.Feature.ALLOW_COMMENTS, jf));
        assertTrue(cfg4.isEnabled(JsonParser.Feature.ALLOW_SINGLE_QUOTES, jf));

        DeserializationConfig cfg5 = cfg4.withoutFeatures(
                JsonParser.Feature.ALLOW_COMMENTS,
                JsonParser.Feature.ALLOW_SINGLE_QUOTES
        );
        assertFalse(cfg5.isEnabled(JsonParser.Feature.ALLOW_COMMENTS, jf));
        assertFalse(cfg5.isEnabled(JsonParser.Feature.ALLOW_SINGLE_QUOTES, jf));
    }

    // Tests initialize(JsonParser) with configured parser features
    @Test
    public void testInitialize_withConfiguredParserFeatures_overridesParserSettings() throws Exception {
        DeserializationConfig cfg = config.with(JsonParser.Feature.ALLOW_COMMENTS);
        JsonParser parser = mapper.getFactory().createParser(new StringReader("{}"));
        assertFalse(parser.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));

        cfg.initialize(parser);
        assertTrue(parser.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        parser.close();
    }

    // Tests nodeFactory assignment and immutability
    @Test
    public void testWithJsonNodeFactory_differentAndSameInstance_handlesCorrectly() {
        JsonNodeFactory defaultF = config.getNodeFactory();
        assertNotNull(defaultF);
        assertSame(config, config.with(defaultF));

        JsonNodeFactory customF = new JsonNodeFactory(true);
        DeserializationConfig cfg2 = config.with(customF);
        assertNotSame(config, cfg2);
        assertSame(customF, cfg2.getNodeFactory());
    }

    // Tests DeserializationProblemHandler registration and clearing
    @Test
    public void testWithHandler_andWithNoProblemHandlers_managesHandlerChain() {
        assertNull(config.getProblemHandlers());
        assertSame(config, config.withNoProblemHandlers());

        DeserializationProblemHandler handler1 = new DeserializationProblemHandler() {};
        DeserializationConfig cfg1 = config.withHandler(handler1);
        assertNotNull(cfg1.getProblemHandlers());
        assertSame(handler1, cfg1.getProblemHandlers().value());

        // Prevent duplicate handler addition
        assertSame(cfg1, cfg1.withHandler(handler1));

        DeserializationProblemHandler handler2 = new DeserializationProblemHandler() {};
        DeserializationConfig cfg2 = cfg1.withHandler(handler2);
        assertSame(handler2, cfg2.getProblemHandlers().value());
        assertSame(handler1, cfg2.getProblemHandlers().next().value());

        DeserializationConfig cfgCleared = cfg2.withNoProblemHandlers();
        assertNull(cfgCleared.getProblemHandlers());
    }

    // Tests root name setting and root wrapping calculation
    @Test
    public void testWithRootName_andUseRootWrapping_returnsExpectedValues() {
        assertFalse(config.useRootWrapping());

        PropertyName rootName = PropertyName.construct("customRoot");
        DeserializationConfig cfgWithRoot = config.withRootName(rootName);
        assertNotSame(config, cfgWithRoot);
        assertTrue(cfgWithRoot.useRootWrapping());
        assertSame(cfgWithRoot, cfgWithRoot.withRootName(rootName));

        // Empty root name disables wrapping
        DeserializationConfig cfgEmptyRoot = config.withRootName(PropertyName.construct(""));
        assertFalse(cfgEmptyRoot.useRootWrapping());

        // Null root name with UNWRAP_ROOT_VALUE enabled
        DeserializationConfig cfgNullRoot = config.withRootName((PropertyName) null);
        assertSame(config, cfgNullRoot);

        // String overload of withRootName
        DeserializationConfig cfgStringRoot = config.withRootName("strRoot");
        assertNotSame(config, cfgStringRoot);
        assertEquals("strRoot", cfgStringRoot.getRootName().getSimpleName());

        DeserializationConfig cfgNullStringRoot = config.withRootName((String) null);
        assertNull(cfgNullStringRoot.getRootName());

        DeserializationConfig cfgUnwrap = config.with(DeserializationFeature.UNWRAP_ROOT_VALUE);
        assertTrue(cfgUnwrap.useRootWrapping());
    }

    // Tests active view configuration
    @Test
    public void testWithView_differentAndSameClass_returnsCorrectInstance() {
        assertNull(config.getActiveView());
        assertSame(config, config.withView((Class<?>) null));

        DeserializationConfig cfgView = config.withView(String.class);
        assertNotSame(config, cfgView);
        assertEquals(String.class, cfgView.getActiveView());
        assertSame(cfgView, cfgView.withView(String.class));
    }

    // Tests context attributes configuration
    @Test
    public void testWithContextAttributes_differentAndSameInstance_returnsCorrectInstance() {
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute("k1", "v1");
        DeserializationConfig cfgAttrs = config.with(attrs);
        assertNotSame(config, cfgAttrs);
        assertEquals("v1", cfgAttrs.getAttributes().getAttribute("k1"));
        assertSame(cfgAttrs, cfgAttrs.with(attrs));
    }

    // Tests annotation introspector retrieval when annotations are enabled vs disabled
    @Test
    public void testGetAnnotationIntrospector_withAndWithoutAnnotations_returnsAppropriateIntrospector() {
        assertTrue(config.isEnabled(MapperFeature.USE_ANNOTATIONS));
        assertFalse(config.getAnnotationIntrospector() instanceof NopAnnotationIntrospector);

        DeserializationConfig noAnnotCfg = config.without(MapperFeature.USE_ANNOTATIONS);
        assertTrue(noAnnotCfg.getAnnotationIntrospector() instanceof NopAnnotationIntrospector);

        AnnotationIntrospector intro = new JacksonAnnotationIntrospector();
        DeserializationConfig cfgWithIntro = config.with(intro);
        assertNotNull(cfgWithIntro.getAnnotationIntrospector());

        DeserializationConfig cfgAppended = config.withAppendedAnnotationIntrospector(intro);
        assertNotNull(cfgAppended.getAnnotationIntrospector());

        DeserializationConfig cfgInserted = config.withInsertedAnnotationIntrospector(intro);
        assertNotNull(cfgInserted.getAnnotationIntrospector());
    }

    // Tests default visibility checker behavior when auto-detection features are toggled
    @Test
    public void testGetDefaultVisibilityChecker_autoDetectDisabled_adjustsVisibility() {
        VisibilityChecker<?> vc = config.getDefaultVisibilityChecker();
        assertNotNull(vc);

        DeserializationConfig cfgNoAuto = config.without(
                MapperFeature.AUTO_DETECT_SETTERS,
                MapperFeature.AUTO_DETECT_CREATORS,
                MapperFeature.AUTO_DETECT_FIELDS
        );
        VisibilityChecker<?> vcNoAuto = cfgNoAuto.getDefaultVisibilityChecker();
        assertNotNull(vcNoAuto);
    }

    // Tests withVisibility method
    @Test
    public void testWithVisibility_changesVisibilityChecker() {
        DeserializationConfig cfg = config.withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        assertNotSame(config, cfg);
        assertNotNull(cfg.getDefaultVisibilityChecker());
    }

    // Tests BaseSettings fluent delegation methods (DateFormat, Locale, TimeZone, TypeFactory, SubtypeResolver, PropertyNamingStrategy)
    @Test
    public void testWithBaseSettingsDelegates_returnsUpdatedInstances() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        DeserializationConfig cfgDf = config.with(df);
        assertNotSame(config, cfgDf);
        assertEquals(df, cfgDf.getDateFormat());

        Locale locale = Locale.GERMANY;
        DeserializationConfig cfgLoc = config.with(locale);
        assertNotSame(config, cfgLoc);
        assertEquals(locale, cfgLoc.getLocale());

        TimeZone tz = TimeZone.getTimeZone("GMT+2");
        DeserializationConfig cfgTz = config.with(tz);
        assertNotSame(config, cfgTz);
        assertEquals(tz, cfgTz.getTimeZone());

        TypeFactory tf = TypeFactory.defaultInstance();
        DeserializationConfig cfgTf = config.with(tf);
        assertNotNull(cfgTf);

        SubtypeResolver str = new StdSubtypeResolver();
        DeserializationConfig cfgStr = config.with(str);
        assertNotNull(cfgStr);
        assertSame(cfgStr, cfgStr.with(str));

        PropertyNamingStrategy pns = PropertyNamingStrategy.SNAKE_CASE;
        DeserializationConfig cfgPns = config.with(pns);
        assertNotSame(config, cfgPns);
        assertSame(pns, cfgPns.getPropertyNamingStrategy());
        assertSame(cfgPns, cfgPns.with(pns));
    }

    // Tests bean introspection methods
    @Test
    public void testIntrospectMethods_returnsValidBeanDescriptions() {
        JavaType type = config.constructType(String.class);

        BeanDescription desc1 = config.introspectClassAnnotations(type);
        assertNotNull(desc1);
        assertEquals(String.class, desc1.getBeanClass());

        BeanDescription desc1Class = config.introspectClassAnnotations(String.class);
        assertNotNull(desc1Class);
        assertEquals(String.class, desc1Class.getBeanClass());

        BeanDescription desc2 = config.introspectDirectClassAnnotations(type);
        assertNotNull(desc2);
        assertEquals(String.class, desc2.getBeanClass());

        BeanDescription desc2Class = config.introspectDirectClassAnnotations(String.class);
        assertNotNull(desc2Class);
        assertEquals(String.class, desc2Class.getBeanClass());

        BeanDescription desc3 = config.introspect(type);
        assertNotNull(desc3);
        assertEquals(String.class, desc3.getBeanClass());

        BeanDescription desc4 = config.introspectForCreation(type);
        assertNotNull(desc4);
        assertEquals(String.class, desc4.getBeanClass());

        BeanDescription desc5 = config.introspectForBuilder(type);
        assertNotNull(desc5);
        assertEquals(String.class, desc5.getBeanClass());
    }

    // Tests default property inclusion and format accessors
    @Test
    public void testGetDefaultPropertyInclusionAndFormat_returnsEmptyDefaults() {
        assertNotNull(config.getDefaultPropertyInclusion());
        JsonInclude.Value defaultIncl = JsonInclude.Value.empty();
        assertEquals(defaultIncl, config.getDefaultPropertyInclusion(String.class, defaultIncl));
        assertNotNull(config.getDefaultPropertyFormat(String.class));
    }

    // Tests findTypeDeserializer for simple type without type resolver
    @Test
    public void testFindTypeDeserializer_forSimpleTypeWithoutTyper_returnsNull() throws Exception {
        JavaType type = config.constructType(String.class);
        assertNull(config.findTypeDeserializer(type));
    }
}