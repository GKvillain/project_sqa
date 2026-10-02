# Zest – JacksonCore-13 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 399497 (valid 9.36%) |
| Cycles | 122 |
| Corpus | 8 |
| Zest branch coverage (total / valid) | 122 / 35 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 8 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 9 / 35 = 25.71% |
| Branch coverage | 4 / 14 = 28.57% |
| Test suite length (statements) | 8 |
| Mutation score | 25 / 31 = 80.65% |
| Fuzz time | 128s |
| Pipeline time | 265s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonCore/zest/13/t12001/JacksonCore-13f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_13/b120_r1/src/com/fasterxml/jackson/core/json/JsonGeneratorImpl_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_13/b120_r1`
