# Zest – JacksonCore-6 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 434187 (valid 91.31%) |
| Cycles | 36 |
| Corpus | 80 |
| Zest branch coverage (total / valid) | 204 / 201 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 80 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 73 / 89 = 82.02% |
| Branch coverage | 52 / 70 = 74.29% |
| Test suite length (statements) | 80 |
| Mutation score | 92 / 181 = 50.83% |
| Fuzz time | 129s |
| Pipeline time | 193s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonCore/zest/6/t12001/JacksonCore-6f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_6/b120_r1/src/com/fasterxml/jackson/core/JsonPointer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_6/b120_r1`
