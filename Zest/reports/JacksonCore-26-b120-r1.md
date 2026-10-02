# Zest – JacksonCore-26 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 429946 (valid 40.44%) |
| Cycles | 149 |
| Corpus | 9 |
| Zest branch coverage (total / valid) | 111 / 38 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 9 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 10 / 1483 = 0.67% |
| Branch coverage | 0 / 951 = 0.00% |
| Test suite length (statements) | 9 |
| Mutation score | 3310 / 3310 = 100.00% |
| Fuzz time | 128s |
| Pipeline time | 204s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonCore/zest/26/t12001/JacksonCore-26f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_26/b120_r1/src/com/fasterxml/jackson/core/json/async/NonBlockingJsonParser_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_26/b120_r1`
