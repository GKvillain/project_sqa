# Zest – Collections-10 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 335669 (valid 90.77%) |
| Cycles | 22 |
| Corpus | 47 |
| Zest branch coverage (total / valid) | 212 / 209 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 47 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 82 / 119 = 68.91% |
| Branch coverage | 31 / 48 = 64.58% |
| Test suite length (statements) | 47 |
| Mutation score | 62 / 78 = 79.49% |
| Fuzz time | 126s |
| Pipeline time | 200s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Collections/zest/10/t12001/Collections-10f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_10/b120_r1/src/org/apache/commons/collections/map/MultiValueMap_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_10/b120_r1`
