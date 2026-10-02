# Zest – JacksonDatabind-34 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 380216 (valid 85.17%) |
| Cycles | 123 |
| Corpus | 15 |
| Zest branch coverage (total / valid) | 943 / 940 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 15 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 16 / 29 = 55.17% |
| Branch coverage | 10 / 24 = 41.67% |
| Test suite length (statements) | 15 |
| Mutation score | 18 / 23 = 78.26% |
| Fuzz time | 128s |
| Pipeline time | 241s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/34/t12001/JacksonDatabind-34f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_34/b120_r1/src/com/fasterxml/jackson/databind/ser/std/NumberSerializer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_34/b120_r1`
