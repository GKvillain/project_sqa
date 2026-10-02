# Zest – Math-7 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 114 (valid 92.98%) |
| Cycles | 0 |
| Corpus | 23 |
| Zest branch coverage (total / valid) | 466 / 446 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 21 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 25 / 129 = 19.38% |
| Branch coverage | 3 / 58 = 5.17% |
| Test suite length (statements) | 16 |
| Mutation score | 117 / 137 = 85.40% |
| Fuzz time | 122s |
| Pipeline time | 510s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Math/zest/7/t12001/Math-7f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Math_7/b120_r1/src/org/apache/commons/math3/ode/AbstractIntegrator_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Math_7/b120_r1`
