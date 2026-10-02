# Zest – Closure-40 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 465538 (valid 73.49%) |
| Cycles | 41 |
| Corpus | 43 |
| Zest branch coverage (total / valid) | 260 / 257 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 43 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 100 / 711 = 14.06% |
| Branch coverage | 34 / 434 = 7.83% |
| Test suite length (statements) | 85 |
| Mutation score | 670 / 717 = 93.44% |
| Fuzz time | 122s |
| Pipeline time | 282s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/40/t12001/Closure-40f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_40/b120_r1/src/com/google/javascript/jscomp/NameAnalyzer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_40/b120_r1`
