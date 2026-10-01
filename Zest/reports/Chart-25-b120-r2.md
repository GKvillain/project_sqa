# Zest – Chart-25 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 105092 (valid 75.29%) |
| Cycles | 10 |
| Corpus | 42 |
| Zest branch coverage (total / valid) | 241 / 238 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 42 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 20 / 182 = 10.99% |
| Branch coverage | 3 / 90 = 3.33% |
| Test suite length (statements) | 42 |
| Mutation score | 435 / 439 = 99.09% |
| Fuzz time | 121s |
| Pipeline time | 259s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Chart/zest/25/t12002/Chart-25f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_25/b120_r2/src/org/jfree/chart/renderer/category/StatisticalBarRenderer_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_25/b120_r2`
