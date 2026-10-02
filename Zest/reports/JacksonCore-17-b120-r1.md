# Zest – JacksonCore-17 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 526459 (valid 5.05%) |
| Cycles | 104 |
| Corpus | 10 |
| Zest branch coverage (total / valid) | 123 / 36 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 10 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 8 / 1019 = 0.79% |
| Branch coverage | 0 / 541 = 0.00% |
| Test suite length (statements) | 10 |
| Mutation score | 2215 / 2218 = 99.86% |
| Fuzz time | 125s |
| Pipeline time | 187s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonCore/zest/17/t12001/JacksonCore-17f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_17/b120_r1/src/com/fasterxml/jackson/core/json/UTF8JsonGenerator_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_17/b120_r1`
