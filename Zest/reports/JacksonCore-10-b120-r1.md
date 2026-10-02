# Zest – JacksonCore-10 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 524490 (valid 100.00%) |
| Cycles | 134 |
| Corpus | 12 |
| Zest branch coverage (total / valid) | 82 / 82 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 12 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 82 / 467 = 17.56% |
| Branch coverage | 14 / 266 = 5.26% |
| Test suite length (statements) | 12 |
| Mutation score | 1525 / 1701 = 89.65% |
| Fuzz time | 130s |
| Pipeline time | 207s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonCore/zest/10/t12001/JacksonCore-10f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_10/b120_r1/src/com/fasterxml/jackson/core/sym/ByteQuadsCanonicalizer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_10/b120_r1`
