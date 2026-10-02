# Zest – JacksonDatabind-63 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 426431 (valid 95.27%) |
| Cycles | 35 |
| Corpus | 41 |
| Zest branch coverage (total / valid) | 177 / 174 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 41 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 32 / 125 = 25.60% |
| Branch coverage | 5 / 46 = 10.87% |
| Test suite length (statements) | 41 |
| Mutation score | 74 / 77 = 96.10% |
| Fuzz time | 128s |
| Pipeline time | 224s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/63/t12001/JacksonDatabind-63f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_63/b120_r1/src/com/fasterxml/jackson/databind/JsonMappingException_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_63/b120_r1`
