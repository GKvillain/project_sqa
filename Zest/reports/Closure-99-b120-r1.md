# Zest – Closure-99 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 418045 (valid 64.18%) |
| Cycles | 81 |
| Corpus | 12 |
| Zest branch coverage (total / valid) | 141 / 137 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 12 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 10 / 51 = 19.61% |
| Branch coverage | 2 / 62 = 3.23% |
| Test suite length (statements) | 12 |
| Mutation score | 109 / 113 = 96.46% |
| Fuzz time | 120s |
| Pipeline time | 200s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/99/t12001/Closure-99f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_99/b120_r1/src/com/google/javascript/jscomp/CheckGlobalThis_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_99/b120_r1`
