# Zest – JacksonCore-12 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 471434 (valid 7.70%) |
| Cycles | 114 |
| Corpus | 6 |
| Zest branch coverage (total / valid) | 109 / 32 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 6 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 5 / 3192 = 0.16% |
| Branch coverage | 0 / 2260 = 0.00% |
| Test suite length (statements) | 6 |
| Mutation score | 6801 / 6801 = 100.00% |
| Fuzz time | 126s |
| Pipeline time | 192s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonCore/zest/12/t12001/JacksonCore-12f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_12/b120_r1/src/com/fasterxml/jackson/core/json/ReaderBasedJsonParser_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_12/b120_r1`
