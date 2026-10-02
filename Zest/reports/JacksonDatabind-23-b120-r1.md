# Zest – JacksonDatabind-23 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 415422 (valid 79.40%) |
| Cycles | 114 |
| Corpus | 9 |
| Zest branch coverage (total / valid) | 132 / 123 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 8 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 36 / 84 = 42.86% |
| Branch coverage | 5 / 22 = 22.73% |
| Test suite length (statements) | 8 |
| Mutation score | 23 / 47 = 48.94% |
| Fuzz time | 128s |
| Pipeline time | 221s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/23/t12001/JacksonDatabind-23f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_23/b120_r1/src/com/fasterxml/jackson/databind/ser/std/NumberSerializers_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_23/b120_r1`
