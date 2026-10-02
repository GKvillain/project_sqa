# Zest – Closure-38 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 397796 (valid 91.46%) |
| Cycles | 23 |
| Corpus | 65 |
| Zest branch coverage (total / valid) | 251 / 243 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 65 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 73 / 112 = 65.18% |
| Branch coverage | 41 / 66 = 62.12% |
| Test suite length (statements) | 65 |
| Mutation score | 76 / 222 = 34.23% |
| Fuzz time | 122s |
| Pipeline time | 286s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/38/t12001/Closure-38f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_38/b120_r1/src/com/google/javascript/jscomp/CodeConsumer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_38/b120_r1`
