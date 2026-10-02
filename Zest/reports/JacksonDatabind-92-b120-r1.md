# Zest – JacksonDatabind-92 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 510461 (valid 6.31%) |
| Cycles | 80 |
| Corpus | 10 |
| Zest branch coverage (total / valid) | 123 / 35 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 10 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 34 / 349 = 9.74% |
| Branch coverage | 1 / 204 = 0.49% |
| Test suite length (statements) | 10 |
| Mutation score | 216 / 237 = 91.14% |
| Fuzz time | 126s |
| Pipeline time | 235s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/92/t12001/JacksonDatabind-92f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_92/b120_r1/src/com/fasterxml/jackson/databind/deser/BeanDeserializerFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_92/b120_r1`
