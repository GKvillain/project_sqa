package zestd4j;

import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Turns replayed Zest inputs into a JUnit 4 regression test suite.
 * Target = one or more STATIC methods taking a single String/CharSequence.
 * The oracle (expected value / expected exception) is recorded on the FIXED version.
 *
 * Usage: GenericTestWriter <inputs.txt> <out.java> <Cls#m[,Cls#m...]> <testPackage> <TestClassName>
 * Input lines: "<base64>" (method 0) or "<idx> <base64>".
 */
public class GenericTestWriter {
    static final int MAX_LEN = 2000;          // skip huge inputs (Java literal limits / readability)
    static final long CALL_TIMEOUT_MS = 2000; // skip inputs that hang

    public static void main(String[] a) throws Exception {
        if (a.length < 5) {
            System.err.println("Usage: GenericTestWriter <inputs> <out.java> <Cls#m[,Cls#m]> <testPkg> <TestClass>");
            System.exit(1);
        }
        List<String> lines = Files.readAllLines(Paths.get(a[0]));
        String pkg = a[3], testClass = a[4];
        List<Method> methods = new ArrayList<>();
        for (String spec : a[2].split(",")) {
            String[] p = spec.trim().split("#");
            methods.add(find(Class.forName(p[0]), p[1]));
        }

        ExecutorService ex = Executors.newCachedThreadPool(r -> { Thread t = new Thread(r); t.setDaemon(true); return t; });
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n")
          .append("import org.junit.Test;\nimport static org.junit.Assert.*;\n")
          .append("public class ").append(testClass).append(" {\n");
        int i = 0, skipped = 0;
        for (String line : new LinkedHashSet<>(lines)) {
            line = line.replace("\r", "");
            int idx = 0; String b64 = line.trim();
            int sp = line.indexOf(' ');
            if (sp > 0) { try { idx = Integer.parseInt(line.substring(0, sp).trim()); } catch (NumberFormatException e) { skipped++; continue; } b64 = line.substring(sp + 1).trim(); }
            if (idx < 0 || idx >= methods.size()) { skipped++; continue; }
            String s;
            try { s = new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8); } catch (IllegalArgumentException e) { skipped++; continue; }
            if (s.length() > MAX_LEN) { skipped++; continue; }
            Method m = methods.get(idx);
            String call = m.getDeclaringClass().getName().replace('$', '.') + "." + m.getName() + "(\"" + esc(s) + "\")";

            Object r; Throwable thrown = null;
            Future<Object> fu = ex.submit(() -> m.invoke(null, s));
            try {
                r = fu.get(CALL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (TimeoutException te) {
                fu.cancel(true); skipped++; continue;
            } catch (ExecutionException ee) {
                r = null;
                Throwable c = ee.getCause();
                thrown = (c instanceof InvocationTargetException) ? c.getCause() : c;
            }
            if (thrown instanceof Error && !(thrown instanceof AssertionError)) { skipped++; continue; } // StackOverflow/OOM: not a stable oracle

            StringBuilder t = new StringBuilder();
            t.append("  @Test(timeout = 4000)\n  public void test").append(i).append("() throws Throwable {\n");
            if (thrown != null) {
                String tn = Modifier.isPublic(thrown.getClass().getModifiers()) ? thrown.getClass().getName().replace('$', '.') : "Throwable";
                t.append("    try { ").append(call).append("; fail(\"expected ")
                 .append(thrown.getClass().getSimpleName()).append("\"); }\n")
                 .append("    catch (").append(tn).append(" e) { }\n");
            } else if (m.getReturnType() == void.class) {
                t.append("    ").append(call).append(";\n");
            } else if (r == null) {
                t.append("    assertNull(").append(call).append(");\n");
            } else {
                t.append("    Object r = ").append(call).append(";\n")
                 .append("    assertNotNull(r);\n")
                 .append("    assertEquals(\"").append(esc(r.getClass().getName())).append("\", r.getClass().getName());\n");
                if (stableToString(r)) {
                    String shown = show(r);
                    if (shown.length() <= MAX_LEN) {
                        t.append("    assertEquals(\"").append(esc(shown)).append("\", ")
                         .append(r.getClass().isArray() ? "java.util.Arrays.deepToString(new Object[]{r})" : "String.valueOf(r)")
                         .append(");\n");
                    }
                }
            }
            t.append("  }\n");
            sb.append(t); i++;
        }
        sb.append("}\n");
        Path out = Paths.get(a[1]);
        if (out.getParent() != null) Files.createDirectories(out.getParent());
        Files.write(out, sb.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("Wrote " + i + " tests (skipped " + skipped + ")");
        System.exit(0); // don't wait for hung daemon threads
    }

    static Method find(Class<?> c, String name) throws NoSuchMethodException {
        for (Class<?> p : new Class<?>[]{String.class, CharSequence.class, Object.class}) {
            try { Method m = c.getMethod(name, p); if (Modifier.isStatic(m.getModifiers())) { m.setAccessible(true); return m; } }
            catch (NoSuchMethodException ignored) {}
        }
        throw new NoSuchMethodException(c.getName() + "#" + name + "(String) (must be public static)");
    }

    /** Only assert toString() when it is deterministic (no identity hash codes). */
    static boolean stableToString(Object r) {
        if (r.getClass().isArray() || r instanceof CharSequence || r instanceof Number || r instanceof Boolean
            || r instanceof Character || r instanceof Enum) return true;
        try {
            return r.getClass().getMethod("toString").getDeclaringClass() != Object.class
                && !show(r).matches(".*@[0-9a-f]{5,8}.*");
        } catch (Throwable e) { return false; }
    }

    static String show(Object r) {
        return r.getClass().isArray() ? Arrays.deepToString(new Object[]{r}) : String.valueOf(r);
    }

    static String esc(String s) {
        StringBuilder b = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '\\' || c == '"') b.append('\\').append(c);
            // control chars as octal: a unicode escape of a newline would break Java source
            else if (c < 0x20 || c == 0x7f) b.append(String.format("\\%03o", (int) c));
            else if (c > 0x7f) b.append(String.format("\\u%04x", (int) c));
            else b.append(c);
        }
        return b.toString();
    }
}
