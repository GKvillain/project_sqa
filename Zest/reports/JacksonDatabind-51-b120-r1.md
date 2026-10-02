# Zest – JacksonDatabind-51 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 497325 (valid 13.31%) |
| Cycles | 208 |
| Corpus | 6 |
| Zest branch coverage (total / valid) | 105 / 35 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 6 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 2 / 75 = 2.67% |
| Branch coverage | 0 / 36 = 0.00% |
| Test suite length (statements) | 6 |
| Mutation score | 43 / 43 = 100.00% |
| Fuzz time | 127s |
| Pipeline time | 232s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/51/t12001/JacksonDatabind-51f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_51/b120_r1/src/com/fasterxml/jackson/databind/jsontype/impl/TypeDeserializerBase_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_51/b120_r1`
