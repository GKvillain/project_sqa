# Zest – Closure-132 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 563813 (valid 65.21%) |
| Cycles | 35 |
| Corpus | 79 |
| Zest branch coverage (total / valid) | 201 / 198 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 79 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 44 / 759 = 5.80% |
| Branch coverage | 10 / 536 = 1.87% |
| Test suite length (statements) | 79 |
| Mutation score | 859 / 910 = 94.40% |
| Fuzz time | 132s |
| Pipeline time | 296s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/132/t12001/Closure-132f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_132/b120_r1/src/com/google/javascript/jscomp/PeepholeSubstituteAlternateSyntax_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_132/b120_r1`
