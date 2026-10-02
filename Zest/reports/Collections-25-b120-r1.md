# Zest – Collections-25 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 443376 (valid 63.46%) |
| Cycles | 25 |
| Corpus | 46 |
| Zest branch coverage (total / valid) | 205 / 197 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 46 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 25 / 216 = 11.57% |
| Branch coverage | 4 / 132 = 3.03% |
| Test suite length (statements) | 87 |
| Mutation score | 95 / 99 = 95.96% |
| Fuzz time | 126s |
| Pipeline time | 203s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Collections/zest/25/t12001/Collections-25f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_25/b120_r1/src/org/apache/commons/collections4/IteratorUtils_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_25/b120_r1`
