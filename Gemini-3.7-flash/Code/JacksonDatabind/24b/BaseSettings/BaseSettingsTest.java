package com.fasterxml.jackson.databind.cfg;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdDateFormat;

public class BaseSettingsTest {

    private BaseSettings _baseSettings;
    private DateFormat _dateFormat;
    private Locale _locale;
    private TimeZone _timeZone;
    private Base64Variant _base64;

    @Before
    public void setUp() {
        _dateFormat = new StdDateFormat();
        _locale = Locale.US;
        _timeZone = TimeZone.getTimeZone("GMT");
        _base64 = Base64Variants.MIME;

        _baseSettings = new BaseSettings(
                null,
                null,
                new VisibilityChecker.Std(JsonAutoDetect.Visibility.DEFAULT),
                null,
                TypeFactory.defaultInstance(),
                null,
                _dateFormat,
                null,
                _locale,
                _timeZone,
                _base64
        );
    }

    // Tests constructor and all getters
    @Test
    public void testGetters_validInstance_returnsInitializedValues() {
        assertNull(_baseSettings.getClassIntrospector());
        assertNull(_baseSettings.getAnnotationIntrospector());
        assertNotNull(_baseSettings.getVisibilityChecker());
        assertNull(_baseSettings.getPropertyNamingStrategy());
        assertEquals(TypeFactory.defaultInstance(), _baseSettings.getTypeFactory());
        assertNull(_baseSettings.getTypeResolverBuilder());
        assertEquals(_dateFormat, _baseSettings.getDateFormat());
        assertNull(_baseSettings.getHandlerInstantiator());
        assertEquals(_locale, _baseSettings.getLocale());
        assertEquals(_timeZone, _baseSettings.getTimeZone());
        assertEquals(_base64, _baseSettings.getBase64Variant());
    }

    // Tests withClassIntrospector same and different instance
    @Test
    public void testWithClassIntrospector_sameAndDifferent_returnsExpected() {
        assertSame(_baseSettings, _baseSettings.withClassIntrospector(null));
    }

    // Tests withAnnotationIntrospector same and different instance
    @Test
    public void testWithAnnotationIntrospector_sameAndDifferent_returnsExpected() {
        assertSame(_baseSettings, _baseSettings.withAnnotationIntrospector(null));

        AnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        BaseSettings updated = _baseSettings.withAnnotationIntrospector(ai);
        assertNotSame(_baseSettings, updated);
        assertSame(ai, updated.getAnnotationIntrospector());
    }

    // Tests withInsertedAnnotationIntrospector and withAppendedAnnotationIntrospector
    @Test
    public void testWithInsertedAndAppendedAnnotationIntrospector_validInput_returnsCombined() {
        AnnotationIntrospector ai1 = new JacksonAnnotationIntrospector();
        BaseSettings s1 = _baseSettings.withAnnotationIntrospector(ai1);

        AnnotationIntrospector ai2 = new JacksonAnnotationIntrospector();
        BaseSettings s2 = s1.withInsertedAnnotationIntrospector(ai2);
        assertNotNull(s2.getAnnotationIntrospector());

        BaseSettings s3 = s1.withAppendedAnnotationIntrospector(ai2);
        assertNotNull(s3.getAnnotationIntrospector());
    }

    // Tests withVisibilityChecker and withVisibility
    @Test
    public void testWithVisibilityCheckerAndVisibility_validInput_updatesVisibility() {
        VisibilityChecker<?> vc = _baseSettings.getVisibilityChecker();
        assertSame(_baseSettings, _baseSettings.withVisibilityChecker(vc));

        BaseSettings updated = _baseSettings.withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.PUBLIC_ONLY);
        assertNotSame(_baseSettings, updated);
        assertNotNull(updated.getVisibilityChecker());
    }

    // Tests withPropertyNamingStrategy
    @Test
    public void testWithPropertyNamingStrategy_sameAndDifferent_returnsExpected() {
        assertSame(_baseSettings, _baseSettings.withPropertyNamingStrategy(null));
    }

    // Tests withTypeFactory
    @Test
    public void testWithTypeFactory_sameAndDifferent_returnsExpected() {
        assertSame(_baseSettings, _baseSettings.withTypeFactory(TypeFactory.defaultInstance()));

        TypeFactory customTf = TypeFactory.defaultInstance().withClassLoader(getClass().getClassLoader());
        BaseSettings updated = _baseSettings.withTypeFactory(customTf);
        assertNotSame(_baseSettings, updated);
        assertSame(customTf, updated.getTypeFactory());
    }

    // Tests withTypeResolverBuilder
    @Test
    public void testWithTypeResolverBuilder_sameAndDifferent_returnsExpected() {
        assertSame(_baseSettings, _baseSettings.withTypeResolverBuilder(null));
    }

    // Tests withDateFormat same instance
    @Test
    public void testWithDateFormat_sameInstance_returnsSame() {
        assertSame(_baseSettings, _baseSettings.withDateFormat(_dateFormat));
    }

    // Tests withDateFormat null and non-null (verifying timeZone sync)
    @Test
    public void testWithDateFormat_customFormat_updatesDateFormatAndTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(tz);

        BaseSettings updated = _baseSettings.withDateFormat(sdf);
        assertNotSame(_baseSettings, updated);
        assertSame(sdf, updated.getDateFormat());
        assertEquals(tz, updated.getTimeZone());

        BaseSettings nullDf = _baseSettings.withDateFormat(null);
        assertNull(nullDf.getDateFormat());
        assertEquals(_timeZone, nullDf.getTimeZone());
    }

    // Tests withHandlerInstantiator
    @Test
    public void testWithHandlerInstantiator_sameAndDifferent_returnsExpected() {
        assertSame(_baseSettings, _baseSettings.withHandlerInstantiator(null));
    }

    // Tests with(Locale)
    @Test
    public void testWithLocale_sameAndDifferent_returnsExpected() {
        assertSame(_baseSettings, _baseSettings.with(_locale));

        Locale newLocale = Locale.GERMANY;
        BaseSettings updated = _baseSettings.with(newLocale);
        assertNotSame(_baseSettings, updated);
        assertEquals(newLocale, updated.getLocale());
    }

    // Tests with(TimeZone) with null input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithTimeZone_nullInput_throwsException() {
        _baseSettings.with((TimeZone) null);
    }

    // Tests with(TimeZone) with StdDateFormat
    @Test
    public void testWithTimeZone_stdDateFormat_updatesTimeZoneAndDateFormat() {
        TimeZone tz = TimeZone.getTimeZone("EST");
        BaseSettings updated = _baseSettings.with(tz);

        assertNotSame(_baseSettings, updated);
        assertEquals(tz, updated.getTimeZone());
        assertNotNull(updated.getDateFormat());
        assertEquals(tz, updated.getDateFormat().getTimeZone());
    }

    // Tests with(TimeZone) with custom DateFormat (non-StdDateFormat clone path)
    @Test
    public void testWithTimeZone_customDateFormat_updatesTimeZoneAndDateFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        BaseSettings customSettings = _baseSettings.withDateFormat(sdf);

        TimeZone newTz = TimeZone.getTimeZone("PST");
        BaseSettings updated = customSettings.with(newTz);

        assertNotSame(customSettings, updated);
        assertEquals(newTz, updated.getTimeZone());
        assertNotNull(updated.getDateFormat());
        assertEquals(newTz, updated.getDateFormat().getTimeZone());
    }

    // Tests with(Base64Variant)
    @Test
    public void testWithBase64Variant_sameAndDifferent_returnsExpected() {
        assertSame(_baseSettings, _baseSettings.with(_base64));

        Base64Variant newVariant = Base64Variants.MODIFIED_FOR_URL;
        BaseSettings updated = _baseSettings.with(newVariant);
        assertNotSame(_baseSettings, updated);
        assertEquals(newVariant, updated.getBase64Variant());
    }
}