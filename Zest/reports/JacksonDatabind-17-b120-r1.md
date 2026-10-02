# Zest – JacksonDatabind-17 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 370673 (valid 52.61%) |
| Cycles | 39 |
| Corpus | 28 |
| Zest branch coverage (total / valid) | 611 / 608 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 27 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 59 / 638 = 9.25% |
| Branch coverage | 10 / 192 = 5.21% |
| Test suite length (statements) | 27 |
| Mutation score | 315 / 339 = 92.92% |
| Fuzz time | 128s |
| Pipeline time | 223s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/17/t12001/JacksonDatabind-17f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_17/b120_r1/src/com/fasterxml/jackson/databind/ObjectMapper_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_17/b120_r1`
