# Zest – Closure-15 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 449581 (valid 56.99%) |
| Cycles | 79 |
| Corpus | 12 |
| Zest branch coverage (total / valid) | 152 / 149 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 12 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 10 / 168 = 5.95% |
| Branch coverage | 1 / 121 = 0.83% |
| Test suite length (statements) | 12 |
| Mutation score | 184 / 184 = 100.00% |
| Fuzz time | 123s |
| Pipeline time | 276s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/15/t12001/Closure-15f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_15/b120_r1/src/com/google/javascript/jscomp/FlowSensitiveInlineVariables_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_15/b120_r1`
