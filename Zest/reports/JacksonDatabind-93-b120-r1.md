# Zest – JacksonDatabind-93 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 475489 (valid 100.00%) |
| Cycles | 126 |
| Corpus | 11 |
| Zest branch coverage (total / valid) | 62 / 62 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 11 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 24 / 36 = 66.67% |
| Branch coverage | 0 / 14 = 0.00% |
| Test suite length (statements) | 11 |
| Mutation score | 15 / 32 = 46.88% |
| Fuzz time | 125s |
| Pipeline time | 242s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/93/t12001/JacksonDatabind-93f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_93/b120_r1/src/com/fasterxml/jackson/databind/jsontype/impl/SubTypeValidator_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_93/b120_r1`
