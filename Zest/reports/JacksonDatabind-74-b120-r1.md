# Zest – JacksonDatabind-74 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 451106 (valid 60.31%) |
| Cycles | 69 |
| Corpus | 14 |
| Zest branch coverage (total / valid) | 148 / 145 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 14 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 6 / 65 = 9.23% |
| Branch coverage | 0 / 38 = 0.00% |
| Test suite length (statements) | 38 |
| Mutation score | 40 / 40 = 100.00% |
| Fuzz time | 125s |
| Pipeline time | 229s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/74/t12001/JacksonDatabind-74f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_74/b120_r1/src/com/fasterxml/jackson/databind/jsontype/impl/AsPropertyTypeDeserializer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_74/b120_r1`
