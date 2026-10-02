# Zest – JacksonDatabind-18 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 472948 (valid 77.92%) |
| Cycles | 49 |
| Corpus | 30 |
| Zest branch coverage (total / valid) | 162 / 159 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 30 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 32 / 93 = 34.41% |
| Branch coverage | 8 / 55 = 14.55% |
| Test suite length (statements) | 30 |
| Mutation score | 61 / 67 = 91.04% |
| Fuzz time | 128s |
| Pipeline time | 224s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/18/t12001/JacksonDatabind-18f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_18/b120_r1/src/com/fasterxml/jackson/databind/MappingIterator_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_18/b120_r1`
