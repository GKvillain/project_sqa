# Zest – Closure-91 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 413164 (valid 62.65%) |
| Cycles | 77 |
| Corpus | 14 |
| Zest branch coverage (total / valid) | 141 / 137 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 14 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 10 / 58 = 17.24% |
| Branch coverage | 2 / 74 = 2.70% |
| Test suite length (statements) | 14 |
| Mutation score | 134 / 138 = 97.10% |
| Fuzz time | 120s |
| Pipeline time | 204s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/91/t12001/Closure-91f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_91/b120_r1/src/com/google/javascript/jscomp/CheckGlobalThis_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_91/b120_r1`
