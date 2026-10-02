# Zest – JacksonDatabind-40 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 382057 (valid 100.00%) |
| Cycles | 100 |
| Corpus | 10 |
| Zest branch coverage (total / valid) | 111 / 111 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 10 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 27 / 223 = 12.11% |
| Branch coverage | 9 / 134 = 6.72% |
| Test suite length (statements) | 10 |
| Mutation score | 129 / 136 = 94.85% |
| Fuzz time | 127s |
| Pipeline time | 235s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/40/t12001/JacksonDatabind-40f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_40/b120_r1/src/com/fasterxml/jackson/databind/deser/std/NumberDeserializers_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_40/b120_r1`
