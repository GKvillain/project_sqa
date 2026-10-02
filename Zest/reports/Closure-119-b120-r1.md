# Zest – Closure-119 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 416020 (valid 49.81%) |
| Cycles | 27 |
| Corpus | 30 |
| Zest branch coverage (total / valid) | 166 / 163 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 30 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 34 / 496 = 6.85% |
| Branch coverage | 5 / 346 = 1.45% |
| Test suite length (statements) | 30 |
| Mutation score | 568 / 578 = 98.27% |
| Fuzz time | 120s |
| Pipeline time | 277s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/119/t12001/Closure-119f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_119/b120_r1/src/com/google/javascript/jscomp/GlobalNamespace_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_119/b120_r1`
