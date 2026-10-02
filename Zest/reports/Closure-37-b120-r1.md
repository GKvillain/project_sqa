# Zest – Closure-37 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 414484 (valid 72.96%) |
| Cycles | 38 |
| Corpus | 27 |
| Zest branch coverage (total / valid) | 157 / 154 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 26 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 39 / 835 = 4.67% |
| Branch coverage | 9 / 387 = 2.33% |
| Test suite length (statements) | 26 |
| Mutation score | 606 / 625 = 96.96% |
| Fuzz time | 124s |
| Pipeline time | 282s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/37/t12001/Closure-37f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_37/b120_r1/src/com/google/javascript/jscomp/NodeTraversal_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_37/b120_r1`
