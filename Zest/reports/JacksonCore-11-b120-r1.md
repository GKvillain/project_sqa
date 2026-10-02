# Zest – JacksonCore-11 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 408507 (valid 100.00%) |
| Cycles | 102 |
| Corpus | 12 |
| Zest branch coverage (total / valid) | 82 / 82 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 12 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 83 / 469 = 17.70% |
| Branch coverage | 15 / 268 = 5.60% |
| Test suite length (statements) | 12 |
| Mutation score | 1545 / 1710 = 90.35% |
| Fuzz time | 128s |
| Pipeline time | 195s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonCore/zest/11/t12001/JacksonCore-11f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_11/b120_r1/src/com/fasterxml/jackson/core/sym/ByteQuadsCanonicalizer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_11/b120_r1`
