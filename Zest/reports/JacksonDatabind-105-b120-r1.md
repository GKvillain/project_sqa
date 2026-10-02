# Zest – JacksonDatabind-105 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 356723 (valid 100.00%) |
| Cycles | 126 |
| Corpus | 11 |
| Zest branch coverage (total / valid) | 112 / 112 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 11 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 8 / 21 = 38.10% |
| Branch coverage | 1 / 14 = 7.14% |
| Test suite length (statements) | 11 |
| Mutation score | 6 / 10 = 60.00% |
| Fuzz time | 125s |
| Pipeline time | 263s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/105/t12001/JacksonDatabind-105f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_105/b120_r1/src/com/fasterxml/jackson/databind/deser/std/JdkDeserializers_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_105/b120_r1`
