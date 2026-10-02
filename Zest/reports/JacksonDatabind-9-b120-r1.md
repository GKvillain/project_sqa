# Zest – JacksonDatabind-9 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 395708 (valid 66.75%) |
| Cycles | 76 |
| Corpus | 12 |
| Zest branch coverage (total / valid) | 126 / 123 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 12 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 6 / 15 = 40.00% |
| Branch coverage | 1 / 6 = 16.67% |
| Test suite length (statements) | 12 |
| Mutation score | 6 / 7 = 85.71% |
| Fuzz time | 129s |
| Pipeline time | 232s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/9/t12001/JacksonDatabind-9f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_9/b120_r1/src/com/fasterxml/jackson/databind/ser/std/StdKeySerializer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_9/b120_r1`
