# Zest – JacksonDatabind-28 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 376540 (valid 73.95%) |
| Cycles | 103 |
| Corpus | 15 |
| Zest branch coverage (total / valid) | 143 / 140 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 14 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 8 / 30 = 26.67% |
| Branch coverage | 2 / 15 = 13.33% |
| Test suite length (statements) | 14 |
| Mutation score | 8 / 10 = 80.00% |
| Fuzz time | 128s |
| Pipeline time | 278s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/28/t12001/JacksonDatabind-28f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_28/b120_r1/src/com/fasterxml/jackson/databind/deser/std/JsonNodeDeserializer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_28/b120_r1`
