# Zest – JacksonDatabind-1 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 416850 (valid 100.00%) |
| Cycles | 196 |
| Corpus | 7 |
| Zest branch coverage (total / valid) | 98 / 98 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 7 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 2 / 196 = 1.02% |
| Branch coverage | 0 / 104 = 0.00% |
| Test suite length (statements) | 7 |
| Mutation score | 115 / 115 = 100.00% |
| Fuzz time | 129s |
| Pipeline time | 214s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/1/t12001/JacksonDatabind-1f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_1/b120_r1/src/com/fasterxml/jackson/databind/ser/BeanPropertyWriter_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_1/b120_r1`
