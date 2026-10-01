# Zest – Chart-25 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 112791 (valid 70.74%) |
| Cycles | 9 |
| Corpus | 38 |
| Zest branch coverage (total / valid) | 241 / 238 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 38 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 21 / 182 = 11.54% |
| Branch coverage | 3 / 90 = 3.33% |
| Test suite length (statements) | 38 |
| Mutation score | 435 / 439 = 99.09% |
| Fuzz time | 120s |
| Pipeline time | 277s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Chart/zest/25/t12001/Chart-25f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_25/b120_r1/src/org/jfree/chart/renderer/category/StatisticalBarRenderer_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_25/b120_r1`
