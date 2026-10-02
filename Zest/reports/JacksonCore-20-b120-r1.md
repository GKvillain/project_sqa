# Zest – JacksonCore-20 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 424507 (valid 53.44%) |
| Cycles | 24 |
| Corpus | 33 |
| Zest branch coverage (total / valid) | 174 / 171 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 33 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 33 / 266 = 12.41% |
| Branch coverage | 9 / 101 = 8.91% |
| Test suite length (statements) | 33 |
| Mutation score | 185 / 207 = 89.37% |
| Fuzz time | 128s |
| Pipeline time | 187s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonCore/zest/20/t12001/JacksonCore-20f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_20/b120_r1/src/com/fasterxml/jackson/core/JsonGenerator_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_20/b120_r1`
