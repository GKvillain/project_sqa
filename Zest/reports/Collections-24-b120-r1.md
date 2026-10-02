# Zest – Collections-24 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 537840 (valid 0.00%) |
| Cycles | 98 |
| Corpus | 9 |
| Zest branch coverage (total / valid) | 124 / 0 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 9 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 11 / 29 = 37.93% |
| Branch coverage | 9 / 14 = 64.29% |
| Test suite length (statements) | 9 |
| Mutation score | 6 / 13 = 46.15% |
| Fuzz time | 126s |
| Pipeline time | 195s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Collections/zest/24/t12001/Collections-24f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_24/b120_r1/src/org/apache/commons/collections4/collection/UnmodifiableBoundedCollection_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_24/b120_r1`
