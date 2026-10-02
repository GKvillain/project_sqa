# Zest – Collections-28 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 420144 (valid 87.92%) |
| Cycles | 45 |
| Corpus | 61 |
| Zest branch coverage (total / valid) | 346 / 343 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 61 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 220 / 829 = 26.54% |
| Branch coverage | 85 / 546 = 15.57% |
| Test suite length (statements) | 61 |
| Mutation score | 769 / 916 = 83.95% |
| Fuzz time | 124s |
| Pipeline time | 221s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Collections/zest/28/t12001/Collections-28f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_28/b120_r1/src/org/apache/commons/collections4/trie/AbstractPatriciaTrie_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_28/b120_r1`
