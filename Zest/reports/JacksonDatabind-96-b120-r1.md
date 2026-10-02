# Zest – JacksonDatabind-96 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 301642 (valid 89.90%) |
| Cycles | 45 |
| Corpus | 19 |
| Zest branch coverage (total / valid) | 156 / 153 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 19 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 45 / 889 = 5.06% |
| Branch coverage | 7 / 614 = 1.14% |
| Test suite length (statements) | 19 |
| Mutation score | 821 / 835 = 98.32% |
| Fuzz time | 126s |
| Pipeline time | 269s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/96/t12001/JacksonDatabind-96f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_96/b120_r1/src/com/fasterxml/jackson/databind/deser/BasicDeserializerFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_96/b120_r1`
