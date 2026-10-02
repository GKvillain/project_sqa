# Zest – JacksonDatabind-85 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 365006 (valid 52.14%) |
| Cycles | 29 |
| Corpus | 27 |
| Zest branch coverage (total / valid) | 158 / 155 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 27 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 18 / 62 = 29.03% |
| Branch coverage | 9 / 54 = 16.67% |
| Test suite length (statements) | 27 |
| Mutation score | 58 / 71 = 81.69% |
| Fuzz time | 127s |
| Pipeline time | 238s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/85/t12001/JacksonDatabind-85f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_85/b120_r1/src/com/fasterxml/jackson/databind/ser/std/DateTimeSerializerBase_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_85/b120_r1`
