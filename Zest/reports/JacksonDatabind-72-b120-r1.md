# Zest – JacksonDatabind-72 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 459628 (valid 40.17%) |
| Cycles | 162 |
| Corpus | 8 |
| Zest branch coverage (total / valid) | 108 / 38 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 8 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 2 / 48 = 4.17% |
| Branch coverage | 0 / 10 = 0.00% |
| Test suite length (statements) | 8 |
| Mutation score | 11 / 11 = 100.00% |
| Fuzz time | 126s |
| Pipeline time | 248s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/72/t12001/JacksonDatabind-72f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_72/b120_r1/src/com/fasterxml/jackson/databind/deser/impl/InnerClassProperty_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_72/b120_r1`
