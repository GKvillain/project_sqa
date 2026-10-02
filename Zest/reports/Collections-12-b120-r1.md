# Zest – Collections-12 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 452068 (valid 76.43%) |
| Cycles | 29 |
| Corpus | 94 |
| Zest branch coverage (total / valid) | 257 / 254 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 94 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 115 / 490 = 23.47% |
| Branch coverage | 55 / 278 = 19.78% |
| Test suite length (statements) | 94 |
| Mutation score | 392 / 447 = 87.70% |
| Fuzz time | 125s |
| Pipeline time | 205s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Collections/zest/12/t12001/Collections-12f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_12/b120_r1/src/org/apache/commons/collections/ExtendedProperties_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_12/b120_r1`
