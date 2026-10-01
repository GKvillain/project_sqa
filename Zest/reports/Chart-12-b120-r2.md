# Zest – Chart-12 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 103446 (valid 71.37%) |
| Cycles | 4 |
| Corpus | 51 |
| Zest branch coverage (total / valid) | 461 / 456 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 51 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 48 / 190 = 25.26% |
| Branch coverage | 9 / 84 = 10.71% |
| Test suite length (statements) | 51 |
| Mutation score | 220 / 238 = 92.44% |
| Fuzz time | 120s |
| Pipeline time | 270s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Chart/zest/12/t12002/Chart-12f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_12/b120_r2/src/org/jfree/chart/plot/MultiplePiePlot_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_12/b120_r2`
