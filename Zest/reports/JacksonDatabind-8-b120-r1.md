# Zest – JacksonDatabind-8 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 541806 (valid 100.00%) |
| Cycles | 458 |
| Corpus | 5 |
| Zest branch coverage (total / valid) | 43 / 43 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 5 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 24 / 123 = 19.51% |
| Branch coverage | 3 / 72 = 4.17% |
| Test suite length (statements) | 6 |
| Mutation score | 114 / 135 = 84.44% |
| Fuzz time | 127s |
| Pipeline time | 229s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/8/t12001/JacksonDatabind-8f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_8/b120_r1/src/com/fasterxml/jackson/databind/deser/impl/CreatorCollector_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_8/b120_r1`
