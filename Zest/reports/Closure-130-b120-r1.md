# Zest – Closure-130 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 629712 (valid 47.40%) |
| Cycles | 40 |
| Corpus | 32 |
| Zest branch coverage (total / valid) | 181 / 178 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 32 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 33 / 371 = 8.89% |
| Branch coverage | 9 / 266 = 3.38% |
| Test suite length (statements) | 32 |
| Mutation score | 456 / 476 = 95.80% |
| Fuzz time | 132s |
| Pipeline time | 287s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/130/t12001/Closure-130f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_130/b120_r1/src/com/google/javascript/jscomp/CollapseProperties_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_130/b120_r1`
