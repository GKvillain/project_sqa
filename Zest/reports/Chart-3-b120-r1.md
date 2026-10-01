# Zest – Chart-3 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 131212 (valid 48.47%) |
| Cycles | 7 |
| Corpus | 45 |
| Zest branch coverage (total / valid) | 238 / 231 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 44 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 60 / 382 = 15.71% |
| Branch coverage | 18 / 186 = 9.68% |
| Test suite length (statements) | 44 |
| Mutation score | 527 / 623 = 84.59% |
| Fuzz time | 120s |
| Pipeline time | 344s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Chart/zest/3/t12001/Chart-3f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_3/b120_r1/src/org/jfree/data/time/TimeSeries_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_3/b120_r1`
