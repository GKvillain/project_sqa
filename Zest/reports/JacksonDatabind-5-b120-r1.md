# Zest – JacksonDatabind-5 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 484740 (valid 100.00%) |
| Cycles | 389 |
| Corpus | 6 |
| Zest branch coverage (total / valid) | 36 / 36 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 6 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 40 / 368 = 10.87% |
| Branch coverage | 15 / 274 = 5.47% |
| Test suite length (statements) | 6 |
| Mutation score | 358 / 384 = 93.23% |
| Fuzz time | 126s |
| Pipeline time | 219s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/5/t12001/JacksonDatabind-5f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_5/b120_r1/src/com/fasterxml/jackson/databind/introspect/AnnotatedClass_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_5/b120_r1`
