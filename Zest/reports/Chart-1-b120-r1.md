# Zest – Chart-1 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 149712 (valid 73.24%) |
| Cycles | 9 |
| Corpus | 56 |
| Zest branch coverage (total / valid) | 385 / 382 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 56 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 77 / 574 = 13.41% |
| Branch coverage | 23 / 242 = 9.50% |
| Test suite length (statements) | 56 |
| Mutation score | 407 / 443 = 91.87% |
| Fuzz time | 120s |
| Pipeline time | 315s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Chart/zest/1/t12001/Chart-1f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_1/b120_r1/src/org/jfree/chart/renderer/category/AbstractCategoryItemRenderer_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_1/b120_r1`
