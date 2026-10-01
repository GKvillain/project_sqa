# Zest – Chart-26 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 118260 (valid 80.08%) |
| Cycles | 10 |
| Corpus | 46 |
| Zest branch coverage (total / valid) | 211 / 208 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 46 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 65 / 351 = 18.52% |
| Branch coverage | 8 / 130 = 6.15% |
| Test suite length (statements) | 46 |
| Mutation score | 478 / 531 = 90.02% |
| Fuzz time | 121s |
| Pipeline time | 259s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Chart/zest/26/t12002/Chart-26f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_26/b120_r2/src/org/jfree/chart/axis/Axis_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_26/b120_r2`
