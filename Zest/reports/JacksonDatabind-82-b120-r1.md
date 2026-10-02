# Zest – JacksonDatabind-82 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 451451 (valid 3.58%) |
| Cycles | 58 |
| Corpus | 12 |
| Zest branch coverage (total / valid) | 127 / 35 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 12 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 32 / 355 = 9.01% |
| Branch coverage | 3 / 212 = 1.42% |
| Test suite length (statements) | 12 |
| Mutation score | 220 / 233 = 94.42% |
| Fuzz time | 126s |
| Pipeline time | 234s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/82/t12001/JacksonDatabind-82f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_82/b120_r1/src/com/fasterxml/jackson/databind/deser/BeanDeserializerFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_82/b120_r1`
