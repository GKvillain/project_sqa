# Zest – Chart-16 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 147005 (valid 56.97%) |
| Cycles | 5 |
| Corpus | 89 |
| Zest branch coverage (total / valid) | 307 / 302 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 89 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 94 / 188 = 50.00% |
| Branch coverage | 47 / 128 = 36.72% |
| Test suite length (statements) | 226 |
| Mutation score | 266 / 306 = 86.93% |
| Fuzz time | 121s |
| Pipeline time | 296s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Chart/zest/16/t12001/Chart-16f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_16/b120_r1/src/org/jfree/data/category/DefaultIntervalCategoryDataset_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_16/b120_r1`
