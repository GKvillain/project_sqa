# Zest – JacksonDatabind-35 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 450335 (valid 8.53%) |
| Cycles | 111 |
| Corpus | 7 |
| Zest branch coverage (total / valid) | 109 / 36 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 7 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 2 / 36 = 5.56% |
| Branch coverage | 0 / 18 = 0.00% |
| Test suite length (statements) | 7 |
| Mutation score | 20 / 20 = 100.00% |
| Fuzz time | 128s |
| Pipeline time | 238s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/35/t12001/JacksonDatabind-35f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_35/b120_r1/src/com/fasterxml/jackson/databind/jsontype/impl/AsWrapperTypeDeserializer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_35/b120_r1`
