# Zest – Collections-6 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 387534 (valid 98.40%) |
| Cycles | 31 |
| Corpus | 80 |
| Zest branch coverage (total / valid) | 367 / 364 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 80 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 169 / 495 = 34.14% |
| Branch coverage | 105 / 376 = 27.93% |
| Test suite length (statements) | 80 |
| Mutation score | 530 / 624 = 84.94% |
| Fuzz time | 127s |
| Pipeline time | 203s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Collections/zest/6/t12001/Collections-6f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_6/b120_r1/src/org/apache/commons/collections/map/Flat3Map_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_6/b120_r1`
