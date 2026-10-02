# Zest – Closure-112 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 527583 (valid 50.63%) |
| Cycles | 61 |
| Corpus | 19 |
| Zest branch coverage (total / valid) | 161 / 154 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 19 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 4 / 715 = 0.56% |
| Branch coverage | 2 / 465 = 0.43% |
| Test suite length (statements) | 19 |
| Mutation score | 627 / 627 = 100.00% |
| Fuzz time | 120s |
| Pipeline time | 282s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/112/t12001/Closure-112f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_112/b120_r1/src/com/google/javascript/jscomp/TypeInference_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_112/b120_r1`
