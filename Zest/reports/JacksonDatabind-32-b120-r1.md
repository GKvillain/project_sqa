# Zest – JacksonDatabind-32 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 418018 (valid 64.84%) |
| Cycles | 44 |
| Corpus | 25 |
| Zest branch coverage (total / valid) | 142 / 139 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 25 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 29 / 263 = 11.03% |
| Branch coverage | 6 / 146 = 4.11% |
| Test suite length (statements) | 25 |
| Mutation score | 174 / 198 = 87.88% |
| Fuzz time | 128s |
| Pipeline time | 225s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/32/t12001/JacksonDatabind-32f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_32/b120_r1/src/com/fasterxml/jackson/databind/deser/std/UntypedObjectDeserializer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_32/b120_r1`
