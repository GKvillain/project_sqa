# Zest – JacksonDatabind-97 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 276280 (valid 96.75%) |
| Cycles | 31 |
| Corpus | 73 |
| Zest branch coverage (total / valid) | 957 / 954 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 73 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 29 / 41 = 70.73% |
| Branch coverage | 21 / 34 = 61.76% |
| Test suite length (statements) | 76 |
| Mutation score | 11 / 17 = 64.71% |
| Fuzz time | 126s |
| Pipeline time | 259s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/97/t12001/JacksonDatabind-97f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_97/b120_r1/src/com/fasterxml/jackson/databind/node/POJONode_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_97/b120_r1`
