package zestd4j;
import com.pholser.junit.quickcheck.generator.*;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

public class NumericStringGenerator extends Generator<String> {
    private static final String[] PREFIX = {"", "-", "+", "0x", "0X", "-0x", "#", "0"};
    private static final String[] SUFFIX = {"", "L", "l", "F", "f", "D", "d", "e5", "E-3", "e+10"};
    public NumericStringGenerator() { super(String.class); }

    @Override
    public String generate(SourceOfRandomness r, GenerationStatus st) {
        String p = r.choose(PREFIX);
        boolean hex = p.contains("x") || p.contains("X") || p.equals("#");
        String digits = hex ? "0123456789abcdefABCDEF" : "0123456789";
        StringBuilder sb = new StringBuilder(p);
        int len = r.nextInt(1, 20);
        for (int i = 0; i < len; i++) sb.append(digits.charAt(r.nextInt(digits.length())));
        if (!hex && r.nextBoolean()) {
            sb.append('.');
            int f = r.nextInt(0, 8);
            for (int i = 0; i < f; i++) sb.append(digits.charAt(r.nextInt(10)));
        }
        sb.append(r.choose(SUFFIX));
        return sb.toString();
    }
}
