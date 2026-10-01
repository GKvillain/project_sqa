package zestd4j;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Turns replayed AutoFuzz (DriverGen2) inputs into a JUnit 4 regression suite.
 * Each input is re-executed through Harness on the FIXED version; the observation string
 * (return values / exceptions / state) becomes the expected value. Inputs whose observation
 * differs between two executions (non-deterministic) or that hang / throw Errors are dropped.
 * The Harness source is embedded in the test as nested class Z, so the suite is self-contained.
 *
 * Usage: HarnessTestWriter <inputs.txt> <out.java> <testPackage> <TestClass> <targets.txt> <Harness.java>
 */
public class HarnessTestWriter {
    static final long CALL_TIMEOUT_MS = 3000;

    public static void main(String[] a) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(a[0]));
        String pkg = a[2], testClass = a[3];
        String[] T = Files.readAllLines(Paths.get(a[4])).stream().filter(x -> !x.trim().isEmpty()).toArray(String[]::new);
        String harness = new String(Files.readAllBytes(Paths.get(a[5])), StandardCharsets.UTF_8);

        ExecutorService ex = Executors.newCachedThreadPool(r -> { Thread t = new Thread(r); t.setDaemon(true); return t; });
        StringBuilder tests = new StringBuilder();
        int n = 0, skipped = 0;
        for (String line : new LinkedHashSet<>(lines)) {
            String[] f = line.replace("\r", "").trim().split(" ", -1);
            if (f.length != 18) { skipped++; continue; }
            Object[] v;
            try { v = parse(f); } catch (Exception e) { skipped++; continue; }
            String o1 = exec(ex, T, v), o2 = o1 == null ? null : exec(ex, T, v);
            if (o1 == null || !o1.equals(o2)) { skipped++; continue; }
            tests.append("  @Test(timeout = 4000)\n  public void test").append(n++).append("() throws Throwable {\n")
                 .append("    assertEquals(\"").append(esc(o1)).append("\",\n        Z.run(T, ")
                 .append(v[0]).append(", ").append(v[1]).append(", ").append(v[2]).append(", ").append(v[3]).append(", ")
                 .append(str(v[4])).append(", ").append(str(v[5])).append(", ").append(str(v[6])).append(",\n        ")
                 .append(i((Integer) v[7])).append(", ").append(i((Integer) v[8])).append(", ").append(i((Integer) v[9])).append(", ")
                 .append(l((Long) v[10])).append(", ").append(l((Long) v[11])).append(", ")
                 .append(v[12]).append(", ").append(v[13]).append(", ")
                 .append("(char) ").append((int) (Character) v[14]).append(", (char) ").append((int) (Character) v[15]).append(",\n        ")
                 .append(dbl((Double) v[16])).append(", ").append(dbl((Double) v[17])).append("));\n  }\n");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n")
          .append("import org.junit.Test;\nimport static org.junit.Assert.*;\n")
          .append("public class ").append(testClass).append(" {\n")
          .append(tests)
          .append("\n  // ZEST-HARNESS (support code below: not test statements)\n")
          .append("  static final String[] T = {\n");
        for (String d : T) sb.append("    \"").append(esc(d)).append("\",\n");
        sb.append("  };\n\n").append(nested(harness)).append("}\n");
        Path out = Paths.get(a[1]);
        if (out.getParent() != null) Files.createDirectories(out.getParent());
        Files.write(out, sb.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("Wrote " + n + " tests (skipped " + skipped + ")");
        System.exit(0);
    }

    static Object[] parse(String[] f) {
        Object[] v = new Object[18];
        for (int k = 0; k < 4; k++) v[k] = Integer.parseInt(f[k]);
        for (int k = 4; k < 7; k++) {
            if (!f[k].startsWith("_")) throw new IllegalArgumentException();
            v[k] = new String(Base64.getDecoder().decode(f[k].substring(1)), StandardCharsets.UTF_8);
        }
        for (int k = 7; k < 10; k++) v[k] = Integer.parseInt(f[k]);
        v[10] = Long.parseLong(f[10]); v[11] = Long.parseLong(f[11]);
        v[12] = Boolean.parseBoolean(f[12]); v[13] = Boolean.parseBoolean(f[13]);
        v[14] = (char) Integer.parseInt(f[14]); v[15] = (char) Integer.parseInt(f[15]);
        v[16] = Double.longBitsToDouble(Long.parseLong(f[16])); v[17] = Double.longBitsToDouble(Long.parseLong(f[17]));
        for (int k = 4; k < 7; k++) if (((String) v[k]).length() > 1000) throw new IllegalArgumentException();
        return v;
    }

    /** Run once through Harness; null = hang or Error (StackOverflow, OOM, ...). */
    static String exec(ExecutorService ex, String[] T, Object[] v) {
        Future<String> fu = ex.submit(() -> {
            try {
                return Harness.run(T, (Integer) v[0], (Integer) v[1], (Integer) v[2], (Integer) v[3],
                    (String) v[4], (String) v[5], (String) v[6], (Integer) v[7], (Integer) v[8], (Integer) v[9],
                    (Long) v[10], (Long) v[11], (Boolean) v[12], (Boolean) v[13], (Character) v[14], (Character) v[15],
                    (Double) v[16], (Double) v[17]);
            } catch (Error | Exception e) { throw e; } catch (Throwable t) { throw new Exception(t); }
        });
        try { return fu.get(CALL_TIMEOUT_MS, TimeUnit.MILLISECONDS); }
        catch (Throwable t) { fu.cancel(true); return null; }
    }

    /** Harness source -> nested static class Z (package/comments at top removed, class renamed). */
    static String nested(String src) {
        StringBuilder b = new StringBuilder();
        boolean started = false;
        for (String ln : src.split("\n")) {
            if (!started) {
                if (ln.startsWith("public class Harness")) { started = true; b.append("  static class Z {\n"); }
                continue;
            }
            b.append("  ").append(ln.replaceAll("\\bHarness\\b", "Z")).append('\n');
        }
        return b.toString();
    }

    static String str(Object s) { return "\"" + esc((String) s) + "\""; }
    static String i(int x) { return x == Integer.MIN_VALUE ? "Integer.MIN_VALUE" : Integer.toString(x); }
    static String l(long x) { return x == Long.MIN_VALUE ? "Long.MIN_VALUE" : x + "L"; }
    static String dbl(double d) { return "Double.longBitsToDouble(0x" + Long.toHexString(Double.doubleToRawLongBits(d)) + "L)"; }

    static String esc(String s) {
        StringBuilder b = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '\\' || c == '"') b.append('\\').append(c);
            else if (c < 0x20 || c == 0x7f) b.append(String.format("\\%03o", (int) c));
            else if (c > 0x7f) b.append(String.format("\\u%04x", (int) c));
            else b.append(c);
        }
        return b.toString();
    }
}
