package zestd4j;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Call InputLog.record(...) first thing in every @Fuzz method.
 * Only writes when ZEST_DUMP is set (i.e. during replay), one input per line:
 *   "<base64>"            for single-method drivers
 *   "<idx> <base64>"      for multi-method drivers (idx = which target method)
 */
public class InputLog {
    public static void record(String s) { write(null, s); }
    public static void record(int idx, String s) { write(idx, s); }

    /** Harness-driver inputs: one line of 18 space-separated fields (strings as "_"+base64). */
    public static void recordCall(int n, int w0, int w1, int w2, String s0, String s1, String s2,
                                  int i0, int i1, int i2, long l0, long l1, boolean b0, boolean b1,
                                  char c0, char c1, double d0, double d1) {
        String f = System.getenv("ZEST_DUMP");
        if (f == null) return;
        StringBuilder b = new StringBuilder();
        b.append(n).append(' ').append(w0).append(' ').append(w1).append(' ').append(w2);
        for (String s : new String[]{s0, s1, s2})
            b.append(" _").append(Base64.getEncoder().encodeToString((s == null ? "" : s).getBytes(StandardCharsets.UTF_8)));
        b.append(' ').append(i0).append(' ').append(i1).append(' ').append(i2)
         .append(' ').append(l0).append(' ').append(l1)
         .append(' ').append(b0).append(' ').append(b1)
         .append(' ').append((int) c0).append(' ').append((int) c1)
         .append(' ').append(Double.doubleToRawLongBits(d0)).append(' ').append(Double.doubleToRawLongBits(d1));
        try (FileWriter w = new FileWriter(f, true)) { w.write(b.append('\n').toString()); } catch (Exception ignored) {}
    }

    private static void write(Integer idx, String s) {
        String f = System.getenv("ZEST_DUMP");
        if (f == null || s == null) return;
        try (FileWriter w = new FileWriter(f, true)) {
            String b = Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
            w.write((idx == null ? "" : idx + " ") + b + "\n");
        } catch (Exception ignored) {}
    }
}
