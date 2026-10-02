# Zest – Closure-126 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 499270 (valid 48.75%) |
| Cycles | 49 |
| Corpus | 18 |
| Zest branch coverage (total / valid) | 150 / 147 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 18 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 8 / 99 = 8.08% |
| Branch coverage | 0 / 67 = 0.00% |
| Test suite length (statements) | 18 |
| Mutation score | 79 / 84 = 94.05% |
| Fuzz time | 131s |
| Pipeline time | 299s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/126/t12001/Closure-126f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_126/b120_r1/src/com/google/javascript/jscomp/MinimizeExitPoints_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_126/b120_r1`
