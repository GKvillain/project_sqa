package com.fasterxml.jackson.databind;

import org.junit.Test;
import static org.junit.Assert.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyName;

public class SerializationConfigTest {

    // Helper method to create a basic configuration for testing
    private SerializationConfig createConfig() {
        BaseSettings base = new BaseSettings();
        SubtypeResolver str = new SubtypeResolver();
        SimpleMixInResolver mixins = new SimpleMixInResolver(null);
        RootNameLookup rootNames = new RootNameLookup();
        return new SerializationConfig(base, str, mixins, rootNames);
    }

    // Tests constructor with default values
    @Test
    public void testConstructor_defaultValues_createsConfig() {
        SerializationConfig config = createConfig();
        assertNotNull(config);
        assertNull(config.getFilterProvider());
        assertNotNull(config.getDefaultPrettyPrinter());
        assertFalse(config.isEnabled(SerializationFeature.INDENT_OUTPUT));
    }

    // Tests with(SerializationFeature) enables the feature
    @Test
    public void testWith_serializationFeature_enablesFeature() {
        SerializationConfig config = createConfig();
        assertFalse(config.isEnabled(SerializationFeature.INDENT_OUTPUT));
        SerializationConfig newConfig = config.with(SerializationFeature.INDENT_OUTPUT);
        assertTrue(newConfig.isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertNotSame(config, newConfig);
    }

    // Tests without(SerializationFeature) disables the feature
    @Test
    public void testWithout_serializationFeature_disablesFeature() {
        SerializationConfig config = createConfig().with(SerializationFeature.INDENT_OUTPUT);
        assertTrue(config.isEnabled(SerializationFeature.INDENT_OUTPUT));
        SerializationConfig newConfig = config.without(SerializationFeature.INDENT_OUTPUT);
        assertFalse(newConfig.isEnabled(SerializationFeature.INDENT_OUTPUT));
    }

    // Tests with(SerializationFeature) returns same instance if already enabled
    @Test
    public void testWith_serializationFeatureAlreadyEnabled_returnsSameInstance() {
        SerializationConfig config = createConfig().with(SerializationFeature.INDENT_OUTPUT);
        SerializationConfig newConfig = config.with(SerializationFeature.INDENT_OUTPUT);
        assertSame(config, newConfig);
    }

    // Tests withFeatures(SerializationFeature...) enables multiple features
    @Test
    public void testWithFeatures_multipleFeatures_enablesAll() {
        SerializationConfig config = createConfig();
        SerializationConfig newConfig = config.withFeatures(
            SerializationFeature.INDENT_OUTPUT,
            SerializationFeature.WRAP_ROOT_VALUE
        );
        assertTrue(newConfig.isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertTrue(newConfig.isEnabled(SerializationFeature.WRAP_ROOT_VALUE));
    }

    // Tests withoutFeatures(SerializationFeature...) disables multiple features
    @Test
    public void testWithoutFeatures_multipleFeatures_disablesAll() {
        SerializationConfig config = createConfig()
            .withFeatures(SerializationFeature.INDENT_OUTPUT, SerializationFeature.WRAP_ROOT_VALUE);
        SerializationConfig newConfig = config.withoutFeatures(
            SerializationFeature.INDENT_OUTPUT,
            SerializationFeature.WRAP_ROOT_VALUE
        );
        assertFalse(newConfig.isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertFalse(newConfig.isEnabled(SerializationFeature.WRAP_ROOT_VALUE));
    }

    // Tests withFilters returns new instance with different filter provider
    @Test
    public void testWithFilters_newProvider_returnsNewConfig() {
        SerializationConfig config = createConfig();
        assertNull(config.getFilterProvider());
        FilterProvider fp = new FilterProvider();
        SerializationConfig newConfig = config.withFilters(fp);
        assertSame(fp, newConfig.getFilterProvider());
        assertNotSame(config, newConfig);
    }

    // Tests withFilters returns same instance if same provider
    @Test
    public void testWithFilters_sameProvider_returnsSameInstance() {
        FilterProvider fp = new FilterProvider();
        SerializationConfig config = createConfig().withFilters(fp);
        SerializationConfig newConfig = config.withFilters(fp);
        assertSame(config, newConfig);
    }

    // Tests with(MapperFeature) enables the mapper feature
    @Test
    public void testWith_mapperFeature_enablesFeature() {
        SerializationConfig config = createConfig();
        SerializationConfig newConfig = config.with(MapperFeature.USE_ANNOTATIONS);
        assertNotNull(newConfig);
        assertNotSame(config, newConfig);
    }

    // Tests without(MapperFeature) disables the mapper feature
    @Test
    public void testWithout_mapperFeature_disablesFeature() {
        SerializationConfig config = createConfig();
        SerializationConfig newConfig = config.without(MapperFeature.USE_ANNOTATIONS);
        assertNotNull(newConfig);
        assertNotSame(config, newConfig);
    }

    // Tests with(JsonGenerator.Feature) enables generator feature
    @Test
    public void testWith_generatorFeature_enablesFeature() {
        SerializationConfig config = createConfig();
        SerializationConfig newConfig = config.with(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        assertNotNull(newConfig);
        assertNotSame(config, newConfig);
    }

    // Tests without(JsonGenerator.Feature) disables generator feature
    @Test
    public void testWithout_generatorFeature_disablesFeature() {
        SerializationConfig config = createConfig().with(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        SerializationConfig newConfig = config.without(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        assertNotNull(newConfig);
        assertNotSame(config, newConfig);
    }

    // Tests with(FormatFeature) enables format feature
    @Test
    public void testWith_formatFeature_enablesFeature() {
        SerializationConfig config = createConfig();
        SerializationConfig newConfig = config.with(new FormatFeature() {
            public boolean enabledByDefault() { return false; }
            public boolean enabledIn(int flags) { return (flags & 1) != 0; }
            public int getMask() { return 1; }
        });
        assertNotNull(newConfig);
        assertNotSame(config, newConfig);
    }

    // Tests withDefaultPrettyPrinter changes pretty printer
    @Test
    public void testWithDefaultPrettyPrinter_newPrinter_returnsNewConfig() {
        SerializationConfig config = createConfig();
        PrettyPrinter pp = new DefaultPrettyPrinter();
        SerializationConfig newConfig = config.withDefaultPrettyPrinter(pp);
        assertSame(pp, newConfig.getDefaultPrettyPrinter());
        assertNotSame(config, newConfig);
    }

    // Tests withDefaultPrettyPrinter returns same instance if same printer
    @Test
    public void testWithDefaultPrettyPrinter_samePrinter_returnsSameInstance() {
        PrettyPrinter pp = new DefaultPrettyPrinter();
        SerializationConfig config = createConfig().withDefaultPrettyPrinter(pp);
        SerializationConfig newConfig = config.withDefaultPrettyPrinter(pp);
        assertSame(config, newConfig);
    }

    // Tests withPropertyInclusion changes inclusion setting
    @Test
    public void testWithPropertyInclusion_newValue_returnsNewConfig() {
        SerializationConfig config = createConfig();
        JsonInclude.Value incl = JsonInclude.Value.empty().withValueInclusion(JsonInclude.Include.NON_NULL);
        SerializationConfig newConfig = config.withPropertyInclusion(incl);
        assertEquals(incl, newConfig.getDefaultPropertyInclusion());
        assertNotSame(config, newConfig);
    }

    // Tests withPropertyInclusion returns same instance if same value
    @Test
    public void testWithPropertyInclusion_sameValue_returnsSameInstance() {
        JsonInclude.Value incl = JsonInclude.Value.empty();
        SerializationConfig config = createConfig().withPropertyInclusion(incl);
        SerializationConfig newConfig = config.withPropertyInclusion(incl);
        assertSame(config, newConfig);
    }

    // Tests hasSerializationFeatures checks masked features
    @Test
    public void testHasSerializationFeatures_multipleFeatures_returnsCorrectResult() {
        SerializationConfig config = createConfig()
            .with(SerializationFeature.INDENT_OUTPUT)
            .with(SerializationFeature.WRAP_ROOT_VALUE);
        int mask = SerializationFeature.INDENT_OUTPUT.getMask() | SerializationFeature.WRAP_ROOT_VALUE.getMask();
        assertTrue(config.hasSerializationFeatures(mask));
        int partialMask = SerializationFeature.INDENT_OUTPUT.getMask();
        assertTrue(config.hasSerializationFeatures(partialMask));
    }

    // Tests hasSerializationFeatures returns false if not all features enabled
    @Test
    public void testHasSerializationFeatures_notAllEnabled_returnsFalse() {
        SerializationConfig config = createConfig().with(SerializationFeature.INDENT_OUTPUT);
        int mask = SerializationFeature.INDENT_OUTPUT.getMask() | SerializationFeature.WRAP_ROOT_VALUE.getMask();
        assertFalse(config.hasSerializationFeatures(mask));
    }

    // Tests toString returns correct format
    @Test
    public void testToString_returnsFormattedString() {
        SerializationConfig config = createConfig();
        String str = config.toString();
        assertTrue(str.startsWith("[SerializationConfig: flags=0x"));
        assertTrue(str.endsWith("]"));
    }

    // Tests useRootWrapping returns false when root name is empty
    @Test
    public void testUseRootWrapping_emptyRootName_returnsFalse() {
        SerializationConfig config = createConfig().withRootName(new PropertyName(""));
        assertFalse(config.useRootWrapping());
    }

    // Tests useRootWrapping returns true when root name is non-empty
    @Test
    public void testUseRootWrapping_nonEmptyRootName_returnsTrue() {
        SerializationConfig config = createConfig().withRootName(new PropertyName("root"));
        assertTrue(config.useRootWrapping());
    }

    // ===== New test cases for uncovered areas =====

    // Tests withRootName with a new name returns a new config
    @Test
    public void testWithRootName_newName_returnsNewConfig() {
        SerializationConfig config = createConfig();
        PropertyName name = new PropertyName("myRoot");
        SerializationConfig newConfig = config.withRootName(name);
        assertNotSame(config, newConfig);
        assertEquals(name, newConfig.getRootName());
    }

    // Tests withRootName with the same name returns the same instance
    @Test
    public void testWithRootName_sameName_returnsSameInstance() {
        PropertyName name = new PropertyName("myRoot");
        SerializationConfig config = createConfig().withRootName(name);
        SerializationConfig newConfig = config.withRootName(name);
        assertSame(config, newConfig);
    }

    // Tests withView with a new view returns a new config
    @Test
    public void testWithView_newView_returnsNewConfig() {
        SerializationConfig config = createConfig();
        Class<?> view = Object.class;
        SerializationConfig newConfig = config.withView(view);
        assertNotSame(config, newConfig);
        assertSame(view, newConfig.getView());
    }

    // Tests withView with the same view returns the same instance
    @Test
    public void testWithView_sameView_returnsSameInstance() {
        Class<?> view = Object.class;
        SerializationConfig config = createConfig().withView(view);
        SerializationConfig newConfig = config.withView(view);
        assertSame(config, newConfig);
    }

    // Tests withDefaultPropertyFormat with a new value returns a new config
    @Test
    public void testWithDefaultPropertyFormat_newValue_returnsNewConfig() {
        SerializationConfig config = createConfig();
        JsonFormat.Value format = JsonFormat.Value.empty();
        SerializationConfig newConfig = config.withDefaultPropertyFormat(format);
        assertNotSame(config, newConfig);
        assertEquals(format, newConfig.getDefaultPropertyFormat());
    }

    // Tests withDefaultPropertyFormat with the same value returns the same instance
    @Test
    public void testWithDefaultPropertyFormat_sameValue_returnsSameInstance() {
        JsonFormat.Value format = JsonFormat.Value.empty();
        SerializationConfig config = createConfig().withDefaultPropertyFormat(format);
        SerializationConfig newConfig = config.withDefaultPropertyFormat(format);
        assertSame(config, newConfig);
    }
}