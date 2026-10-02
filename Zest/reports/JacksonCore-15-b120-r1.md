# Zest – JacksonCore-15 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 558019 (valid 64.52%) |
| Cycles | 100 |
| Corpus | 17 |
| Zest branch coverage (total / valid) | 138 / 135 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 17 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 28 / 378 = 7.41% |
| Branch coverage | 7 / 269 = 2.60% |
| Test suite length (statements) | 17 |
| Mutation score | 324 / 344 = 94.19% |
| Fuzz time | 127s |
| Pipeline time | 191s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonCore/zest/15/t12001/JacksonCore-15f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_15/b120_r1/src/com/fasterxml/jackson/core/filter/FilteringParserDelegate_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_15/b120_r1`
