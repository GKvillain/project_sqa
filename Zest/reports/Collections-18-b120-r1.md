# Zest – Collections-18 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 388875 (valid 79.56%) |
| Cycles | 23 |
| Corpus | 52 |
| Zest branch coverage (total / valid) | 201 / 198 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 52 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 77 / 98 = 78.57% |
| Branch coverage | 28 / 40 = 70.00% |
| Test suite length (statements) | 52 |
| Mutation score | 50 / 67 = 74.63% |
| Fuzz time | 126s |
| Pipeline time | 218s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Collections/zest/18/t12001/Collections-18f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_18/b120_r1/src/org/apache/commons/collections/set/ListOrderedSet_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_18/b120_r1`
