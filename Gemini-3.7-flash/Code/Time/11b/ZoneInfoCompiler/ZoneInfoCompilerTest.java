package org.joda.time.tz;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import static org.junit.Assert.*;

public class ZoneInfoCompilerTest {

    // Tests verbose() called in a separate thread to detect ThreadLocal initialization defect
    @Test
    public void testVerbose_newThread_returnsFalseWithoutNPE() throws Throwable {
        final boolean[] result = new boolean[1];
        final Throwable[] error = new Throwable[1];
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    result[0] = ZoneInfoCompiler.verbose();
                } catch (Throwable th) {
                    error[0] = th;
                }
            }
        });
        t.start();
        t.join();
        if (error[0] != null) {
            throw error[0];
        }
        assertFalse(result[0]);
    }

    // Tests verbose() in current thread
    @Test
    public void testVerbose_currentThread_returnsBoolean() {
        assertNotNull(ZoneInfoCompiler.verbose());
    }

    // Tests parseYear with various keywords and values
    @Test
    public void testParseYear_keywordsAndValues_returnsCorrectInt() {
        assertEquals(Integer.MIN_VALUE, ZoneInfoCompiler.parseYear("minimum", 2000));
        assertEquals(Integer.MIN_VALUE, ZoneInfoCompiler.parseYear("min", 2000));
        assertEquals(Integer.MAX_VALUE, ZoneInfoCompiler.parseYear("maximum", 2000));
        assertEquals(Integer.MAX_VALUE, ZoneInfoCompiler.parseYear("max", 2000));
        assertEquals(1995, ZoneInfoCompiler.parseYear("only", 1995));
        assertEquals(2020, ZoneInfoCompiler.parseYear("2020", 2000));
    }

    // Tests parseMonth with English month names
    @Test
    public void testParseMonth_validMonthNames_returnsMonthOfYear() {
        assertEquals(1, ZoneInfoCompiler.parseMonth("Jan"));
        assertEquals(1, ZoneInfoCompiler.parseMonth("January"));
        assertEquals(2, ZoneInfoCompiler.parseMonth("Feb"));
        assertEquals(12, ZoneInfoCompiler.parseMonth("Dec"));
        assertEquals(12, ZoneInfoCompiler.parseMonth("December"));
    }

    // Tests parseDayOfWeek with English day names
    @Test
    public void testParseDayOfWeek_validDayNames_returnsDayOfWeek() {
        assertEquals(1, ZoneInfoCompiler.parseDayOfWeek("Mon"));
        assertEquals(1, ZoneInfoCompiler.parseDayOfWeek("Monday"));
        assertEquals(7, ZoneInfoCompiler.parseDayOfWeek("Sun"));
        assertEquals(7, ZoneInfoCompiler.parseDayOfWeek("Sunday"));
    }

    // Tests parseOptional with dash and non-dash strings
    @Test
    public void testParseOptional_dashAndString_returnsNullOrValue() {
        assertNull(ZoneInfoCompiler.parseOptional("-"));
        assertEquals("test", ZoneInfoCompiler.parseOptional("test"));
    }

    // Tests parseTime with positive, negative, and standard time formats
    @Test
    public void testParseTime_validTimeStrings_returnsMillis() {
        assertEquals(0, ZoneInfoCompiler.parseTime("0"));
        assertEquals(3600000, ZoneInfoCompiler.parseTime("1:00"));
        assertEquals(3661000, ZoneInfoCompiler.parseTime("1:01:01"));
        assertEquals(-3600000, ZoneInfoCompiler.parseTime("-1:00"));
    }

    // Tests parseTime with invalid string format
    @Test(expected = IllegalArgumentException.class)
    public void testParseTime_invalidFormat_throwsIllegalArgumentException() {
        ZoneInfoCompiler.parseTime("invalid_time");
    }

    // Tests parseZoneChar for standard, UTC, and wall clock indicators
    @Test
    public void testParseZoneChar_variousChars_returnsStandardizedChar() {
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
        assertEquals('w', ZoneInfoCompiler.parseZoneChar('x'));
    }

    // Tests getStartOfYear and getLenientISOChronology helper methods
    @Test
    public void testGetStartOfYearAndGetLenientISOChronology_notNull() {
        assertNotNull(ZoneInfoCompiler.getStartOfYear());
        assertNotNull(ZoneInfoCompiler.getLenientISOChronology());
    }

    // Tests test() static method with matching and non-matching IDs
    @Test
    public void testTest_mismatchedId_returnsTrue() {
        DateTimeZone utc = DateTimeZone.UTC;
        assertTrue(ZoneInfoCompiler.test("America/New_York", utc));
        assertTrue(ZoneInfoCompiler.test("UTC", utc));
    }

    // Tests writeZoneInfoMap method
    @Test
    public void testWriteZoneInfoMap_validMap_writesDataSuccessfully() throws Exception {
        Map<String, DateTimeZone> map = new HashMap<String, DateTimeZone>();
        map.put("UTC", DateTimeZone.UTC);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(baos);
        ZoneInfoCompiler.writeZoneInfoMap(dout, map);
        dout.flush();

        byte[] bytes = baos.toByteArray();
        assertTrue(bytes.length > 0);
    }

    // Tests main method with empty arguments
    @Test
    public void testMain_emptyArgs_noException() throws Exception {
        ZoneInfoCompiler.main(new String[0]);
    }

    // Tests main method with help argument
    @Test
    public void testMain_helpArg_noException() throws Exception {
        ZoneInfoCompiler.main(new String[]{"-?"});
    }

    // Tests main method with incomplete arguments
    @Test
    public void testMain_incompleteSrc_noException() throws Exception {
        ZoneInfoCompiler.main(new String[]{"-src"});
    }

    // Tests parsing data files with Zone, Rule, and Link definitions
    @Test
    public void testParseDataFile_validZoneAndRule_compilesSuccessfully() throws Exception {
        String data =
            "# Test zone info data\n" +
            "Rule\tUS\t1918\t1919\t-\tMar\tlastSun\t2:00\t1:00\tD\n" +
            "Rule\tUS\t1918\t1919\t-\tOct\tlastSun\t2:00\t0\tS\n" +
            "Zone\tAmerica/TestZone\t-5:00\tUS\tE%sT\t1920\n" +
            "\t\t\t-5:00\t-\tEST\n" +
            "Link\tAmerica/TestZone\tAmerica/TestLink\n";

        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        BufferedReader reader = new BufferedReader(new StringReader(data));
        compiler.parseDataFile(reader);
        reader.close();

        Map<String, DateTimeZone> compiled = compiler.compile(null, null);
        assertNotNull(compiled);
        assertTrue(compiled.containsKey("America/TestZone"));
        assertTrue(compiled.containsKey("America/TestLink"));
    }

    // Tests parsing cutover with specific day of month rule syntax
    @Test
    public void testParseDataFile_complexRuleSyntax_compilesSuccessfully() throws Exception {
        String data =
            "Rule\tEU\t1981\tmax\t-\tMar\tSun>=25\t2:00s\t1:00\tS\n" +
            "Rule\tEU\t1996\tmax\t-\tOct\tSun<=31\t2:00s\t0\t-\n" +
            "Zone\tEurope/TestZone\t1:00\tEU\tCE%sT\n";

        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        BufferedReader reader = new BufferedReader(new StringReader(data));
        compiler.parseDataFile(reader);
        reader.close();

        Map<String, DateTimeZone> compiled = compiler.compile(null, null);
        assertNotNull(compiled);
        assertTrue(compiled.containsKey("Europe/TestZone"));
    }

    // Tests parseMonth with invalid month name
    @Test(expected = IllegalArgumentException.class)
    public void testParseMonth_invalidMonth_throwsIllegalArgumentException() {
        ZoneInfoCompiler.parseMonth("InvalidMonth");
    }

    // Tests parseDayOfWeek with invalid day name
    @Test(expected = IllegalArgumentException.class)
    public void testParseDayOfWeek_invalidDay_throwsIllegalArgumentException() {
        ZoneInfoCompiler.parseDayOfWeek("InvalidDay");
    }

    // Tests parseDataFile with backwards link syntax and numeric day of month
    @Test
    public void testParseDataFile_backwardsAndNumericDay() throws Exception {
        String data =
            "Rule\tTestRule\t2000\tonly\t-\tJan\t15\t2:00u\t1:00\tD\n" +
            "Rule\tTestRule\t2000\tonly\t-\tFeb\t15\t2:00w\t0\tS\n" +
            "Zone\tTest/Zone\t0:00\tTestRule\tT%sT\t2001\tJan\t1\t0:00\n" +
            "\t\t\t0:00\t-\tGMT\n" +
            "Link\tTest/Zone\tTest/LinkOld\n";

        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        BufferedReader reader = new BufferedReader(new StringReader(data));
        compiler.parseDataFile(reader, true);
        reader.close();

        Map<String, DateTimeZone> compiled = compiler.compile(null, null);
        assertNotNull(compiled);
        assertTrue(compiled.containsKey("Test/Zone"));
        assertTrue(compiled.containsKey("Test/LinkOld"));
    }

    // Tests parseDataFile with inline comments and multiple cutovers
    @Test
    public void testParseDataFile_inlineCommentsAndUntilFields() throws Exception {
        String data =
            "# Header comment\n" +
            "\n" +
            "Zone\tAsia/TestZone\t5:30\t-\tIST\t1970\tJan\t15\t12:00:00s\n" +
            "\t\t\t6:00\t-\t+06\t1980\tFeb\tlastSun\t0:00u\n" +
            "\t\t\t5:30\t-\tIST\n";

        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        BufferedReader reader = new BufferedReader(new StringReader(data));
        compiler.parseDataFile(reader, false);
        reader.close();

        Map<String, DateTimeZone> compiled = compiler.compile(null, null);
        assertNotNull(compiled);
        assertTrue(compiled.containsKey("Asia/TestZone"));
    }

    // Tests compile method with physical output directory and source files
    @Test
    public void testCompile_withOutputDirAndSources() throws Exception {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "joda_tz_test_" + System.currentTimeMillis());
        tempDir.mkdirs();
        try {
            File sourceFile = new File(tempDir, "sample_tz.txt");
            FileOutputStream fos = new FileOutputStream(sourceFile);
            String data =
                "Rule\tSample\t2000\tmax\t-\tApr\t1\t0:00\t1:00\tD\n" +
                "Rule\tSample\t2000\tmax\t-\tOct\t1\t0:00\t0\tS\n" +
                "Zone\tSample/Zone\t2:00\tSample\tS%sT\n";
            fos.write(data.getBytes("UTF-8"));
            fos.close();

            File outDir = new File(tempDir, "output");
            outDir.mkdirs();

            ZoneInfoCompiler compiler = new ZoneInfoCompiler();
            Map<String, DateTimeZone> map = compiler.compile(outDir, new File[]{sourceFile});
            assertNotNull(map);
            assertTrue(map.containsKey("Sample/Zone"));
            assertTrue(new File(outDir, "Sample/Zone").exists() || new File(outDir, "ZoneInfoMap").exists());
        } finally {
            deleteRecursive(tempDir);
        }
    }

    // Tests main method execution with -src, -dst, and -verbose arguments
    @Test
    public void testMain_withArgsAndFiles() throws Exception {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "joda_tz_main_" + System.currentTimeMillis());
        tempDir.mkdirs();
        try {
            File srcDir = new File(tempDir, "src");
            srcDir.mkdirs();
            File dstDir = new File(tempDir, "dst");
            dstDir.mkdirs();

            File sourceFile = new File(srcDir, "zones.txt");
            FileOutputStream fos = new FileOutputStream(sourceFile);
            String data =
                "Zone\tMain/Test\t0:00\t-\tGMT\n";
            fos.write(data.getBytes("UTF-8"));
            fos.close();

            String[] args = new String[]{
                "-src", srcDir.getAbsolutePath(),
                "-dst", dstDir.getAbsolutePath(),
                "-verbose",
                "zones.txt"
            };
            ZoneInfoCompiler.main(args);
            assertTrue(new File(dstDir, "Main/Test").exists() || new File(dstDir, "ZoneInfoMap").exists());
        } finally {
            deleteRecursive(tempDir);
        }
    }

    // Tests main method with incomplete -dst argument
    @Test
    public void testMain_incompleteDst_noException() throws Exception {
        ZoneInfoCompiler.main(new String[]{"-dst"});
    }

    // Tests writeZoneInfoMap with multiple entries and aliases
    @Test
    public void testWriteZoneInfoMap_multipleEntriesAndLinks() throws Exception {
        Map<String, DateTimeZone> map = new HashMap<String, DateTimeZone>();
        DateTimeZone utc = DateTimeZone.UTC;
        map.put("UTC", utc);
        map.put("Etc/UTC", utc);
        map.put("Universal", utc);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(baos);
        ZoneInfoCompiler.writeZoneInfoMap(dout, map);
        dout.flush();

        byte[] bytes = baos.toByteArray();
        assertTrue(bytes.length > 0);
    }

    private static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursive(child);
                }
            }
        }
        file.delete();
    }
}