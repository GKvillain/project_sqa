# Zest – Closure-36 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 510685 (valid 54.47%) |
| Cycles | 94 |
| Corpus | 10 |
| Zest branch coverage (total / valid) | 171 / 163 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 10 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 23 / 291 = 7.90% |
| Branch coverage | 3 / 234 = 1.28% |
| Test suite length (statements) | 10 |
| Mutation score | 398 / 398 = 100.00% |
| Fuzz time | 123s |
| Pipeline time | 279s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/36/t12001/Closure-36f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_36/b120_r1/src/com/google/javascript/jscomp/InlineVariables_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_36/b120_r1`
