# Zest – JacksonDatabind-42 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 459107 (valid 100.00%) |
| Cycles | 107 |
| Corpus | 14 |
| Zest branch coverage (total / valid) | 126 / 126 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 14 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 15 / 112 = 13.39% |
| Branch coverage | 12 / 81 = 14.81% |
| Test suite length (statements) | 21 |
| Mutation score | 135 / 147 = 91.84% |
| Fuzz time | 127s |
| Pipeline time | 228s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/42/t12001/JacksonDatabind-42f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_42/b120_r1/src/com/fasterxml/jackson/databind/deser/std/FromStringDeserializer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_42/b120_r1`
