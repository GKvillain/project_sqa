# Zest – Collections-19 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 398432 (valid 71.77%) |
| Cycles | 23 |
| Corpus | 48 |
| Zest branch coverage (total / valid) | 206 / 199 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 48 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 61 / 111 = 54.95% |
| Branch coverage | 21 / 40 = 52.50% |
| Test suite length (statements) | 48 |
| Mutation score | 63 / 82 = 76.83% |
| Fuzz time | 125s |
| Pipeline time | 212s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Collections/zest/19/t12001/Collections-19f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_19/b120_r1/src/org/apache/commons/collections/list/SetUniqueList_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_19/b120_r1`
