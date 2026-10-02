# Zest – JacksonDatabind-99 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 342797 (valid 63.08%) |
| Cycles | 53 |
| Corpus | 21 |
| Zest branch coverage (total / valid) | 163 / 160 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 21 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 6 / 71 = 8.45% |
| Branch coverage | 1 / 28 = 3.57% |
| Test suite length (statements) | 21 |
| Mutation score | 32 / 34 = 94.12% |
| Fuzz time | 126s |
| Pipeline time | 243s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/99/t12001/JacksonDatabind-99f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_99/b120_r1/src/com/fasterxml/jackson/databind/type/ReferenceType_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_99/b120_r1`
