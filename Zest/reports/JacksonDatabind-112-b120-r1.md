# Zest – JacksonDatabind-112 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 329664 (valid 71.43%) |
| Cycles | 34 |
| Corpus | 25 |
| Zest branch coverage (total / valid) | 181 / 179 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 25 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 24 / 91 = 26.37% |
| Branch coverage | 11 / 60 = 18.33% |
| Test suite length (statements) | 25 |
| Mutation score | 38 / 71 = 53.52% |
| Fuzz time | 125s |
| Pipeline time | 250s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/112/t12001/JacksonDatabind-112f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_112/b120_r1/src/com/fasterxml/jackson/databind/deser/std/StringCollectionDeserializer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_112/b120_r1`
