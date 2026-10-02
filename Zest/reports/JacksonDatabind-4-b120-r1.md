# Zest – JacksonDatabind-4 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 410667 (valid 83.06%) |
| Cycles | 68 |
| Corpus | 14 |
| Zest branch coverage (total / valid) | 130 / 127 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 14 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 12 / 68 = 17.65% |
| Branch coverage | 0 / 36 = 0.00% |
| Test suite length (statements) | 14 |
| Mutation score | 46 / 53 = 86.79% |
| Fuzz time | 128s |
| Pipeline time | 222s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/4/t12001/JacksonDatabind-4f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_4/b120_r1/src/com/fasterxml/jackson/databind/deser/std/StringArrayDeserializer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_4/b120_r1`
