# Zest – JacksonDatabind-48 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 517345 (valid 21.18%) |
| Cycles | 145 |
| Corpus | 7 |
| Zest branch coverage (total / valid) | 106 / 36 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 7 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 4 / 577 = 0.69% |
| Branch coverage | 0 / 262 = 0.00% |
| Test suite length (statements) | 7 |
| Mutation score | 523 / 523 = 100.00% |
| Fuzz time | 128s |
| Pipeline time | 239s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/48/t12001/JacksonDatabind-48f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_48/b120_r1/src/com/fasterxml/jackson/databind/DeserializationConfig_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_48/b120_r1`
