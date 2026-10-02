# Zest – JacksonDatabind-67 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 345408 (valid 90.40%) |
| Cycles | 40 |
| Corpus | 25 |
| Zest branch coverage (total / valid) | 164 / 161 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 25 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 46 / 793 = 5.80% |
| Branch coverage | 8 / 612 = 1.31% |
| Test suite length (statements) | 25 |
| Mutation score | 774 / 786 = 98.47% |
| Fuzz time | 126s |
| Pipeline time | 250s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/67/t12001/JacksonDatabind-67f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_67/b120_r1/src/com/fasterxml/jackson/databind/deser/BasicDeserializerFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_67/b120_r1`
