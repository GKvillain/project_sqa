# Zest – JacksonDatabind-70 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 432515 (valid 100.00%) |
| Cycles | 61 |
| Corpus | 20 |
| Zest branch coverage (total / valid) | 87 / 87 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 20 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 12 / 226 = 5.31% |
| Branch coverage | 6 / 138 = 4.35% |
| Test suite length (statements) | 20 |
| Mutation score | 495 / 500 = 99.00% |
| Fuzz time | 125s |
| Pipeline time | 261s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/70/t12001/JacksonDatabind-70f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_70/b120_r1/src/com/fasterxml/jackson/databind/deser/impl/BeanPropertyMap_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_70/b120_r1`
