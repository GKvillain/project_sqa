# Zest – JacksonDatabind-62 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 406918 (valid 100.00%) |
| Cycles | 92 |
| Corpus | 16 |
| Zest branch coverage (total / valid) | 124 / 124 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 16 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 11 / 137 = 8.03% |
| Branch coverage | 0 / 74 = 0.00% |
| Test suite length (statements) | 16 |
| Mutation score | 102 / 102 = 100.00% |
| Fuzz time | 125s |
| Pipeline time | 227s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/62/t12001/JacksonDatabind-62f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_62/b120_r1/src/com/fasterxml/jackson/databind/deser/std/CollectionDeserializer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_62/b120_r1`
