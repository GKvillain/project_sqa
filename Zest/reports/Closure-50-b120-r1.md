# Zest – Closure-50 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 453249 (valid 59.45%) |
| Cycles | 48 |
| Corpus | 27 |
| Zest branch coverage (total / valid) | 155 / 152 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 27 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 18 / 312 = 5.77% |
| Branch coverage | 14 / 253 = 5.53% |
| Test suite length (statements) | 27 |
| Mutation score | 557 / 578 = 96.37% |
| Fuzz time | 122s |
| Pipeline time | 273s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/50/t12001/Closure-50f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_50/b120_r1/src/com/google/javascript/jscomp/PeepholeReplaceKnownMethods_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_50/b120_r1`
