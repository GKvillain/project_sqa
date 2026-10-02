# Zest – JacksonDatabind-47 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 432917 (valid 83.89%) |
| Cycles | 81 |
| Corpus | 21 |
| Zest branch coverage (total / valid) | 156 / 153 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 21 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 14 / 186 = 7.53% |
| Branch coverage | 0 / 48 = 0.00% |
| Test suite length (statements) | 21 |
| Mutation score | 67 / 67 = 100.00% |
| Fuzz time | 128s |
| Pipeline time | 234s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/47/t12001/JacksonDatabind-47f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_47/b120_r1/src/com/fasterxml/jackson/databind/AnnotationIntrospector_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_47/b120_r1`
