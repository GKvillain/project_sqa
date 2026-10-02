# Zest – JacksonCore-4 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 383895 (valid 74.43%) |
| Cycles | 22 |
| Corpus | 41 |
| Zest branch coverage (total / valid) | 231 / 218 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 41 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 117 / 264 = 44.32% |
| Branch coverage | 50 / 134 = 37.31% |
| Test suite length (statements) | 41 |
| Mutation score | 327 / 480 = 68.12% |
| Fuzz time | 129s |
| Pipeline time | 193s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonCore/zest/4/t12001/JacksonCore-4f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_4/b120_r1/src/com/fasterxml/jackson/core/util/TextBuffer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_4/b120_r1`
