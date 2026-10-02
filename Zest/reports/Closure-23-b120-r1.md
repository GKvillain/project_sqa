# Zest – Closure-23 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 416136 (valid 63.02%) |
| Cycles | 27 |
| Corpus | 37 |
| Zest branch coverage (total / valid) | 162 / 159 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 37 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 29 / 698 = 4.15% |
| Branch coverage | 10 / 545 = 1.83% |
| Test suite length (statements) | 37 |
| Mutation score | 741 / 757 = 97.89% |
| Fuzz time | 123s |
| Pipeline time | 289s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/23/t12001/Closure-23f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_23/b120_r1/src/com/google/javascript/jscomp/PeepholeFoldConstants_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_23/b120_r1`
