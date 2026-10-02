# Zest – Closure-114 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 388277 (valid 77.10%) |
| Cycles | 30 |
| Corpus | 44 |
| Zest branch coverage (total / valid) | 262 / 259 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 44 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 98 / 752 = 13.03% |
| Branch coverage | 34 / 469 = 7.25% |
| Test suite length (statements) | 163 |
| Mutation score | 729 / 766 = 95.17% |
| Fuzz time | 120s |
| Pipeline time | 282s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/114/t12001/Closure-114f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_114/b120_r1/src/com/google/javascript/jscomp/NameAnalyzer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_114/b120_r1`
