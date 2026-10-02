# Zest – Closure-72 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 399453 (valid 55.53%) |
| Cycles | 23 |
| Corpus | 38 |
| Zest branch coverage (total / valid) | 186 / 183 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 38 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 35 / 218 = 16.06% |
| Branch coverage | 7 / 103 = 6.80% |
| Test suite length (statements) | 38 |
| Mutation score | 166 / 192 = 86.46% |
| Fuzz time | 120s |
| Pipeline time | 233s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/72/t12001/Closure-72f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_72/b120_r1/src/com/google/javascript/jscomp/FunctionToBlockMutator_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_72/b120_r1`
