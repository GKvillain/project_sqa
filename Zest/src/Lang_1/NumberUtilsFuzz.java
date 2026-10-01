package zestd4j;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.runner.RunWith;
import static org.junit.Assume.assumeTrue;

@RunWith(JQF.class)
public class NumberUtilsFuzz {
    @Fuzz
    public void createNumber(@From(NumericStringGenerator.class) String s) {
        InputLog.record(s);
        try {
            NumberUtils.createNumber(s);
        } catch (NumberFormatException e) {
            assumeTrue(false);
        }
    }
}
