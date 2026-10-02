# Zest – JacksonDatabind-103 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 344020 (valid 50.09%) |
| Cycles | 8 |
| Corpus | 97 |
| Zest branch coverage (total / valid) | 263 / 260 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 97 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 188 / 4259 = 4.41% |
| Branch coverage | 40 / 2537 = 1.58% |
| Test suite length (statements) | 97 |
| Mutation score | 3085 / 3127 = 98.66% |
| Fuzz time | 125s |
| Pipeline time | 265s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/103/t12001/JacksonDatabind-103f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_103/b120_r1/src/com/fasterxml/jackson/databind/DatabindContext_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_103/b120_r1`
