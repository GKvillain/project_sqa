# Zest – JacksonDatabind-83 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 382257 (valid 100.00%) |
| Cycles | 123 |
| Corpus | 11 |
| Zest branch coverage (total / valid) | 126 / 126 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 11 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 16 / 121 = 13.22% |
| Branch coverage | 13 / 86 = 15.12% |
| Test suite length (statements) | 15 |
| Mutation score | 151 / 164 = 92.07% |
| Fuzz time | 126s |
| Pipeline time | 238s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/83/t12001/JacksonDatabind-83f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_83/b120_r1/src/com/fasterxml/jackson/databind/deser/std/FromStringDeserializer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_83/b120_r1`
