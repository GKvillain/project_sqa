# Zest – JacksonCore-21 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 381831 (valid 82.82%) |
| Cycles | 52 |
| Corpus | 17 |
| Zest branch coverage (total / valid) | 138 / 135 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 17 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 25 / 386 = 6.48% |
| Branch coverage | 6 / 271 = 2.21% |
| Test suite length (statements) | 17 |
| Mutation score | 332 / 343 = 96.79% |
| Fuzz time | 126s |
| Pipeline time | 203s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonCore/zest/21/t12001/JacksonCore-21f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_21/b120_r1/src/com/fasterxml/jackson/core/filter/FilteringParserDelegate_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_21/b120_r1`
