# Zest – Chart-7 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 183407 (valid 75.95%) |
| Cycles | 19 |
| Corpus | 39 |
| Zest branch coverage (total / valid) | 219 / 212 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 39 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 56 / 159 = 35.22% |
| Branch coverage | 9 / 54 = 16.67% |
| Test suite length (statements) | 39 |
| Mutation score | 172 / 307 = 56.03% |
| Fuzz time | 120s |
| Pipeline time | 296s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Chart/zest/7/t12001/Chart-7f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_7/b120_r1/src/org/jfree/data/time/TimePeriodValues_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_7/b120_r1`
