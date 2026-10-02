# Zest – Closure-76 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 372884 (valid 51.96%) |
| Cycles | 36 |
| Corpus | 24 |
| Zest branch coverage (total / valid) | 143 / 140 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 24 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 15 / 173 = 8.67% |
| Branch coverage | 1 / 132 = 0.76% |
| Test suite length (statements) | 24 |
| Mutation score | 192 / 194 = 98.97% |
| Fuzz time | 120s |
| Pipeline time | 245s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/76/t12001/Closure-76f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_76/b120_r1/src/com/google/javascript/jscomp/DeadAssignmentsElimination_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_76/b120_r1`
