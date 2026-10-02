# Zest – JacksonDatabind-43 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 394698 (valid 74.28%) |
| Cycles | 71 |
| Corpus | 9 |
| Zest branch coverage (total / valid) | 108 / 101 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 9 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 3 / 32 = 9.38% |
| Branch coverage | 0 / 6 = 0.00% |
| Test suite length (statements) | 9 |
| Mutation score | 7 / 7 = 100.00% |
| Fuzz time | 128s |
| Pipeline time | 233s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/43/t12001/JacksonDatabind-43f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_43/b120_r1/src/com/fasterxml/jackson/databind/deser/impl/ObjectIdValueProperty_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_43/b120_r1`
