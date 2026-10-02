# Zest – JacksonDatabind-45 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 505180 (valid 55.19%) |
| Cycles | 46 |
| Corpus | 26 |
| Zest branch coverage (total / valid) | 158 / 155 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 26 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 17 / 41 = 41.46% |
| Branch coverage | 9 / 38 = 23.68% |
| Test suite length (statements) | 26 |
| Mutation score | 43 / 56 = 76.79% |
| Fuzz time | 127s |
| Pipeline time | 224s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/45/t12001/JacksonDatabind-45f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_45/b120_r1/src/com/fasterxml/jackson/databind/ser/std/DateTimeSerializerBase_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_45/b120_r1`
