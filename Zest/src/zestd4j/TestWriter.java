package zestd4j;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.apache.commons.lang3.math.NumberUtils;

public class TestWriter {
    public static void main(String[] a) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(a[0]));
        StringBuilder sb = new StringBuilder();
        sb.append("package org.apache.commons.lang3.math;\n")
          .append("import org.junit.Test;\nimport static org.junit.Assert.*;\n")
          .append("public class NumberUtils_Zest_Test {\n");
        int i = 0;
        for (String b64 : new LinkedHashSet<>(lines)) {
            String s = new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8);
            String lit = esc(s);
            sb.append("  @Test(timeout = 4000)\n  public void test").append(i++).append("() {\n");
            try {
                Number n = NumberUtils.createNumber(s);
                sb.append("    Number n = NumberUtils.createNumber(\"").append(lit).append("\");\n")
                  .append("    assertEquals(\"").append(n.getClass().getName()).append("\", n.getClass().getName());\n")
                  .append("    assertEquals(\"").append(esc(n.toString())).append("\", n.toString());\n");
            } catch (Throwable t) {
                sb.append("    try { NumberUtils.createNumber(\"").append(lit).append("\"); fail(\"expected ")
                  .append(t.getClass().getSimpleName()).append("\"); }\n")
                  .append("    catch (").append(t.getClass().getName()).append(" e) { }\n");
            }
            sb.append("  }\n");
        }
        sb.append("}\n");
        Files.write(Paths.get(a[1]), sb.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("Wrote " + i + " tests");
    }
    static String esc(String s) {
        StringBuilder b = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '\\' || c == '"') b.append('\\').append(c);
            else if (c < 0x20 || c > 0x7e) b.append(String.format("\\u%04x", (int) c));
            else b.append(c);
        }
        return b.toString();
    }
}
