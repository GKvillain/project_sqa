# Zest – Closure-70 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 457232 (valid 44.13%) |
| Cycles | 68 |
| Corpus | 13 |
| Zest branch coverage (total / valid) | 123 / 120 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 13 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 20 / 806 = 2.48% |
| Branch coverage | 1 / 621 = 0.16% |
| Test suite length (statements) | 13 |
| Mutation score | 964 / 966 = 99.79% |
| Fuzz time | 120s |
| Pipeline time | 237s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/70/t12001/Closure-70f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_70/b120_r1/src/com/google/javascript/jscomp/TypedScopeCreator_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_70/b120_r1`
