# Zest – JacksonDatabind-3 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 477964 (valid 63.34%) |
| Cycles | 68 |
| Corpus | 15 |
| Zest branch coverage (total / valid) | 130 / 127 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 15 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 13 / 62 = 20.97% |
| Branch coverage | 0 / 36 = 0.00% |
| Test suite length (statements) | 15 |
| Mutation score | 46 / 53 = 86.79% |
| Fuzz time | 128s |
| Pipeline time | 225s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/3/t12001/JacksonDatabind-3f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_3/b120_r1/src/com/fasterxml/jackson/databind/deser/std/StringArrayDeserializer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_3/b120_r1`
