# Zest – JacksonDatabind-58 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 480111 (valid 4.50%) |
| Cycles | 61 |
| Corpus | 11 |
| Zest branch coverage (total / valid) | 124 / 35 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 11 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 17 / 323 = 5.26% |
| Branch coverage | 1 / 200 = 0.50% |
| Test suite length (statements) | 11 |
| Mutation score | 209 / 214 = 97.66% |
| Fuzz time | 125s |
| Pipeline time | 232s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/58/t12001/JacksonDatabind-58f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_58/b120_r1/src/com/fasterxml/jackson/databind/deser/BeanDeserializerFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_58/b120_r1`
