#!/bin/bash

set -o pipefail

# ============================================================
# Usage:
#   ./run_all_gwo.sh <PROJECT> <START_BUG> <END_BUG>
#
# Examples:
#   ./run_all_gwo.sh Chart 1 26
#   ./run_all_gwo.sh Lang 1 65
#   ./run_all_gwo.sh Math 1 106
#   ./run_all_gwo.sh Time 1 27
#   ./run_all_gwo.sh Closure 1 133
# ============================================================

if [ $# -lt 3 ]; then
    echo "Usage: $0 <PROJECT> <START_BUG> <END_BUG>"
    echo
    echo "Examples:"
    echo "  $0 Chart 1 26"
    echo "  $0 Lang 1 65"
    echo "  $0 Math 1 106"
    echo "  $0 Time 1 27"
    echo "  $0 Closure 1 133"
    exit 1
fi

PROJECT="$1"
START_BUG="$2"
END_BUG="$3"

# ============================================================
# Paths
# ============================================================

WORKSPACE="$HOME/d4j-workspace"

# Result:
# ~/Result_Round1/
# ├── Chart/
# ├── Lang/
# ├── Math/
# ├── Time/
# └── Closure/

RESULT_BASE="$HOME/Result_Round1/$PROJECT"

EVOSUITE_JAR="$HOME/evosuite-gwo/evosuite/master/target/evosuite-master-1.2.1-SNAPSHOT.jar"


# ============================================================
# Validate input
# ============================================================

if ! [[ "$START_BUG" =~ ^[0-9]+$ ]] || ! [[ "$END_BUG" =~ ^[0-9]+$ ]]; then
    echo "ERROR: START_BUG and END_BUG must be numbers."
    exit 1
fi

if [ "$START_BUG" -gt "$END_BUG" ]; then
    echo "ERROR: START_BUG must be <= END_BUG."
    exit 1
fi

if [ ! -f "$EVOSUITE_JAR" ]; then
    echo "ERROR: EvoSuite JAR not found:"
    echo "$EVOSUITE_JAR"
    exit 1
fi

mkdir -p "$RESULT_BASE"


# ============================================================
# Header
# ============================================================

echo
echo "======================================================"
echo "EvoSuite GWO"
echo "======================================================"
echo "Project       : $PROJECT"
echo "Bug range     : ${START_BUG}-${END_BUG}"
echo "Workspace     : $WORKSPACE"
echo "Result base   : $RESULT_BASE"
echo "EvoSuite JAR  : $EVOSUITE_JAR"
echo "======================================================"
echo


# ============================================================
# Run each bug
# ============================================================

for BUG in $(seq "$START_BUG" "$END_BUG"); do

    PROJECT_DIR="$WORKSPACE/${PROJECT}-${BUG}b"

    # --------------------------------------------------------
    # Result structure
    #
    # Result_Round1/
    # └── Chart/
    #     └── Chart-10b/
    #         ├── GWO/
    #         └── GWO-compile.log
    # --------------------------------------------------------

    BUG_RESULT_BASE="$RESULT_BASE/${PROJECT}-${BUG}b"
    RESULT_DIR="$BUG_RESULT_BASE/GWO"

    echo
    echo "======================================================"
    echo "Running ${PROJECT}-${BUG}b"
    echo "======================================================"


    # --------------------------------------------------------
    # Check project directory
    # --------------------------------------------------------

    if [ ! -d "$PROJECT_DIR" ]; then
        echo "SKIP: Project directory not found:"
        echo "$PROJECT_DIR"
        continue
    fi

    cd "$PROJECT_DIR" || {
        echo "SKIP: Cannot enter $PROJECT_DIR"
        continue
    }


    # --------------------------------------------------------
    # Create result directory
    # --------------------------------------------------------

    mkdir -p "$RESULT_DIR"


    # --------------------------------------------------------
    # Clean old build files
    # --------------------------------------------------------

    echo "Cleaning old build files..."

    rm -rf build build-tests


    # --------------------------------------------------------
    # Compile
    # --------------------------------------------------------

    echo "Compiling ${PROJECT}-${BUG}b..."

    defects4j compile > "$BUG_RESULT_BASE/GWO-compile.log" 2>&1

    COMPILE_EXIT=$?

    if [ $COMPILE_EXIT -ne 0 ]; then
        echo "COMPILE FAILED: ${PROJECT}-${BUG}b"
        echo "See:"
        echo "$BUG_RESULT_BASE/GWO-compile.log"
        continue
    fi

    echo "Compile successful."


    # --------------------------------------------------------
    # Export classpath
    # --------------------------------------------------------

    echo "Exporting classpath..."

    CP_COMPILE="$(defects4j export -p cp.compile 2>/dev/null)"
    CP_TEST="$(defects4j export -p cp.test 2>/dev/null)"
    BIN_CLASSES="$(defects4j export -p dir.bin.classes 2>/dev/null)"
    BIN_TESTS="$(defects4j export -p dir.bin.tests 2>/dev/null)"

    VALID_CP=""


    # --------------------------------------------------------
    # Function: add valid classpath item
    # --------------------------------------------------------

    add_cp() {

        local ITEM="$1"

        [ -z "$ITEM" ] && return

        if [ -f "$ITEM" ] || [ -d "$ITEM" ]; then

            if [ -z "$VALID_CP" ]; then
                VALID_CP="$ITEM"
            else
                VALID_CP="$VALID_CP:$ITEM"
            fi

        fi
    }


    # --------------------------------------------------------
    # Add compile/test dependencies
    # --------------------------------------------------------

    OLD_IFS="$IFS"
    IFS=':'

    for ITEM in $CP_COMPILE; do
        add_cp "$ITEM"
    done

    for ITEM in $CP_TEST; do
        add_cp "$ITEM"
    done

    IFS="$OLD_IFS"


    # --------------------------------------------------------
    # Add compiled classes / test classes
    # --------------------------------------------------------

    add_cp "$PROJECT_DIR/$BIN_CLASSES"
    add_cp "$PROJECT_DIR/$BIN_TESTS"

    add_cp "$PROJECT_DIR/build"
    add_cp "$PROJECT_DIR/build-tests"


    # --------------------------------------------------------
    # Show classpath
    # --------------------------------------------------------

    echo
    echo "Classpath:"
    echo "$VALID_CP"
    echo


    # --------------------------------------------------------
    # Get modified classes
    # --------------------------------------------------------

    TARGETS="$(defects4j export -p classes.modified 2>/dev/null)"

    if [ -z "$TARGETS" ]; then
        echo "No modified classes found for ${PROJECT}-${BUG}b"
        continue
    fi


    # --------------------------------------------------------
    # Run GWO for each modified class
    # --------------------------------------------------------

    while IFS= read -r TARGET; do

        [ -z "$TARGET" ] && continue

        SAFE_TARGET="$(echo "$TARGET" | tr '.' '_')"

        TARGET_DIR="$RESULT_DIR/$SAFE_TARGET"
        TARGET_LOG="$TARGET_DIR/terminal.log"

        mkdir -p "$TARGET_DIR"

        echo
        echo "------------------------------------------------------"
        echo "${PROJECT}-${BUG}b"
        echo "Target class: $TARGET"
        echo "Output: $TARGET_DIR"
        echo "------------------------------------------------------"


        # ----------------------------------------------------
        # Skip if test already generated
        # ----------------------------------------------------

        if [ -f "$TARGET_DIR/evosuite-tests/${SAFE_TARGET}_ESTest.java" ]; then
            echo "Already generated. Skipping $TARGET"
            continue
        fi


        # ----------------------------------------------------
        # Run EvoSuite GWO
        # ----------------------------------------------------

        cd "$TARGET_DIR" || continue

        java -jar "$EVOSUITE_JAR" \
            -generateSuite \
            -Dalgorithm=GWO \
            -Dsearch_budget=60 \
            -Dtest_dir="$TARGET_DIR/evosuite-tests" \
            -class "$TARGET" \
            -projectCP "$VALID_CP" \
            2>&1 | tee "$TARGET_LOG"

        EXIT_CODE=${PIPESTATUS[0]}


        # ----------------------------------------------------
        # Check result
        # ----------------------------------------------------

        if grep -q "Generated .* tests with total length" "$TARGET_LOG"; then

            echo "GENERATED: $TARGET"

        elif grep -q "Search finished after" "$TARGET_LOG"; then

            echo "SEARCH FINISHED BUT TEST GENERATION MAY HAVE FAILED: $TARGET"

        else

            echo "FAILED: $TARGET"

        fi

        echo "Exit code: $EXIT_CODE"


    done <<< "$TARGETS"


    echo
    echo "Finished ${PROJECT}-${BUG}b"

done


# ============================================================
# Finished
# ============================================================

echo
echo "======================================================"
echo "All ${PROJECT} bugs finished"
echo "Bug range: ${START_BUG}-${END_BUG}"
echo "Result directory: $RESULT_BASE"
echo "======================================================"