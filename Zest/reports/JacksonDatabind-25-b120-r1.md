# Zest – JacksonDatabind-25 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 440300 (valid 66.31%) |
| Cycles | 40 |
| Corpus | 24 |
| Zest branch coverage (total / valid) | 182 / 179 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 24 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 48 / 1058 = 4.54% |
| Branch coverage | 5 / 770 = 0.65% |
| Test suite length (statements) | 24 |
| Mutation score | 963 / 977 = 98.57% |
| Fuzz time | 129s |
| Pipeline time | 222s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/25/t12001/JacksonDatabind-25f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_25/b120_r1/src/com/fasterxml/jackson/databind/deser/BasicDeserializerFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_25/b120_r1`
