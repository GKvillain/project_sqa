# Zest – Closure-87 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 388908 (valid 64.29%) |
| Cycles | 23 |
| Corpus | 68 |
| Zest branch coverage (total / valid) | 191 / 188 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 68 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 44 / 464 = 9.48% |
| Branch coverage | 11 / 319 = 3.45% |
| Test suite length (statements) | 68 |
| Mutation score | 540 / 587 = 91.99% |
| Fuzz time | 120s |
| Pipeline time | 244s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/87/t12001/Closure-87f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_87/b120_r1/src/com/google/javascript/jscomp/PeepholeSubstituteAlternateSyntax_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_87/b120_r1`
