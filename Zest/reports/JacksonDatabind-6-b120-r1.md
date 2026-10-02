# Zest – JacksonDatabind-6 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 363292 (valid 90.02%) |
| Cycles | 41 |
| Corpus | 64 |
| Zest branch coverage (total / valid) | 536 / 534 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 64 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 101 / 163 = 61.96% |
| Branch coverage | 54 / 106 = 50.94% |
| Test suite length (statements) | 64 |
| Mutation score | 181 / 301 = 60.13% |
| Fuzz time | 128s |
| Pipeline time | 237s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/6/t12001/JacksonDatabind-6f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_6/b120_r1/src/com/fasterxml/jackson/databind/util/StdDateFormat_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_6/b120_r1`
