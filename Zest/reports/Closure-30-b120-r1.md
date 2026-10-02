# Zest – Closure-30 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 425588 (valid 55.04%) |
| Cycles | 40 |
| Corpus | 24 |
| Zest branch coverage (total / valid) | 162 / 159 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 24 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 15 / 337 = 4.45% |
| Branch coverage | 1 / 224 = 0.45% |
| Test suite length (statements) | 24 |
| Mutation score | 298 / 300 = 99.33% |
| Fuzz time | 123s |
| Pipeline time | 285s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/30/t12001/Closure-30f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_30/b120_r1/src/com/google/javascript/jscomp/FlowSensitiveInlineVariables_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_30/b120_r1`
