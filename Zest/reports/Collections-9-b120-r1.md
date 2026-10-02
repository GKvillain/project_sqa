# Zest – Collections-9 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 438100 (valid 76.84%) |
| Cycles | 33 |
| Corpus | 89 |
| Zest branch coverage (total / valid) | 257 / 254 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 89 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 118 / 491 = 24.03% |
| Branch coverage | 55 / 280 = 19.64% |
| Test suite length (statements) | 89 |
| Mutation score | 388 / 448 = 86.61% |
| Fuzz time | 126s |
| Pipeline time | 202s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Collections/zest/9/t12001/Collections-9f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_9/b120_r1/src/org/apache/commons/collections/ExtendedProperties_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_9/b120_r1`
