# Zest – JacksonCore-9 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 421114 (valid 55.14%) |
| Cycles | 49 |
| Corpus | 34 |
| Zest branch coverage (total / valid) | 163 / 160 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 34 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 16 / 3128 = 0.51% |
| Branch coverage | 5 / 2269 = 0.22% |
| Test suite length (statements) | 34 |
| Mutation score | 6616 / 6619 = 99.95% |
| Fuzz time | 129s |
| Pipeline time | 202s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonCore/zest/9/t12001/JacksonCore-9f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_9/b120_r1/src/com/fasterxml/jackson/core/base/ParserMinimalBase_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_9/b120_r1`
