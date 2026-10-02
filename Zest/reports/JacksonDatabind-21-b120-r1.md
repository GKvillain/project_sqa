# Zest – JacksonDatabind-21 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 416153 (valid 65.95%) |
| Cycles | 28 |
| Corpus | 42 |
| Zest branch coverage (total / valid) | 225 / 222 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 42 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 22 / 342 = 6.43% |
| Branch coverage | 12 / 255 = 4.71% |
| Test suite length (statements) | 42 |
| Mutation score | 230 / 240 = 95.83% |
| Fuzz time | 128s |
| Pipeline time | 234s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/21/t12001/JacksonDatabind-21f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_21/b120_r1/src/com/fasterxml/jackson/databind/introspect/JacksonAnnotationIntrospector_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_21/b120_r1`
