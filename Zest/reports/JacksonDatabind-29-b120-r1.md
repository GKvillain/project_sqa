# Zest – JacksonDatabind-29 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 511128 (valid 6.13%) |
| Cycles | 104 |
| Corpus | 8 |
| Zest branch coverage (total / valid) | 107 / 35 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 8 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 8 / 146 = 5.48% |
| Branch coverage | 0 / 66 = 0.00% |
| Test suite length (statements) | 11 |
| Mutation score | 108 / 108 = 100.00% |
| Fuzz time | 128s |
| Pipeline time | 240s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/29/t12001/JacksonDatabind-29f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_29/b120_r1/src/com/fasterxml/jackson/databind/deser/impl/ExternalTypeHandler_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_29/b120_r1`
