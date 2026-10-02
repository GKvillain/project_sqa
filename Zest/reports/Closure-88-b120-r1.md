# Zest – Closure-88 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 396602 (valid 67.69%) |
| Cycles | 46 |
| Corpus | 23 |
| Zest branch coverage (total / valid) | 142 / 139 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 23 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 13 / 137 = 9.49% |
| Branch coverage | 1 / 104 = 0.96% |
| Test suite length (statements) | 23 |
| Mutation score | 163 / 168 = 97.02% |
| Fuzz time | 120s |
| Pipeline time | 237s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/88/t12001/Closure-88f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_88/b120_r1/src/com/google/javascript/jscomp/DeadAssignmentsElimination_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_88/b120_r1`
