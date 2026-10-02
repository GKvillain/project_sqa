# Zest – Collections-14 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 346690 (valid 85.98%) |
| Cycles | 40 |
| Corpus | 41 |
| Zest branch coverage (total / valid) | 184 / 181 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 40 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 17 / 21 = 80.95% |
| Branch coverage | 4 / 4 = 100.00% |
| Test suite length (statements) | 40 |
| Mutation score | 10 / 17 = 58.82% |
| Fuzz time | 126s |
| Pipeline time | 206s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Collections/zest/14/t12001/Collections-14f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_14/b120_r1/src/org/apache/commons/collections/map/CaseInsensitiveMap_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_14/b120_r1`
