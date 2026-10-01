#!/usr/bin/env bash
# Scaffold a new bug:  ./new_bug.sh <PID> <BID>     e.g.  ./new_bug.sh Csv 4
# Creates bugs/<PID>_<BID>.env and src/<PID>_<BID>/<Name>Fuzz.java, then shows the bug info.
set -uo pipefail
[ $# -lt 2 ] && { echo "Usage: $0 <PID> <BID>"; exit 1; }
PID=$1; BID=$2; KEY=${PID}_${BID}
WORK=${WORK:-$HOME/zest-d4j}
export TZ=America/Los_Angeles
for v in b f; do
  dir=$HOME/d4j/${KEY}$v
  [ -d "$dir" ] || defects4j checkout -p "$PID" -v "${BID}$v" -w "$dir" >/dev/null 2>&1
  (cd "$dir" && defects4j compile >/dev/null 2>&1) || echo "WARNING: compile ${BID}$v failed"
done
cd "$HOME/d4j/${KEY}f"
defects4j info -p "$PID" -b "$BID" | sed -n '/Summary for Bug/,$p'
TARGET=$(defects4j export -p classes.modified 2>/dev/null | head -1)
SIMPLE=${TARGET##*.}
echo; echo "Modified class : $TARGET"
echo "Trigger tests  :"; defects4j export -p tests.trigger 2>/dev/null | sed 's/^/  /'

mkdir -p "$WORK/src/$KEY" "$WORK/bugs"
DRV="$WORK/src/$KEY/${SIMPLE}Fuzz.java"
if [ ! -f "$DRV" ]; then
cat > "$DRV" <<JAVA
package zestd4j;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;
import static org.junit.Assume.assumeTrue;

// $PID-$BID fuzz driver. TODO:
//  1) call the buggy method (see "Root cause" above)
//  2) optionally add a structured generator: @From(MyGenerator.class) String s
//     (put MyGenerator.java in this same folder; see src/Lang_1/NumericStringGenerator.java)
//  3) catch the "expected / invalid input" exception and call assumeTrue(false)
@RunWith(JQF.class)
public class ${SIMPLE}Fuzz {
    @Fuzz
    public void fuzz(String s) {
        InputLog.record(s);                      // keep: needed to turn the corpus into JUnit tests
        try {
            $TARGET.TODO_METHOD(s);
        } catch (IllegalArgumentException e) {   // TODO: the exception that means "invalid input"
            assumeTrue(false);
        }
    }
}
JAVA
fi
ENV="$WORK/bugs/$KEY.env"
if [ ! -f "$ENV" ]; then
cat > "$ENV" <<CFG
# $PID-$BID
PID=$PID
BID=$BID
DRIVER=zestd4j.${SIMPLE}Fuzz
METHOD=fuzz
# generic writer: static method with one String argument
WRITER=generic
TARGET_CLASS=$TARGET
TARGET_METHOD=TODO_METHOD
# for anything else, write your own writer class and use:
# WRITER=zestd4j.MyTestWriter
# TEST_FILE=path/to/pkg/My_Zest_Test.java
CFG
fi
echo
echo "Next:"
echo "  1) edit $DRV"
echo "  2) edit $ENV   (TARGET_METHOD)"
echo "  3) quick check : ./zest_pipeline.sh bugs/$KEY.env 60 1"
echo "  4) full runs   : ./run_all.sh \"60 300 900\" 5 bugs/$KEY.env"
