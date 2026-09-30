package org.joda.time.tz;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ZoneInfoCompiler}.
 */
public class ZoneInfoCompilerTest {

    private File tempDir;

    @Before
    public void setUp() throws Exception {
        tempDir = new File(System.getProperty("java.io.tmpdir"), "zic_test_" + System.currentTimeMillis());
        tempDir.mkdirs();
    }

    @After
    public void tearDown() throws Exception {
        deleteDir(tempDir);
    }

    private void deleteDir(File dir) {
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteDir(f);
                    } else {
                        f.delete();
                    }
                }
            }
            dir.delete();
        }
    }

    // Tests verbose flag in main thread
    @Test
    public void testVerbose_mainThread_returnsBoolean() {
        assertFalse(ZoneInfoCompiler.verbose());
    }

    // Tests verbose flag in separate thread to catch ThreadLocal uninitialized defect (Defects4J Time-11)
    @Test
    public void testVerbose_separateThread_returnsFalseWithoutNPE() throws Exception {
        final AtomicBoolean result = new AtomicBoolean(true);
        final AtomicReference<Throwable> exception = new AtomicReference<Throwable>();

        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    result.set(ZoneInfoCompiler.verbose());
                } catch (Throwable th) {
                    exception.set(th);
                }
            }
        });

        t.start();
        t.join();

        assertNull("ZoneInfoCompiler.verbose() should not throw exception in new thread", exception.get());
        assertFalse("ZoneInfoCompiler.verbose() default should be false in new thread", result.get());
    }

    // Tests parseYear with various keywords and values
    @Test
    public void testParseYear_keywordsAndValues_returnsExpectedYears() {
        assertEquals(Integer.MIN_VALUE, ZoneInfoCompiler.parseYear("min", 2000));
        assertEquals(Integer.MIN_VALUE, ZoneInfoCompiler.parseYear("minimum", 2000));
        assertEquals(Integer.MAX_VALUE, ZoneInfoCompiler.parseYear("max", 2000));
        assertEquals(Integer.MAX_VALUE, ZoneInfoCompiler.parseYear("maximum", 2000));
        assertEquals(1995, ZoneInfoCompiler.parseYear("only", 1995));
        assertEquals(2023, ZoneInfoCompiler.parseYear("2023", 2000));
    }

    // Tests parseMonth with standard month strings
    @Test
    public void testParseMonth_validMonths_returnsCorrectMonthNumber() {
        assertEquals(1, ZoneInfoCompiler.parseMonth("Jan"));
        assertEquals(1, ZoneInfoCompiler.parseMonth("January"));
        assertEquals(6, ZoneInfoCompiler.parseMonth("Jun"));
        assertEquals(12, ZoneInfoCompiler.parseMonth("Dec"));
        assertEquals(12, ZoneInfoCompiler.parseMonth("December"));
    }

    // Tests parseDayOfWeek with standard day strings
    @Test
    public void testParseDayOfWeek_validDays_returnsCorrectDayNumber() {
        assertEquals(1, ZoneInfoCompiler.parseDayOfWeek("Mon"));
        assertEquals(1, ZoneInfoCompiler.parseDayOfWeek("Monday"));
        assertEquals(5, ZoneInfoCompiler.parseDayOfWeek("Fri"));
        assertEquals(7, ZoneInfoCompiler.parseDayOfWeek("Sun"));
        assertEquals(7, ZoneInfoCompiler.parseDayOfWeek("Sunday"));
    }

    // Tests parseOptional for hyphen and normal values
    @Test
    public void testParseOptional_hyphenAndValues_returnsExpected() {
        assertNull(ZoneInfoCompiler.parseOptional("-"));
        assertEquals("EST", ZoneInfoCompiler.parseOptional("EST"));
        assertEquals("0", ZoneInfoCompiler.parseOptional("0"));
    }

    // Tests parseTime for standard, zero, and negative values
    @Test
    public void testParseTime_validFormats_returnsMillis() {
        assertEquals(0, ZoneInfoCompiler.parseTime("0"));
        assertEquals(0, ZoneInfoCompiler.parseTime("00:00:00"));
        assertEquals(3600000, ZoneInfoCompiler.parseTime("1:00"));
        assertEquals(3600000, ZoneInfoCompiler.parseTime("01:00:00"));
        assertEquals(5400000, ZoneInfoCompiler.parseTime("1:30"));
        assertEquals(-7200000, ZoneInfoCompiler.parseTime("-2:00"));
    }

    // Tests parseTime with invalid string format
    @Test(expected = IllegalArgumentException.class)
    public void testParseTime_invalidFormat_throwsException() {
        ZoneInfoCompiler.parseTime("invalid");
    }

    // Tests parseZoneChar for standard, utc, and wall time characters
    @Test
    public void testParseZoneChar_allVariants_returnsStandardChar() {
        assertEquals('s', ZoneInfoCompiler.parseZoneChar('s'));
        assertEquals('s', ZoneInfoCompiler.parseZoneChar('S'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('u'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('U'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('g'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('G'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('z'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('Z'));
        assertEquals('w', ZoneInfoCompiler.parseZoneChar('w'));
        assertEquals('w', ZoneInfoCompiler.parseZoneChar('W'));
        assertEquals('w', ZoneInfoCompiler.parseZoneChar('x')); // default to 'w'
    }

    // Tests static test method with matching and non-matching IDs
    @Test
    public void testTest_validDateTimeZone_returnsTrue() {
        assertTrue(ZoneInfoCompiler.test("UTC", DateTimeZone.UTC));
        assertTrue(ZoneInfoCompiler.test("MismatchedId", DateTimeZone.UTC));
    }

    // Tests getStartOfYear singleton instance
    @Test
    public void testGetStartOfYear_invoked_returnsNonNullInstance() {
        ZoneInfoCompiler.DateTimeOfYear doy1 = ZoneInfoCompiler.getStartOfYear();
        ZoneInfoCompiler.DateTimeOfYear doy2 = ZoneInfoCompiler.getStartOfYear();
        assertNotNull(doy1);
        assertSame(doy1, doy2);
        assertEquals(1, doy1.iMonthOfYear);
        assertEquals(1, doy1.iDayOfMonth);
        assertEquals(0, doy1.iDayOfWeek);
        assertFalse(doy1.iAdvanceDayOfWeek);
        assertEquals(0, doy1.iMillisOfDay);
        assertEquals('w', doy1.iZoneChar);
    }

    // Tests getLenientISOChronology singleton instance
    @Test
    public void testGetLenientISOChronology_invoked_returnsNonNullInstance() {
        assertNotNull(ZoneInfoCompiler.getLenientISOChronology());
        assertSame(ZoneInfoCompiler.getLenientISOChronology(), ZoneInfoCompiler.getLenientISOChronology());
    }

    // Tests writeZoneInfoMap method
    @Test
    public void testWriteZoneInfoMap_validMap_writesWithoutError() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(baos);

        Map<String, DateTimeZone> zimap = new HashMap<String, DateTimeZone>();
        zimap.put("UTC", DateTimeZone.UTC);

        ZoneInfoCompiler.writeZoneInfoMap(dout, zimap);
        dout.flush();

        byte[] bytes = baos.toByteArray();
        assertTrue(bytes.length > 0);
    }

    // Tests parsing data file with Rule, Zone, Link, and comments
    @Test
    public void testParseDataFile_rulesZonesAndLinks_populatesCorrectly() throws IOException {
        String tzData =
            "# Comment line\n" +
            "Rule\tTestRule\t2000\tmax\t-\tMar\tlastSun\t1:00\t1:00\tD\n" +
            "Rule\tTestRule\t2000\tmax\t-\tOct\tlastSun\t2:00\t0\tS\n" +
            "Zone\tTest/Zone\t0:00\tTestRule\tT%sT\t2005\tJan\t1\t0:00\n" +
            "\t\t\t1:00\tTestRule\tT%sT\n" +
            "Zone\tSimple/Zone\t-5:00\t-\tEST\n" +
            "Link\tTest/Zone\tTest/Alias\n";

        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        BufferedReader reader = new BufferedReader(new StringReader(tzData));
        compiler.parseDataFile(reader);

        Map<String, DateTimeZone> compiled = compiler.compile(null, null);
        assertNotNull(compiled.get("Test/Zone"));
        assertNotNull(compiled.get("Simple/Zone"));
        assertNotNull(compiled.get("Test/Alias"));
    }

    // Tests compile method with output directory
    @Test
    public void testCompile_withOutputDir_createsFiles() throws IOException {
        String tzData =
            "Rule\tUSRule\t2000\tonly\t-\tApr\tSun>=1\t2:00\t1:00\tD\n" +
            "Rule\tUSRule\t2000\tonly\t-\tOct\tSun<=31\t2:00\t0\tS\n" +
            "Zone\tAmerica/Custom\t-5:00\tUSRule\tE%sT\n";

        File sourceFile = new File(tempDir, "sample.tz");
        FileOutputStream fos = new FileOutputStream(sourceFile);
        fos.write(tzData.getBytes("UTF-8"));
        fos.close();

        File outputSubDir = new File(tempDir, "compiled");
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        Map<String, DateTimeZone> compiled = compiler.compile(outputSubDir, new File[]{sourceFile});

        assertTrue(outputSubDir.exists());
        assertTrue(new File(outputSubDir, "ZoneInfoMap").exists());
        assertTrue(new File(outputSubDir, "America/Custom").exists());
        assertNotNull(compiled.get("America/Custom"));
    }

    // Tests main method without arguments prints usage without failure
    @Test
    public void testMain_noArguments_printsUsage() throws Exception {
        ZoneInfoCompiler.main(new String[0]);
    }

    // Tests main method with help argument
    @Test
    public void testMain_helpArgument_printsUsage() throws Exception {
        ZoneInfoCompiler.main(new String[]{"-?"});
    }

    // Tests main method with source and destination arguments
    @Test
    public void testMain_compileSourceFile_executesSuccessfully() throws Exception {
        String tzData = "Zone\tEtc/UTC_Custom\t0:00\t-\tUTC\n";
        File srcFile = new File(tempDir, "utc_custom.tz");
        FileOutputStream fos = new FileOutputStream(srcFile);
        fos.write(tzData.getBytes("UTF-8"));
        fos.close();

        File destDir = new File(tempDir, "output");
        String[] args = new String[]{
            "-src", tempDir.getAbsolutePath(),
            "-dst", destDir.getAbsolutePath(),
            "-verbose",
            "utc_custom.tz"
        };

        ZoneInfoCompiler.main(args);
        assertTrue(destDir.exists());
        assertTrue(new File(destDir, "Etc/UTC_Custom").exists());
    }

    // Tests DateTimeOfYear string constructors, fields and toString
    @Test
    public void testDateTimeOfYear_constructorsAndToString() {
        ZoneInfoCompiler.DateTimeOfYear doyDefault = new ZoneInfoCompiler.DateTimeOfYear();
        assertNotNull(doyDefault.toString());

        StringTokenizer st1 = new StringTokenizer("Mar lastSun 1:00s");
        ZoneInfoCompiler.DateTimeOfYear doy1 = new ZoneInfoCompiler.DateTimeOfYear(st1);
        assertEquals(3, doy1.iMonthOfYear);
        assertEquals(-1, doy1.iDayOfMonth);
        assertEquals(7, doy1.iDayOfWeek);
        assertFalse(doy1.iAdvanceDayOfWeek);
        assertEquals(3600000, doy1.iMillisOfDay);
        assertEquals('s', doy1.iZoneChar);
        assertNotNull(doy1.toString());

        StringTokenizer st2 = new StringTokenizer("Apr Sun>=10 2:00u");
        ZoneInfoCompiler.DateTimeOfYear doy2 = new ZoneInfoCompiler.DateTimeOfYear(st2);
        assertEquals(4, doy2.iMonthOfYear);
        assertEquals(10, doy2.iDayOfMonth);
        assertEquals(7, doy2.iDayOfWeek);
        assertTrue(doy2.iAdvanceDayOfWeek);
        assertEquals(7200000, doy2.iMillisOfDay);
        assertEquals('u', doy2.iZoneChar);

        StringTokenizer st3 = new StringTokenizer("May Sun<=20 3:00w");
        ZoneInfoCompiler.DateTimeOfYear doy3 = new ZoneInfoCompiler.DateTimeOfYear(st3);
        assertEquals(5, doy3.iMonthOfYear);
        assertEquals(20, doy3.iDayOfMonth);
        assertEquals(7, doy3.iDayOfWeek);
        assertFalse(doy3.iAdvanceDayOfWeek);
        assertEquals(10800000, doy3.iMillisOfDay);
        assertEquals('w', doy3.iZoneChar);

        StringTokenizer st4 = new StringTokenizer("Jun 15");
        ZoneInfoCompiler.DateTimeOfYear doy4 = new ZoneInfoCompiler.DateTimeOfYear(st4);
        assertEquals(6, doy4.iMonthOfYear);
        assertEquals(15, doy4.iDayOfMonth);
        assertEquals(0, doy4.iDayOfWeek);
        assertEquals(0, doy4.iMillisOfDay);
        assertEquals('w', doy4.iZoneChar);
    }

    // Tests DateTimeOfYear addRecurring and addCutover methods
    @Test
    public void testDateTimeOfYear_addRecurringAndAddCutover() {
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
        StringTokenizer st = new StringTokenizer("Oct lastSun 2:00s");
        ZoneInfoCompiler.DateTimeOfYear doy = new ZoneInfoCompiler.DateTimeOfYear(st);

        doy.addRecurring(builder, "S", 0, 2000, 2010);
        doy.addCutover(builder, 2005);
        assertNotNull(builder.toDateTimeZone("TestZone", true));
    }

    // Tests backward file parsing flag
    @Test
    public void testParseDataFile_backwardMode_processesLinks() throws IOException {
        String tzData =
            "Link\tAmerica/New_York\tUS/Eastern\n";

        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        BufferedReader reader = new BufferedReader(new StringReader(tzData));
        compiler.parseDataFile(reader, true);

        Map<String, DateTimeZone> compiled = compiler.compile(null, null);
        assertNotNull(compiled);
    }

    // Tests main with invalid arguments (missing parameter values)
    @Test
    public void testMain_invalidOptions_handlesGracefully() throws Exception {
        ZoneInfoCompiler.main(new String[]{"-src"});
        ZoneInfoCompiler.main(new String[]{"-dst"});
        ZoneInfoCompiler.main(new String[]{"-unknownOption"});
    }
}