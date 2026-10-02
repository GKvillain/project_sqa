# Zest – JacksonDatabind-71 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 390036 (valid 65.10%) |
| Cycles | 12 |
| Corpus | 94 |
| Zest branch coverage (total / valid) | 249 / 246 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 94 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 78 / 149 = 52.35% |
| Branch coverage | 56 / 97 = 57.73% |
| Test suite length (statements) | 95 |
| Mutation score | 47 / 86 = 54.65% |
| Fuzz time | 127s |
| Pipeline time | 243s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/71/t12001/JacksonDatabind-71f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_71/b120_r1/src/com/fasterxml/jackson/databind/deser/std/StdKeyDeserializer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_71/b120_r1`
