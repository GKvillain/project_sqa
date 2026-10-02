# Zest – JacksonDatabind-33 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 402324 (valid 68.63%) |
| Cycles | 22 |
| Corpus | 45 |
| Zest branch coverage (total / valid) | 226 / 223 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 45 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 24 / 371 = 6.47% |
| Branch coverage | 12 / 282 = 4.26% |
| Test suite length (statements) | 45 |
| Mutation score | 256 / 267 = 95.88% |
| Fuzz time | 126s |
| Pipeline time | 236s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/33/t12001/JacksonDatabind-33f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_33/b120_r1/src/com/fasterxml/jackson/databind/introspect/JacksonAnnotationIntrospector_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_33/b120_r1`
