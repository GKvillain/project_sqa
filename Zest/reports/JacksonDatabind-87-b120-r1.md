# Zest – JacksonDatabind-87 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 370905 (valid 90.46%) |
| Cycles | 37 |
| Corpus | 90 |
| Zest branch coverage (total / valid) | 423 / 420 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 90 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 76 / 204 = 37.25% |
| Branch coverage | 22 / 136 = 16.18% |
| Test suite length (statements) | 90 |
| Mutation score | 298 / 366 = 81.42% |
| Fuzz time | 126s |
| Pipeline time | 247s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/87/t12001/JacksonDatabind-87f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_87/b120_r1/src/com/fasterxml/jackson/databind/util/StdDateFormat_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_87/b120_r1`
