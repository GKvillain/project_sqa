# Zest – Closure-21 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 479328 (valid 61.51%) |
| Cycles | 85 |
| Corpus | 17 |
| Zest branch coverage (total / valid) | 218 / 214 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 17 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 19 / 70 = 27.14% |
| Branch coverage | 1 / 38 = 2.63% |
| Test suite length (statements) | 17 |
| Mutation score | 54 / 63 = 85.71% |
| Fuzz time | 123s |
| Pipeline time | 282s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/21/t12001/Closure-21f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_21/b120_r1/src/com/google/javascript/jscomp/CheckSideEffects_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_21/b120_r1`
