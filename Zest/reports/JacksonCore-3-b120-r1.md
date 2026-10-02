# Zest – JacksonCore-3 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 467069 (valid 100.00%) |
| Cycles | 163 |
| Corpus | 6 |
| Zest branch coverage (total / valid) | 45 / 45 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 6 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 4 / 1540 = 0.26% |
| Branch coverage | 0 / 1016 = 0.00% |
| Test suite length (statements) | 6 |
| Mutation score | 3601 / 3601 = 100.00% |
| Fuzz time | 129s |
| Pipeline time | 195s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonCore/zest/3/t12001/JacksonCore-3f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_3/b120_r1/src/com/fasterxml/jackson/core/json/UTF8StreamJsonParser_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_3/b120_r1`
