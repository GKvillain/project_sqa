# Zest – JacksonDatabind-39 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 425892 (valid 58.62%) |
| Cycles | 90 |
| Corpus | 11 |
| Zest branch coverage (total / valid) | 124 / 121 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 11 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 4 / 13 = 30.77% |
| Branch coverage | 0 / 8 = 0.00% |
| Test suite length (statements) | 11 |
| Mutation score | 8 / 10 = 80.00% |
| Fuzz time | 128s |
| Pipeline time | 235s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/39/t12001/JacksonDatabind-39f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_39/b120_r1/src/com/fasterxml/jackson/databind/deser/std/NullifyingDeserializer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_39/b120_r1`
