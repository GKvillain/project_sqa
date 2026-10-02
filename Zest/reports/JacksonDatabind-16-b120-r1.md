# Zest – JacksonDatabind-16 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 433983 (valid 87.82%) |
| Cycles | 53 |
| Corpus | 24 |
| Zest branch coverage (total / valid) | 160 / 157 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 24 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 20 / 35 = 57.14% |
| Branch coverage | 8 / 36 = 22.22% |
| Test suite length (statements) | 24 |
| Mutation score | 39 / 52 = 75.00% |
| Fuzz time | 127s |
| Pipeline time | 220s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/16/t12001/JacksonDatabind-16f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_16/b120_r1/src/com/fasterxml/jackson/databind/introspect/AnnotationMap_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_16/b120_r1`
