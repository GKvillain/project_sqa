# Zest – JacksonDatabind-44 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 472042 (valid 58.23%) |
| Cycles | 63 |
| Corpus | 19 |
| Zest branch coverage (total / valid) | 138 / 135 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 19 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 28 / 91 = 30.77% |
| Branch coverage | 8 / 50 = 16.00% |
| Test suite length (statements) | 20 |
| Mutation score | 65 / 79 = 82.28% |
| Fuzz time | 127s |
| Pipeline time | 222s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/44/t12001/JacksonDatabind-44f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_44/b120_r1/src/com/fasterxml/jackson/databind/type/SimpleType_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_44/b120_r1`
