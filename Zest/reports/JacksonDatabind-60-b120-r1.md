# Zest – JacksonDatabind-60 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 448599 (valid 100.00%) |
| Cycles | 138 |
| Corpus | 12 |
| Zest branch coverage (total / valid) | 111 / 111 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 12 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 3 / 149 = 2.01% |
| Branch coverage | 2 / 74 = 2.70% |
| Test suite length (statements) | 12 |
| Mutation score | 120 / 120 = 100.00% |
| Fuzz time | 127s |
| Pipeline time | 226s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/60/t12001/JacksonDatabind-60f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_60/b120_r1/src/com/fasterxml/jackson/databind/ser/std/JsonValueSerializer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_60/b120_r1`
