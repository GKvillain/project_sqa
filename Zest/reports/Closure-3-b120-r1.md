# Zest – Closure-3 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 586654 (valid 62.01%) |
| Cycles | 110 |
| Corpus | 16 |
| Zest branch coverage (total / valid) | 158 / 155 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 16 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 13 / 180 = 7.22% |
| Branch coverage | 1 / 132 = 0.76% |
| Test suite length (statements) | 16 |
| Mutation score | 195 / 196 = 99.49% |
| Fuzz time | 125s |
| Pipeline time | 277s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/3/t12001/Closure-3f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_3/b120_r1/src/com/google/javascript/jscomp/FlowSensitiveInlineVariables_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_3/b120_r1`
