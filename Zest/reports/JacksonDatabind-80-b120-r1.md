# Zest – JacksonDatabind-80 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 391287 (valid 63.45%) |
| Cycles | 42 |
| Corpus | 34 |
| Zest branch coverage (total / valid) | 190 / 185 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 34 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 18 / 126 = 14.29% |
| Branch coverage | 9 / 82 = 10.98% |
| Test suite length (statements) | 63 |
| Mutation score | 70 / 78 = 89.74% |
| Fuzz time | 127s |
| Pipeline time | 246s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/80/t12001/JacksonDatabind-80f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_80/b120_r1/src/com/fasterxml/jackson/databind/jsontype/impl/StdSubtypeResolver_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_80/b120_r1`
