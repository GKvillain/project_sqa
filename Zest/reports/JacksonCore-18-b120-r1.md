# Zest – JacksonCore-18 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 663734 (valid 0.00%) |
| Cycles | 120 |
| Corpus | 9 |
| Zest branch coverage (total / valid) | 127 / 0 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 9 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 24 / 2113 = 1.14% |
| Branch coverage | 2 / 1082 = 0.18% |
| Test suite length (statements) | 9 |
| Mutation score | 4362 / 4376 = 99.68% |
| Fuzz time | 128s |
| Pipeline time | 187s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonCore/zest/18/t12001/JacksonCore-18f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_18/b120_r1/src/com/fasterxml/jackson/core/base/GeneratorBase_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_18/b120_r1`
