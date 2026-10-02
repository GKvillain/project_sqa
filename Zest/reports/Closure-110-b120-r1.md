# Zest – Closure-110 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 510165 (valid 77.66%) |
| Cycles | 14 |
| Corpus | 158 |
| Zest branch coverage (total / valid) | 343 / 340 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 158 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 132 / 1137 = 11.61% |
| Branch coverage | 38 / 707 = 5.37% |
| Test suite length (statements) | 158 |
| Mutation score | 1266 / 1322 = 95.76% |
| Fuzz time | 121s |
| Pipeline time | 296s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/110/t12001/Closure-110f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_110/b120_r1/src/com/google/javascript/jscomp/ScopedAliases_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_110/b120_r1`
