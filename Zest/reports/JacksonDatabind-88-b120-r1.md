# Zest – JacksonDatabind-88 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 370633 (valid 85.01%) |
| Cycles | 31 |
| Corpus | 28 |
| Zest branch coverage (total / valid) | 161 / 158 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 28 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 21 / 51 = 41.18% |
| Branch coverage | 12 / 28 = 42.86% |
| Test suite length (statements) | 28 |
| Mutation score | 26 / 41 = 63.41% |
| Fuzz time | 126s |
| Pipeline time | 234s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/88/t12001/JacksonDatabind-88f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_88/b120_r1/src/com/fasterxml/jackson/databind/jsontype/impl/ClassNameIdResolver_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_88/b120_r1`
