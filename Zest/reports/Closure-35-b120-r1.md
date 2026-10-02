# Zest – Closure-35 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 386906 (valid 66.23%) |
| Cycles | 51 |
| Corpus | 22 |
| Zest branch coverage (total / valid) | 161 / 154 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 22 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 8 / 618 = 1.29% |
| Branch coverage | 2 / 412 = 0.49% |
| Test suite length (statements) | 22 |
| Mutation score | 577 / 577 = 100.00% |
| Fuzz time | 124s |
| Pipeline time | 280s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/35/t12001/Closure-35f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_35/b120_r1/src/com/google/javascript/jscomp/TypeInference_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_35/b120_r1`
