# Zest – Closure-105 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 399697 (valid 62.27%) |
| Cycles | 25 |
| Corpus | 60 |
| Zest branch coverage (total / valid) | 190 / 183 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 60 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 5 / 994 = 0.50% |
| Branch coverage | 1 / 739 = 0.14% |
| Test suite length (statements) | 60 |
| Mutation score | 1405 / 1445 = 97.23% |
| Fuzz time | 120s |
| Pipeline time | 211s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/105/t12001/Closure-105f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_105/b120_r1/src/com/google/javascript/jscomp/FoldConstants_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_105/b120_r1`
