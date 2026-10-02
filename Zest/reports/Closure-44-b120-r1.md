# Zest – Closure-44 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 399385 (valid 91.44%) |
| Cycles | 22 |
| Corpus | 65 |
| Zest branch coverage (total / valid) | 239 / 231 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 65 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 73 / 111 = 65.77% |
| Branch coverage | 43 / 64 = 67.19% |
| Test suite length (statements) | 65 |
| Mutation score | 72 / 218 = 33.03% |
| Fuzz time | 121s |
| Pipeline time | 277s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/44/t12001/Closure-44f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_44/b120_r1/src/com/google/javascript/jscomp/CodeConsumer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_44/b120_r1`
