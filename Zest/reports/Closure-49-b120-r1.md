# Zest – Closure-49 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 487638 (valid 62.56%) |
| Cycles | 62 |
| Corpus | 22 |
| Zest branch coverage (total / valid) | 171 / 168 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 21 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 26 / 196 = 13.27% |
| Branch coverage | 2 / 105 = 1.90% |
| Test suite length (statements) | 21 |
| Mutation score | 164 / 176 = 93.18% |
| Fuzz time | 122s |
| Pipeline time | 285s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/49/t12001/Closure-49f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_49/b120_r1/src/com/google/javascript/jscomp/MakeDeclaredNamesUnique_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_49/b120_r1`
