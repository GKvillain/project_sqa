# Zest – Collections-22 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 387886 (valid 85.94%) |
| Cycles | 25 |
| Corpus | 52 |
| Zest branch coverage (total / valid) | 239 / 236 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 52 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 112 / 197 = 56.85% |
| Branch coverage | 27 / 52 = 51.92% |
| Test suite length (statements) | 52 |
| Mutation score | 121 / 160 = 75.62% |
| Fuzz time | 124s |
| Pipeline time | 205s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Collections/zest/22/t12001/Collections-22f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_22/b120_r1/src/org/apache/commons/collections4/map/ListOrderedMap_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_22/b120_r1`
