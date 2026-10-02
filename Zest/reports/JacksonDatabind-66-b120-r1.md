# Zest – JacksonDatabind-66 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 426660 (valid 60.08%) |
| Cycles | 11 |
| Corpus | 100 |
| Zest branch coverage (total / valid) | 249 / 246 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 100 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 82 / 160 = 51.25% |
| Branch coverage | 53 / 95 = 55.79% |
| Test suite length (statements) | 101 |
| Mutation score | 50 / 84 = 59.52% |
| Fuzz time | 127s |
| Pipeline time | 233s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/66/t12001/JacksonDatabind-66f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_66/b120_r1/src/com/fasterxml/jackson/databind/deser/std/StdKeyDeserializer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_66/b120_r1`
