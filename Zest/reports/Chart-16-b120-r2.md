# Zest – Chart-16 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 119690 (valid 59.95%) |
| Cycles | 5 |
| Corpus | 95 |
| Zest branch coverage (total / valid) | 308 / 304 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 95 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 97 / 188 = 51.60% |
| Branch coverage | 48 / 128 = 37.50% |
| Test suite length (statements) | 288 |
| Mutation score | 268 / 306 = 87.58% |
| Fuzz time | 120s |
| Pipeline time | 296s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Chart/zest/16/t12002/Chart-16f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_16/b120_r2/src/org/jfree/data/category/DefaultIntervalCategoryDataset_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_16/b120_r2`
