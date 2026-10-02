# Zest – JacksonCore-8 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 410698 (valid 80.60%) |
| Cycles | 23 |
| Corpus | 48 |
| Zest branch coverage (total / valid) | 232 / 219 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 48 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 110 / 271 = 40.59% |
| Branch coverage | 51 / 140 = 36.43% |
| Test suite length (statements) | 48 |
| Mutation score | 368 / 497 = 74.04% |
| Fuzz time | 128s |
| Pipeline time | 199s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonCore/zest/8/t12001/JacksonCore-8f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_8/b120_r1/src/com/fasterxml/jackson/core/util/TextBuffer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_8/b120_r1`
