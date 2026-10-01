# Zest – Chart-21 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 25918 (valid 51.93%) |
| Cycles | 1 |
| Corpus | 45 |
| Zest branch coverage (total / valid) | 267 / 264 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 45 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 67 / 187 = 35.83% |
| Branch coverage | 9 / 82 = 10.98% |
| Test suite length (statements) | 45 |
| Mutation score | 105 / 165 = 63.64% |
| Fuzz time | 120s |
| Pipeline time | 275s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Chart/zest/21/t12001/Chart-21f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_21/b120_r1/src/org/jfree/data/statistics/DefaultBoxAndWhiskerCategoryDataset_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_21/b120_r1`
