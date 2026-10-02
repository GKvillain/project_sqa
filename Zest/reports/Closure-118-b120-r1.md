# Zest – Closure-118 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 397164 (valid 67.00%) |
| Cycles | 42 |
| Corpus | 23 |
| Zest branch coverage (total / valid) | 199 / 193 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 23 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 51 / 464 = 10.99% |
| Branch coverage | 14 / 328 = 4.27% |
| Test suite length (statements) | 23 |
| Mutation score | 383 / 412 = 92.96% |
| Fuzz time | 120s |
| Pipeline time | 285s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/118/t12001/Closure-118f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_118/b120_r1/src/com/google/javascript/jscomp/DisambiguateProperties_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_118/b120_r1`
