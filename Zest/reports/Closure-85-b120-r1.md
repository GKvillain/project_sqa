# Zest – Closure-85 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 342347 (valid 78.15%) |
| Cycles | 55 |
| Corpus | 16 |
| Zest branch coverage (total / valid) | 158 / 155 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 16 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 16 / 74 = 21.62% |
| Branch coverage | 2 / 55 = 3.64% |
| Test suite length (statements) | 16 |
| Mutation score | 98 / 98 = 100.00% |
| Fuzz time | 120s |
| Pipeline time | 222s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/85/t12001/Closure-85f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_85/b120_r1/src/com/google/javascript/jscomp/UnreachableCodeElimination_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_85/b120_r1`
