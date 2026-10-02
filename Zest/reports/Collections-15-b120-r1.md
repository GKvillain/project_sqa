# Zest – Collections-15 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 366492 (valid 80.10%) |
| Cycles | 25 |
| Corpus | 42 |
| Zest branch coverage (total / valid) | 207 / 199 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 42 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 60 / 86 = 69.77% |
| Branch coverage | 19 / 22 = 86.36% |
| Test suite length (statements) | 42 |
| Mutation score | 42 / 60 = 70.00% |
| Fuzz time | 126s |
| Pipeline time | 206s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Collections/zest/15/t12001/Collections-15f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_15/b120_r1/src/org/apache/commons/collections/list/SetUniqueList_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_15/b120_r1`
