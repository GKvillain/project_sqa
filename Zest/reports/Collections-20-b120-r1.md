# Zest – Collections-20 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 435627 (valid 63.38%) |
| Cycles | 46 |
| Corpus | 45 |
| Zest branch coverage (total / valid) | 248 / 245 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 45 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 120 / 292 = 41.10% |
| Branch coverage | 47 / 154 = 30.52% |
| Test suite length (statements) | 51 |
| Mutation score | 359 / 458 = 78.38% |
| Fuzz time | 127s |
| Pipeline time | 216s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Collections/zest/20/t12001/Collections-20f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_20/b120_r1/src/org/apache/commons/collections/list/TreeList_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_20/b120_r1`
