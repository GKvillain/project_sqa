# Zest – JacksonCore-23 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 534427 (valid 75.15%) |
| Cycles | 57 |
| Corpus | 22 |
| Zest branch coverage (total / valid) | 147 / 144 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 22 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 67 / 108 = 62.04% |
| Branch coverage | 18 / 42 = 42.86% |
| Test suite length (statements) | 22 |
| Mutation score | 32 / 86 = 37.21% |
| Fuzz time | 126s |
| Pipeline time | 205s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonCore/zest/23/t12001/JacksonCore-23f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_23/b120_r1/src/com/fasterxml/jackson/core/util/DefaultPrettyPrinter_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_23/b120_r1`
