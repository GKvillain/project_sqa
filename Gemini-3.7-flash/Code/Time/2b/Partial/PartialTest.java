package org.joda.time;

import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;

public class PartialTest {

    // Tests default constructor creating empty partial
    @Test
    public void testConstructor_default_sizeZero() {
        Partial p = new Partial();
        assertEquals(0, p.size());
        assertEquals(0, p.getFieldTypes().length);
        assertEquals(0, p.getValues().length);
    }

    // Tests single field constructor and getters
    @Test
    public void testConstructor_singleField_initializesCorrectly() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        assertEquals(1, p.size());
        assertEquals(DateTimeFieldType.year(), p.getFieldType(0));
        assertEquals(2020, p.getValue(0));
    }

    // Tests single field constructor with null type throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullFieldType_throwsException() {
        new Partial((DateTimeFieldType) null, 1);
    }

    // Tests array constructor with mismatched array lengths throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_mismatchedArrays_throwsException() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {DateTimeFieldType.year()};
        int[] values = new int[] {2020, 10};
        new Partial(types, values);
    }

    // Tests array constructor with invalid field ordering throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidOrder_throwsException() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.year()
        };
        int[] values = new int[] {5, 2020};
        new Partial(types, values);
    }

    // Tests array constructor with duplicate fields throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_duplicateFields_throwsException() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.dayOfMonth(),
            DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] {1, 2};
        new Partial(types, values);
    }

    // Tests copying constructor from another ReadablePartial
    @Test
    public void testConstructor_copyReadablePartial_copiesCorrectly() {
        Partial original = new Partial(DateTimeFieldType.year(), 2021);
        Partial copy = new Partial(original);
        assertEquals(1, copy.size());
        assertEquals(2021, copy.getValue(0));
    }

    // Tests with() method adding a smaller field in correct order
    @Test
    public void testWith_newField_addsInOrder() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        Partial result = p.with(DateTimeFieldType.monthOfYear(), 6);
        assertEquals(2, result.size());
        assertEquals(DateTimeFieldType.year(), result.getFieldType(0));
        assertEquals(DateTimeFieldType.monthOfYear(), result.getFieldType(1));
        assertEquals(2020, result.getValue(0));
        assertEquals(6, result.getValue(1));
    }

    // Tests with() method adding era and year fields (triggers order comparison for unsupported range/unit)
    @Test
    public void testWith_eraAndYear_addsInOrder() {
        Partial p = new Partial(DateTimeFieldType.era(), 1);
        Partial result = p.with(DateTimeFieldType.year(), 2020);
        assertEquals(2, result.size());
        assertEquals(DateTimeFieldType.era(), result.getFieldType(0));
        assertEquals(DateTimeFieldType.year(), result.getFieldType(1));
        assertEquals(1, result.getValue(0));
        assertEquals(2020, result.getValue(1));
    }

    // Tests with() method updating existing field value
    @Test
    public void testWith_existingField_updatesValue() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        Partial updated = p.with(DateTimeFieldType.year(), 2025);
        assertEquals(2025, updated.getValue(0));
        assertSame(updated, updated.with(DateTimeFieldType.year(), 2025));
    }

    // Tests without() method removing an existing field
    @Test
    public void testWithout_existingField_removesField() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 5);
        Partial result = p.without(DateTimeFieldType.monthOfYear());
        assertEquals(1, result.size());
        assertEquals(DateTimeFieldType.year(), result.getFieldType(0));
    }

    // Tests without() method on non-existing field returning same instance
    @Test
    public void testWithout_nonExistingField_returnsSame() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        assertSame(p, p.without(DateTimeFieldType.monthOfYear()));
    }

    // Tests withField() modifying an existing field value
    @Test
    public void testWithField_validField_updatesValue() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        Partial result = p.withField(DateTimeFieldType.year(), 2024);
        assertEquals(2024, result.getValue(0));
        assertSame(result, result.withField(DateTimeFieldType.year(), 2024));
    }

    // Tests withFieldAdded() adding amount to duration field
    @Test
    public void testWithFieldAdded_validDuration_addsValue() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 5);
        Partial result = p.withFieldAdded(DurationFieldType.months(), 3);
        assertEquals(8, result.getValue(0));
        assertSame(result, result.withFieldAdded(DurationFieldType.months(), 0));
    }

    // Tests withFieldAddWrapped() wrapping around boundary
    @Test
    public void testWithFieldAddWrapped_exceedMax_wrapsAround() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 11);
        Partial result = p.withFieldAddWrapped(DurationFieldType.months(), 3);
        assertEquals(2, result.getValue(0));
    }

    // Tests isMatch() comparing with another ReadablePartial
    @Test
    public void testIsMatch_readablePartial_matchesCorrectly() {
        Partial p1 = new Partial(DateTimeFieldType.monthOfYear(), 5);
        Partial p2 = new Partial(DateTimeFieldType.monthOfYear(), 5)
                .with(DateTimeFieldType.dayOfMonth(), 15);
        Partial p3 = new Partial(DateTimeFieldType.monthOfYear(), 6);

        assertTrue(p1.isMatch(p2));
        assertFalse(p1.isMatch(p3));
    }

    // Tests toString(), toStringList(), and formatted toString(pattern)
    @Test
    public void testToString_variousFormats_outputsExpectedStrings() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 12);
        assertEquals("2020-12", p.toString());
        assertEquals("[year=2020, monthOfYear=12]", p.toStringList());
        assertEquals("12/2020", p.toString("MM/yyyy", Locale.ENGLISH));
    }

    // Tests Property operations including get, addToCopy, setCopy, and min/max values
    @Test
    public void testProperty_operations_modifyValuesCorrectly() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 5);
        Partial.Property prop = p.property(DateTimeFieldType.monthOfYear());

        assertEquals(5, prop.get());
        assertEquals(6, prop.addToCopy(1).getValue(0));
        assertEquals(8, prop.setCopy(8).getValue(0));
        assertEquals(1, prop.withMinimumValue().getValue(0));
        assertEquals(12, prop.withMaximumValue().getValue(0));
    }
}