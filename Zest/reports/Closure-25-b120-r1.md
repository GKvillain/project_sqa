# Zest – Closure-25 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 548212 (valid 54.70%) |
| Cycles | 80 |
| Corpus | 19 |
| Zest branch coverage (total / valid) | 161 / 154 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 19 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 8 / 607 = 1.32% |
| Branch coverage | 2 / 402 = 0.50% |
| Test suite length (statements) | 19 |
| Mutation score | 570 / 570 = 100.00% |
| Fuzz time | 122s |
| Pipeline time | 279s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/25/t12001/Closure-25f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_25/b120_r1/src/com/google/javascript/jscomp/TypeInference_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_25/b120_r1`
