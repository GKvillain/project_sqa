# Zest – Closure-20 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 458575 (valid 76.24%) |
| Cycles | 21 |
| Corpus | 71 |
| Zest branch coverage (total / valid) | 193 / 190 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 71 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 38 / 755 = 5.03% |
| Branch coverage | 8 / 530 = 1.51% |
| Test suite length (statements) | 71 |
| Mutation score | 847 / 898 = 94.32% |
| Fuzz time | 123s |
| Pipeline time | 288s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/20/t12001/Closure-20f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_20/b120_r1/src/com/google/javascript/jscomp/PeepholeSubstituteAlternateSyntax_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_20/b120_r1`
