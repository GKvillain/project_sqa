package zestd4j;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class InputLog {
    public static void record(String s) {
        String f = System.getenv("ZEST_DUMP");
        if (f == null) return;
        try (FileWriter w = new FileWriter(f, true)) {
            w.write(Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8)) + "\n");
        } catch (Exception ignored) {}
    }
}
