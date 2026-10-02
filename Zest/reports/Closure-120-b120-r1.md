# Zest – Closure-120 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 392322 (valid 82.52%) |
| Cycles | 51 |
| Corpus | 22 |
| Zest branch coverage (total / valid) | 179 / 176 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 22 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 31 / 212 = 14.62% |
| Branch coverage | 3 / 151 = 1.99% |
| Test suite length (statements) | 22 |
| Mutation score | 253 / 257 = 98.44% |
| Fuzz time | 131s |
| Pipeline time | 290s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/120/t12001/Closure-120f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_120/b120_r1/src/com/google/javascript/jscomp/ReferenceCollectingCallback_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_120/b120_r1`
