package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DoubleMetaphoneTest {

    private DoubleMetaphone doubleMetaphone;

    @Before
    public void setUp() {
        this.doubleMetaphone = new DoubleMetaphone();
    }

    // Tests null, empty, and whitespace inputs
    @Test
    public void testDoubleMetaphone_nullAndEmptyInput_returnsNull() {
        assertNull(this.doubleMetaphone.doubleMetaphone(null));
        assertNull(this.doubleMetaphone.doubleMetaphone(""));
        assertNull(this.doubleMetaphone.doubleMetaphone("   "));
        assertNull(this.doubleMetaphone.doubleMetaphone(null, true));
        assertNull(this.doubleMetaphone.doubleMetaphone("", true));
    }

    // Tests encoding of basic English words
    @Test
    public void testDoubleMetaphone_basicWords_returnsCorrectEncodings() {
        assertEquals("SM0", this.doubleMetaphone.doubleMetaphone("Smith"));
        assertEquals("SNTR", this.doubleMetaphone.doubleMetaphone("Schneider"));
        assertEquals("ALKS", this.doubleMetaphone.doubleMetaphone("Alexander"));
    }

    // Tests alternate encoding paths for names with dual pronunciations
    @Test
    public void testDoubleMetaphone_alternateEncoding_returnsCorrectAlternate() {
        assertEquals("JSN", this.doubleMetaphone.doubleMetaphone("Jason", false));
        assertEquals("ASN", this.doubleMetaphone.doubleMetaphone("Jason", true));
        assertEquals("J", this.doubleMetaphone.doubleMetaphone("Jose", false));
        assertEquals("H", this.doubleMetaphone.doubleMetaphone("Jose", true));
    }

    // Tests silent start prefixes such as GN, KN, PN, WR, PS
    @Test
    public void testDoubleMetaphone_silentStartPrefixes_skipsSilentLetter() {
        assertEquals("NT", this.doubleMetaphone.doubleMetaphone("Knight"));
        assertEquals("NT", this.doubleMetaphone.doubleMetaphone("Gnat"));
        assertEquals("NM", this.doubleMetaphone.doubleMetaphone("Pneumonia"));
        assertEquals("RT", this.doubleMetaphone.doubleMetaphone("Write"));
        assertEquals("SM", this.doubleMetaphone.doubleMetaphone("Psalm"));
    }

    // Tests special non-ASCII characters like C-cedilla and N-tilde
    @Test
    public void testDoubleMetaphone_specialCharacters_encodesProperly() {
        assertEquals("SF", this.doubleMetaphone.doubleMetaphone("\u00C7afe"));
        assertEquals("NNO", this.doubleMetaphone.doubleMetaphone("Ni\u00D1o"));
    }

    // Tests various 'C', 'CC', 'CH', and 'CZ' branches
    @Test
    public void testDoubleMetaphone_cVariations_encodesProperly() {
        assertEquals("SSR", this.doubleMetaphone.doubleMetaphone("Caesar"));
        assertEquals("FKX", this.doubleMetaphone.doubleMetaphone("Focaccia"));
        assertEquals("KST", this.doubleMetaphone.doubleMetaphone("Accident"));
        assertEquals("BXI", this.doubleMetaphone.doubleMetaphone("Bacci"));
        assertEquals("KMR", this.doubleMetaphone.doubleMetaphone("Chemistry"));
        assertEquals("XRN", this.doubleMetaphone.doubleMetaphone("Czerny", false));
        assertEquals("SRN", this.doubleMetaphone.doubleMetaphone("Czerny", true));
        assertEquals("MKFR", this.doubleMetaphone.doubleMetaphone("McCaffrey"));
    }

    // Tests various 'G' and 'GH' branches
    @Test
    public void testDoubleMetaphone_gAndGhVariations_encodesProperly() {
        assertEquals("K", this.doubleMetaphone.doubleMetaphone("Gough"));
        assertEquals("LF", this.doubleMetaphone.doubleMetaphone("Laugh"));
        assertEquals("JLN", this.doubleMetaphone.doubleMetaphone("Ghislane"));
        assertEquals("N", this.doubleMetaphone.doubleMetaphone("Agnes", true));
        assertEquals("KL", this.doubleMetaphone.doubleMetaphone("Tagliaro", false));
        assertEquals("L", this.doubleMetaphone.doubleMetaphone("Tagliaro", true));
        assertEquals("K", this.doubleMetaphone.doubleMetaphone("Hugh"));
    }

    // Tests various 'J' cases including Spanish origins
    @Test
    public void testDoubleMetaphone_jVariations_encodesProperly() {
        assertEquals("SNHS", this.doubleMetaphone.doubleMetaphone("San Jacinto"));
        assertEquals("Y", this.doubleMetaphone.doubleMetaphone("Jankelowicz", true));
        assertEquals("AJ", this.doubleMetaphone.doubleMetaphone("Bajador", false));
        assertEquals("AH", this.doubleMetaphone.doubleMetaphone("Bajador", true));
    }

    // Tests 'S', 'SC', 'SH' and French endings
    @Test
    public void testDoubleMetaphone_sAndScVariations_encodesProperly() {
        assertEquals("XKR", this.doubleMetaphone.doubleMetaphone("Sugar"));
        assertEquals("XMR", this.doubleMetaphone.doubleMetaphone("Schermerhorn", false));
        assertEquals("SKMR", this.doubleMetaphone.doubleMetaphone("Schermerhorn", true));
        assertEquals("SKL", this.doubleMetaphone.doubleMetaphone("School"));
        assertEquals("AYL", this.doubleMetaphone.doubleMetaphone("Island"));
        assertEquals("RNS", this.doubleMetaphone.doubleMetaphone("Resnais", true));
    }

    // Tests 'T', 'TH', and 'TION' cases
    @Test
    public void testDoubleMetaphone_tVariations_encodesProperly() {
        assertEquals("NXN", this.doubleMetaphone.doubleMetaphone("Nation"));
        assertEquals("TX", this.doubleMetaphone.doubleMetaphone("Tia"));
        assertEquals("TM", this.doubleMetaphone.doubleMetaphone("Thomas"));
        assertEquals("0R", this.doubleMetaphone.doubleMetaphone("Thread", false));
        assertEquals("TR", this.doubleMetaphone.doubleMetaphone("Thread", true));
    }

    // Tests 'W', 'WR', 'X', and 'Z' branches
    @Test
    public void testDoubleMetaphone_wAndXAndZVariations_encodesProperly() {
        assertEquals("ASRM", this.doubleMetaphone.doubleMetaphone("Wasserman", false));
        assertEquals("FSRM", this.doubleMetaphone.doubleMetaphone("Wasserman", true));
        assertEquals("TS", this.doubleMetaphone.doubleMetaphone("Filipowicz", false));
        assertEquals("FX", this.doubleMetaphone.doubleMetaphone("Filipowicz", true));
        assertEquals("S", this.doubleMetaphone.doubleMetaphone("Xavier"));
        assertEquals("BR", this.doubleMetaphone.doubleMetaphone("Breaux"));
        assertEquals("J", this.doubleMetaphone.doubleMetaphone("Zhang"));
    }

    // Tests Slavo-Germanic origin detection and logic
    @Test
    public void testDoubleMetaphone_slavoGermanicNames_encodesCorrectly() {
        assertEquals("WTS", this.doubleMetaphone.doubleMetaphone("Witz"));
        assertEquals("KLN", this.doubleMetaphone.doubleMetaphone("Klaus"));
    }

    // Tests max code length getter, setter, and truncation
    @Test
    public void testMaxCodeLen_customLength_truncatesResult() {
        assertEquals(4, this.doubleMetaphone.getMaxCodeLen());
        this.doubleMetaphone.setMaxCodeLen(6);
        assertEquals(6, this.doubleMetaphone.getMaxCodeLen());
        assertEquals("ALKSNT", this.doubleMetaphone.doubleMetaphone("Alexander"));
        this.doubleMetaphone.setMaxCodeLen(2);
        assertEquals("AL", this.doubleMetaphone.doubleMetaphone("Alexander"));
    }

    // Tests isDoubleMetaphoneEqual method for equality checks
    @Test
    public void testIsDoubleMetaphoneEqual_variousInputs_returnsExpected() {
        assertTrue(this.doubleMetaphone.isDoubleMetaphoneEqual("Smith", "Schmidt"));
        assertTrue(this.doubleMetaphone.isDoubleMetaphoneEqual("Wasserman", "Vasserman", true));
        assertFalse(this.doubleMetaphone.isDoubleMetaphoneEqual("Smith", "Jones"));
    }

    // Tests isDoubleMetaphoneEqual with null parameters to catch null pointer defects
    @Test
    public void testIsDoubleMetaphoneEqual_nullInputs_returnsExpectedOrThrows() {
        assertFalse(this.doubleMetaphone.isDoubleMetaphoneEqual("Smith", null));
        assertFalse(this.doubleMetaphone.isDoubleMetaphoneEqual(null, "Smith"));
        assertTrue(this.doubleMetaphone.isDoubleMetaphoneEqual(null, null));
    }

    // Tests encode(Object) with valid String object
    @Test
    public void testEncode_stringObject_returnsEncodedString() throws EncoderException {
        Object result = this.doubleMetaphone.encode((Object) "Smith");
        assertEquals("SM0", result);
        assertEquals("SM0", this.doubleMetaphone.encode("Smith"));
    }

    // Tests encode(Object) exception path for non-String input
    @Test(expected = EncoderException.class)
    public void testEncode_nonStringObject_throwsEncoderException() throws EncoderException {
        this.doubleMetaphone.encode(Integer.valueOf(123));
    }

    // Tests DoubleMetaphoneResult inner class directly
    @Test
    public void testDoubleMetaphoneResult_appendOperations_handlesMaxLengthProperly() {
        DoubleMetaphone.DoubleMetaphoneResult result = this.doubleMetaphone.new DoubleMetaphoneResult(4);
        assertFalse(result.isComplete());
        result.append("TESTING");
        assertEquals("TEST", result.getPrimary());
        assertEquals("TEST", result.getAlternate());
        assertTrue(result.isComplete());
    }

    // Additional tests for DoubleMetaphoneResult append methods
    @Test
    public void testDoubleMetaphoneResult_individualAppendMethods() {
        DoubleMetaphone.DoubleMetaphoneResult result = this.doubleMetaphone.new DoubleMetaphoneResult(5);
        result.append('A');
        result.append('B', 'C');
        result.append("D", "E");
        result.appendPrimary('F');
        result.appendAlternate('G');
        result.appendPrimary("H");
        result.appendAlternate("I");
        assertEquals("ABDFH", result.getPrimary());
        assertEquals("ACEGI", result.getAlternate());
        assertTrue(result.isComplete());
    }

    // Tests vowel initialization and silent B after M at end
    @Test
    public void testDoubleMetaphone_vowelsAndSilentB() {
        assertEquals("APL", this.doubleMetaphone.doubleMetaphone("Apple"));
        assertEquals("ARNJ", this.doubleMetaphone.doubleMetaphone("Orange"));
        assertEquals("0M", this.doubleMetaphone.doubleMetaphone("Thumb"));
        assertEquals("TM", this.doubleMetaphone.doubleMetaphone("Dumb"));
        assertEquals("KM", this.doubleMetaphone.doubleMetaphone("Comb"));
        assertEquals("BB", this.doubleMetaphone.doubleMetaphone("Bob"));
        assertEquals("KBJ", this.doubleMetaphone.doubleMetaphone("Cabbage"));
    }

    // Tests additional C branches: ACH, ARCH, CHIA, CK, CQ, CI/CE/CY
    @Test
    public void testDoubleMetaphone_additionalCBranches() {
        assertEquals("PK", this.doubleMetaphone.doubleMetaphone("Bach", false));
        assertEquals("PX", this.doubleMetaphone.doubleMetaphone("Bach", true));
        assertEquals("PKR", this.doubleMetaphone.doubleMetaphone("Bacher", false));
        assertEquals("PXR", this.doubleMetaphone.doubleMetaphone("Bacher", true));
        assertEquals("XTR", this.doubleMetaphone.doubleMetaphone("Chianti"));
        assertEquals("K", this.doubleMetaphone.doubleMetaphone("Chia"));
        assertEquals("ARKT", this.doubleMetaphone.doubleMetaphone("Architect"));
        assertEquals("ARX", this.doubleMetaphone.doubleMetaphone("Arch", false));
        assertEquals("ARK", this.doubleMetaphone.doubleMetaphone("Arch", true));
        assertEquals("SKST", this.doubleMetaphone.doubleMetaphone("Succeed"));
        assertEquals("PLK", this.doubleMetaphone.doubleMetaphone("Plaque"));
        assertEquals("PLK", this.doubleMetaphone.doubleMetaphone("Placque"));
        assertEquals("SRSL", this.doubleMetaphone.doubleMetaphone("Circle"));
        assertEquals("SBR", this.doubleMetaphone.doubleMetaphone("Cyber"));
        assertEquals("PS", this.doubleMetaphone.doubleMetaphone("Peace"));
        assertEquals("MK", this.doubleMetaphone.doubleMetaphone("Mac"));
        assertEquals("MKH", this.doubleMetaphone.doubleMetaphone("McHugh"));
    }

    // Tests D branches: DG, DT, DD
    @Test
    public void testDoubleMetaphone_dBranches() {
        assertEquals("AJ", this.doubleMetaphone.doubleMetaphone("Edge", false));
        assertEquals("ATK", this.doubleMetaphone.doubleMetaphone("Edgar", false));
        assertEquals("ATK", this.doubleMetaphone.doubleMetaphone("Edgar", true));
        assertEquals("AT", this.doubleMetaphone.doubleMetaphone("Width"));
        assertEquals("AT", this.doubleMetaphone.doubleMetaphone("Add"));
    }

    // Tests G branches: GH, GN, GLI, GE/GI/GY, GG
    @Test
    public void testDoubleMetaphone_additionalGBranches() {
        assertEquals("KST", this.doubleMetaphone.doubleMetaphone("Ghastly"));
        assertEquals("KST", this.doubleMetaphone.doubleMetaphone("Ghost"));
        assertEquals("HKK", this.doubleMetaphone.doubleMetaphone("Hiccough"));
        assertEquals("NT", this.doubleMetaphone.doubleMetaphone("Night"));
        assertEquals("0R", this.doubleMetaphone.doubleMetaphone("Through"));
        assertEquals("SN", this.doubleMetaphone.doubleMetaphone("Sign"));
        assertEquals("N", this.doubleMetaphone.doubleMetaphone("Gnu"));
        assertEquals("JRJ", this.doubleMetaphone.doubleMetaphone("George", false));
        assertEquals("KRJ", this.doubleMetaphone.doubleMetaphone("George", true));
        assertEquals("KRT", this.doubleMetaphone.doubleMetaphone("Gerhard"));
        assertEquals("JJ", this.doubleMetaphone.doubleMetaphone("Gigi", false));
        assertEquals("KJ", this.doubleMetaphone.doubleMetaphone("Gigi", true));
        assertEquals("PJ", this.doubleMetaphone.doubleMetaphone("Biaggi", false));
        assertEquals("PK", this.doubleMetaphone.doubleMetaphone("Biaggi", true));
    }

    // Tests H branches (vowel before and after vs silent)
    @Test
    public void testDoubleMetaphone_hBranches() {
        assertEquals("AHT", this.doubleMetaphone.doubleMetaphone("Ahead"));
        assertEquals("A", this.doubleMetaphone.doubleMetaphone("Ah"));
        assertEquals("HST", this.doubleMetaphone.doubleMetaphone("Host"));
    }

    // Tests J additional branches
    @Test
    public void testDoubleMetaphone_additionalJBranches() {
        assertEquals("HJ", this.doubleMetaphone.doubleMetaphone("Haj", false));
        assertEquals("H", this.doubleMetaphone.doubleMetaphone("Haj", true));
        assertEquals("MRJ", this.doubleMetaphone.doubleMetaphone("Maharajah", false));
        assertEquals("MRH", this.doubleMetaphone.doubleMetaphone("Maharajah", true));
        assertEquals("HLPN", this.doubleMetaphone.doubleMetaphone("Jalapeno", false));
        assertEquals("ALPN", this.doubleMetaphone.doubleMetaphone("Jalapeno", true));
        assertEquals("SNHS", this.doubleMetaphone.doubleMetaphone("San Jose", false));
    }

    // Tests L branches including LL (Spanish vs Germanic/English)
    @Test
    public void testDoubleMetaphone_lBranches() {
        assertEquals("LT", this.doubleMetaphone.doubleMetaphone("Lloyd"));
        assertEquals("ML", this.doubleMetaphone.doubleMetaphone("Mall"));
        assertEquals("KPRL", this.doubleMetaphone.doubleMetaphone("Cabrillo", false));
        assertEquals("KPR", this.doubleMetaphone.doubleMetaphone("Cabrillo", true));
        assertEquals("FL", this.doubleMetaphone.doubleMetaphone("Villa", false));
        assertEquals("F", this.doubleMetaphone.doubleMetaphone("Villa", true));
        assertEquals("ALNT", this.doubleMetaphone.doubleMetaphone("Allende", false));
        assertEquals("AYNT", this.doubleMetaphone.doubleMetaphone("Allende", true));
    }

    // Tests M, N, P, Q, R branches
    @Test
    public void testDoubleMetaphone_mAndNAndPAndQAndRBranches() {
        assertEquals("KMPL", this.doubleMetaphone.doubleMetaphone("Campbell"));
        assertEquals("TM", this.doubleMetaphone.doubleMetaphone("Damn"));
        assertEquals("MN", this.doubleMetaphone.doubleMetaphone("Manna"));
        assertEquals("FN", this.doubleMetaphone.doubleMetaphone("Phone"));
        assertEquals("PP", this.doubleMetaphone.doubleMetaphone("Puppy"));
        assertEquals("KPRT", this.doubleMetaphone.doubleMetaphone("Cupboard"));
        assertEquals("KK", this.doubleMetaphone.doubleMetaphone("Quick"));
        assertEquals("KX", this.doubleMetaphone.doubleMetaphone("Quiche"));
        assertEquals("R", this.doubleMetaphone.doubleMetaphone("Ray"));
        assertEquals("KR", this.doubleMetaphone.doubleMetaphone("Carrier"));
        assertEquals("RS", this.doubleMetaphone.doubleMetaphone("Rozier", false));
        assertEquals("RZR", this.doubleMetaphone.doubleMetaphone("Rozier", true));
    }

    // Tests S, SC, SCH and SZ branches
    @Test
    public void testDoubleMetaphone_additionalSBranches() {
        assertEquals("XN", this.doubleMetaphone.doubleMetaphone("Shine"));
        assertEquals("XLMP", this.doubleMetaphone.doubleMetaphone("Schlumberger", false));
        assertEquals("SKLM", this.doubleMetaphone.doubleMetaphone("Schlumberger", true));
        assertEquals("AXNP", this.doubleMetaphone.doubleMetaphone("Eschenbach", false));
        assertEquals("ASKN", this.doubleMetaphone.doubleMetaphone("Eschenbach", true));
        assertEquals("PLX", this.doubleMetaphone.doubleMetaphone("Polish"));
        assertEquals("AYL", this.doubleMetaphone.doubleMetaphone("Isla"));
        assertEquals("SN", this.doubleMetaphone.doubleMetaphone("Szaniszlo", false));
        assertEquals("XN", this.doubleMetaphone.doubleMetaphone("Szaniszlo", true));
    }

    // Tests T, V, W, X, Z branches and edge lengths
    @Test
    public void testDoubleMetaphone_additionalTAndVAndWAndXAndZBranches() {
        assertEquals("MX", this.doubleMetaphone.doubleMetaphone("Match"));
        assertEquals("TM", this.doubleMetaphone.doubleMetaphone("Thames"));
        assertEquals("FN", this.doubleMetaphone.doubleMetaphone("Van"));
        assertEquals("FKTR", this.doubleMetaphone.doubleMetaphone("Victor"));
        assertEquals("ANTR", this.doubleMetaphone.doubleMetaphone("Wander", false));
        assertEquals("FNTR", this.doubleMetaphone.doubleMetaphone("Wander", true));
        assertEquals("PRNN", this.doubleMetaphone.doubleMetaphone("Browning"));
        assertEquals("AKS", this.doubleMetaphone.doubleMetaphone("Aux"));
        assertEquals("SK", this.doubleMetaphone.doubleMetaphone("Zack"));
        assertEquals("PTS", this.doubleMetaphone.doubleMetaphone("Pizza", false));
        assertEquals("TS", this.doubleMetaphone.doubleMetaphone("Pizza", true));
        assertEquals("MSRT", this.doubleMetaphone.doubleMetaphone("Mozart", false));
        assertEquals("MTSR", this.doubleMetaphone.doubleMetaphone("Mozart", true));
    }

    // Tests single characters and boundary inputs
    @Test
    public void testDoubleMetaphone_singleCharactersAndSanitization() {
        assertEquals("A", this.doubleMetaphone.doubleMetaphone("A"));
        assertEquals("S", this.doubleMetaphone.doubleMetaphone("Z"));
        assertEquals("K", this.doubleMetaphone.doubleMetaphone("C"));
        assertEquals("", this.doubleMetaphone.doubleMetaphone("H"));
        assertEquals("STNT", this.doubleMetaphone.doubleMetaphone("  St. Andrews  "));
        assertEquals("MKTN", this.doubleMetaphone.doubleMetaphone("McDonald's"));
    }
}