# Zest – JacksonDatabind-49 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 270735 (valid 72.89%) |
| Cycles | 40 |
| Corpus | 12 |
| Zest branch coverage (total / valid) | 135 / 132 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 12 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 10 / 22 = 45.45% |
| Branch coverage | 2 / 14 = 14.29% |
| Test suite length (statements) | 12 |
| Mutation score | 19 / 26 = 73.08% |
| Fuzz time | 128s |
| Pipeline time | 234s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/49/t12001/JacksonDatabind-49f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_49/b120_r1/src/com/fasterxml/jackson/databind/ser/impl/WritableObjectId_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_49/b120_r1`
