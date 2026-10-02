# Zest – JacksonDatabind-26 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 417482 (valid 69.44%) |
| Cycles | 35 |
| Corpus | 28 |
| Zest branch coverage (total / valid) | 159 / 156 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 28 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 56 / 267 = 20.97% |
| Branch coverage | 14 / 148 = 9.46% |
| Test suite length (statements) | 28 |
| Mutation score | 143 / 178 = 80.34% |
| Fuzz time | 128s |
| Pipeline time | 251s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/26/t12001/JacksonDatabind-26f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_26/b120_r1/src/com/fasterxml/jackson/databind/ser/BeanPropertyWriter_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_26/b120_r1`
