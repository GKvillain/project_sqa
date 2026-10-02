# Zest – JacksonDatabind-13 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 466075 (valid 53.84%) |
| Cycles | 109 |
| Corpus | 8 |
| Zest branch coverage (total / valid) | 127 / 98 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 7 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 3 / 136 = 2.21% |
| Branch coverage | 0 / 72 = 0.00% |
| Test suite length (statements) | 7 |
| Mutation score | 56 / 56 = 100.00% |
| Fuzz time | 129s |
| Pipeline time | 224s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/13/t12001/JacksonDatabind-13f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_13/b120_r1/src/com/fasterxml/jackson/databind/deser/DefaultDeserializationContext_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_13/b120_r1`
