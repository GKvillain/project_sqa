# Zest – Collections-1 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 401936 (valid 98.23%) |
| Cycles | 31 |
| Corpus | 76 |
| Zest branch coverage (total / valid) | 350 / 347 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 76 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 151 / 495 = 30.51% |
| Branch coverage | 93 / 376 = 24.73% |
| Test suite length (statements) | 76 |
| Mutation score | 548 / 624 = 87.82% |
| Fuzz time | 127s |
| Pipeline time | 199s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Collections/zest/1/t12001/Collections-1f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_1/b120_r1/src/org/apache/commons/collections/map/Flat3Map_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_1/b120_r1`
