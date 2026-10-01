# Zest – Chart-24 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 22834 (valid 92.16%) |
| Cycles | 3 |
| Corpus | 36 |
| Zest branch coverage (total / valid) | 155 / 152 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 36 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 18 / 25 = 72.00% |
| Branch coverage | 4 / 10 = 40.00% |
| Test suite length (statements) | 36 |
| Mutation score | 32 / 39 = 82.05% |
| Fuzz time | 120s |
| Pipeline time | 269s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Chart/zest/24/t12001/Chart-24f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_24/b120_r1/src/org/jfree/chart/renderer/GrayPaintScale_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_24/b120_r1`
