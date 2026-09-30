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
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
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
        _timeZone = TimeZone.getTimeZone("UTC");
        _base64 = Base64Variants.MIME;

        _baseSettings = new BaseSettings(
                null,
                new JacksonAnnotationIntrospector(),
                VisibilityChecker.Std.defaultInstance(),
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

    // Tests getters return expected initialized values
    @Test
    public void testGetters_initialState_returnsCorrectValues() {
        assertNull(_baseSettings.getClassIntrospector());
        assertNotNull(_baseSettings.getAnnotationIntrospector());
        assertNotNull(_baseSettings.getVisibilityChecker());
        assertNull(_baseSettings.getPropertyNamingStrategy());
        assertNotNull(_baseSettings.getTypeFactory());
        assertNull(_baseSettings.getTypeResolverBuilder());
        assertEquals(_dateFormat, _baseSettings.getDateFormat());
        assertNull(_baseSettings.getHandlerInstantiator());
        assertEquals(_locale, _baseSettings.getLocale());
        assertEquals(_timeZone, _baseSettings.getTimeZone());
        assertEquals(_base64, _baseSettings.getBase64Variant());
    }

    // Tests with(TimeZone) when timezone is null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWith_nullTimeZone_throwsIllegalArgumentException() {
        _baseSettings.with((TimeZone) null);
    }

    // Tests with(TimeZone) with same instance returns this
    @Test
    public void testWith_sameTimeZone_returnsSame() {
        BaseSettings result = _baseSettings.with(_timeZone);
        assertSame(_baseSettings, result);
    }

    // Tests with(TimeZone) with StdDateFormat updates timezone correctly (Defects4J 24b defect test)
    @Test
    public void testWith_validTimeZoneAndStdDateFormat_returnsNewInstanceWithUpdatedTimeZone() {
        TimeZone newTz = TimeZone.getTimeZone("GMT+2");
        BaseSettings result = _baseSettings.with(newTz);

        assertNotSame(_baseSettings, result);
        assertEquals(newTz, result.getTimeZone());
        assertEquals(newTz, result.getDateFormat().getTimeZone());
    }

    // Tests with(TimeZone) with custom DateFormat clones and updates timezone
    @Test
    public void testWith_customDateFormat_updatesTimeZoneOnClonedDateFormat() {
        SimpleDateFormat customFormat = new SimpleDateFormat("yyyy/MM/dd");
        customFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        BaseSettings settings = _baseSettings.withDateFormat(customFormat);

        TimeZone newTz = TimeZone.getTimeZone("PST");
        BaseSettings result = settings.with(newTz);

        assertNotSame(settings, result);
        assertEquals(newTz, result.getTimeZone());
        assertEquals(newTz, result.getDateFormat().getTimeZone());
        assertNotSame(customFormat, result.getDateFormat());
    }

    // Tests with(TimeZone) when DateFormat is null
    @Test
    public void testWith_timeZoneWhenDateFormatIsNull_updatesTimeZone() {
        BaseSettings noDf = _baseSettings.withDateFormat(null);
        TimeZone newTz = TimeZone.getTimeZone("PST");
        BaseSettings result = noDf.with(newTz);

        assertNotSame(noDf, result);
        assertNull(result.getDateFormat());
        assertEquals(newTz, result.getTimeZone());
    }

    // Tests withDateFormat with null DateFormat keeps existing timezone
    @Test
    public void testWithDateFormat_nullDateFormat_preservesExistingTimeZone() {
        BaseSettings result = _baseSettings.withDateFormat(null);

        assertNotSame(_baseSettings, result);
        assertNull(result.getDateFormat());
        assertEquals(_timeZone, result.getTimeZone());
    }

    // Tests withDateFormat with same instance returns this
    @Test
    public void testWithDateFormat_sameInstance_returnsSame() {
        BaseSettings result = _baseSettings.withDateFormat(_dateFormat);
        assertSame(_baseSettings, result);
    }

    // Tests withDateFormat with different DateFormat extracts DateFormat's timezone
    @Test
    public void testWithDateFormat_differentDateFormat_updatesDateFormatAndTimeZone() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        TimeZone tz = TimeZone.getTimeZone("EST");
        df.setTimeZone(tz);

        BaseSettings result = _baseSettings.withDateFormat(df);

        assertNotSame(_baseSettings, result);
        assertSame(df, result.getDateFormat());
        assertEquals(tz, result.getTimeZone());
    }

    // Tests with(Locale) with same instance returns this
    @Test
    public void testWith_sameLocale_returnsSame() {
        BaseSettings result = _baseSettings.with(_locale);
        assertSame(_baseSettings, result);
    }

    // Tests with(Locale) with different Locale returns new instance
    @Test
    public void testWith_differentLocale_returnsNewInstance() {
        Locale newLocale = Locale.FRANCE;
        BaseSettings result = _baseSettings.with(newLocale);

        assertNotSame(_baseSettings, result);
        assertEquals(newLocale, result.getLocale());
    }

    // Tests with(Base64Variant) with same instance returns this
    @Test
    public void testWith_sameBase64Variant_returnsSame() {
        BaseSettings result = _baseSettings.with(_base64);
        assertSame(_baseSettings, result);
    }

    // Tests with(Base64Variant) with different variant returns new instance
    @Test
    public void testWith_differentBase64Variant_returnsNewInstance() {
        Base64Variant newVariant = Base64Variants.MODIFIED_FOR_URL;
        BaseSettings result = _baseSettings.with(newVariant);

        assertNotSame(_baseSettings, result);
        assertEquals(newVariant, result.getBase64Variant());
    }

    // Tests withClassIntrospector with same and new instance
    @Test
    public void testWithClassIntrospector_branchTesting() {
        assertSame(_baseSettings, _baseSettings.withClassIntrospector(null));

        ClassIntrospector ci = new BasicClassIntrospector();
        BaseSettings result = _baseSettings.withClassIntrospector(ci);
        assertNotSame(_baseSettings, result);
        assertSame(ci, result.getClassIntrospector());
    }

    // Tests withAnnotationIntrospector with same and new instance
    @Test
    public void testWithAnnotationIntrospector_branchTesting() {
        assertSame(_baseSettings, _baseSettings.withAnnotationIntrospector(_baseSettings.getAnnotationIntrospector()));

        AnnotationIntrospector ai = AnnotationIntrospector.nopInstance();
        BaseSettings result = _baseSettings.withAnnotationIntrospector(ai);
        assertNotSame(_baseSettings, result);
        assertSame(ai, result.getAnnotationIntrospector());
    }

    // Tests withInsertedAnnotationIntrospector and withAppendedAnnotationIntrospector
    @Test
    public void testWithInsertedAndAppendedAnnotationIntrospector_createsPairs() {
        AnnotationIntrospector extra = AnnotationIntrospector.nopInstance();

        BaseSettings inserted = _baseSettings.withInsertedAnnotationIntrospector(extra);
        assertNotSame(_baseSettings, inserted);
        assertNotNull(inserted.getAnnotationIntrospector());

        BaseSettings appended = _baseSettings.withAppendedAnnotationIntrospector(extra);
        assertNotSame(_baseSettings, appended);
        assertNotNull(appended.getAnnotationIntrospector());
    }

    // Tests withVisibilityChecker and withVisibility
    @Test
    public void testWithVisibilityChecker_and_withVisibility() {
        assertSame(_baseSettings, _baseSettings.withVisibilityChecker(_baseSettings.getVisibilityChecker()));

        VisibilityChecker<?> vc = VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY);
        BaseSettings result = _baseSettings.withVisibilityChecker(vc);
        assertNotSame(_baseSettings, result);
        assertSame(vc, result.getVisibilityChecker());

        BaseSettings result2 = _baseSettings.withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NONE);
        assertNotSame(_baseSettings, result2);
        assertNotNull(result2.getVisibilityChecker());
    }

    // Tests withPropertyNamingStrategy with same and new instance
    @Test
    public void testWithPropertyNamingStrategy_branchTesting() {
        assertSame(_baseSettings, _baseSettings.withPropertyNamingStrategy(null));

        PropertyNamingStrategy pns = new PropertyNamingStrategy() {};
        BaseSettings result = _baseSettings.withPropertyNamingStrategy(pns);
        assertNotSame(_baseSettings, result);
        assertSame(pns, result.getPropertyNamingStrategy());
    }

    // Tests withTypeFactory with same and new instance
    @Test
    public void testWithTypeFactory_branchTesting() {
        assertSame(_baseSettings, _baseSettings.withTypeFactory(_baseSettings.getTypeFactory()));

        TypeFactory tf = TypeFactory.defaultInstance().withModifier(null);
        BaseSettings result = _baseSettings.withTypeFactory(tf);
        assertNotSame(_baseSettings, result);
        assertSame(tf, result.getTypeFactory());
    }

    // Tests withTypeResolverBuilder with same and new instance
    @Test
    public void testWithTypeResolverBuilder_branchTesting() {
        assertSame(_baseSettings, _baseSettings.withTypeResolverBuilder(null));

        TypeResolverBuilder<?> typer = new StdTypeResolverBuilder();
        BaseSettings result = _baseSettings.withTypeResolverBuilder(typer);
        assertNotSame(_baseSettings, result);
        assertSame(typer, result.getTypeResolverBuilder());
    }

    // Tests withHandlerInstantiator with same and null instance
    @Test
    public void testWithHandlerInstantiator_branchTesting() {
        assertSame(_baseSettings, _baseSettings.withHandlerInstantiator(null));

        BaseSettings result = _baseSettings.withHandlerInstantiator(null);
        assertSame(_baseSettings, result);
    }
}