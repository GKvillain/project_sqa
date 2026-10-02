# Zest – Closure-100 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 401759 (valid 79.63%) |
| Cycles | 80 |
| Corpus | 9 |
| Zest branch coverage (total / valid) | 141 / 138 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 9 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 9 / 45 = 20.00% |
| Branch coverage | 2 / 56 = 3.57% |
| Test suite length (statements) | 9 |
| Mutation score | 93 / 99 = 93.94% |
| Fuzz time | 120s |
| Pipeline time | 205s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/100/t12001/Closure-100f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_100/b120_r1/src/com/google/javascript/jscomp/CheckGlobalThis_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_100/b120_r1`
