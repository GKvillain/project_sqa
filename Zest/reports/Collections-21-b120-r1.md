# Zest – Collections-21 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 417063 (valid 68.25%) |
| Cycles | 27 |
| Corpus | 43 |
| Zest branch coverage (total / valid) | 200 / 193 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 43 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 50 / 109 = 45.87% |
| Branch coverage | 15 / 38 = 39.47% |
| Test suite length (statements) | 43 |
| Mutation score | 70 / 76 = 92.11% |
| Fuzz time | 126s |
| Pipeline time | 209s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Collections/zest/21/t12001/Collections-21f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_21/b120_r1/src/org/apache/commons/collections4/list/SetUniqueList_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_21/b120_r1`
