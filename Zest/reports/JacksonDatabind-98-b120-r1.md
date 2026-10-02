# Zest – JacksonDatabind-98 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 406773 (valid 49.77%) |
| Cycles | 58 |
| Corpus | 21 |
| Zest branch coverage (total / valid) | 165 / 162 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 21 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 15 / 213 = 7.04% |
| Branch coverage | 0 / 94 = 0.00% |
| Test suite length (statements) | 56 |
| Mutation score | 160 / 160 = 100.00% |
| Fuzz time | 124s |
| Pipeline time | 249s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/98/t12001/JacksonDatabind-98f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_98/b120_r1/src/com/fasterxml/jackson/databind/deser/impl/ExternalTypeHandler_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_98/b120_r1`
