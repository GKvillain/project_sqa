# Zest – Closure-129 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 471608 (valid 76.04%) |
| Cycles | 63 |
| Corpus | 19 |
| Zest branch coverage (total / valid) | 139 / 136 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 19 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 14 / 74 = 18.92% |
| Branch coverage | 4 / 53 = 7.55% |
| Test suite length (statements) | 19 |
| Mutation score | 69 / 78 = 88.46% |
| Fuzz time | 132s |
| Pipeline time | 288s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/129/t12001/Closure-129f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_129/b120_r1/src/com/google/javascript/jscomp/PrepareAst_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_129/b120_r1`
