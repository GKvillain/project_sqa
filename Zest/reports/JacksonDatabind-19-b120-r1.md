# Zest – JacksonDatabind-19 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 514420 (valid 100.00%) |
| Cycles | 428 |
| Corpus | 5 |
| Zest branch coverage (total / valid) | 35 / 35 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 5 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 48 / 349 = 13.75% |
| Branch coverage | 18 / 232 = 7.76% |
| Test suite length (statements) | 5 |
| Mutation score | 346 / 360 = 96.11% |
| Fuzz time | 127s |
| Pipeline time | 233s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/19/t12001/JacksonDatabind-19f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_19/b120_r1/src/com/fasterxml/jackson/databind/type/TypeFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_19/b120_r1`
