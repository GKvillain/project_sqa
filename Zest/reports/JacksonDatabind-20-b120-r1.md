# Zest – JacksonDatabind-20 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 322847 (valid 94.50%) |
| Cycles | 22 |
| Corpus | 79 |
| Zest branch coverage (total / valid) | 1001 / 998 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 79 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 60 / 186 = 32.26% |
| Branch coverage | 30 / 84 = 35.71% |
| Test suite length (statements) | 79 |
| Mutation score | 70 / 95 = 73.68% |
| Fuzz time | 129s |
| Pipeline time | 243s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/20/t12001/JacksonDatabind-20f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_20/b120_r1/src/com/fasterxml/jackson/databind/node/ObjectNode_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_20/b120_r1`
