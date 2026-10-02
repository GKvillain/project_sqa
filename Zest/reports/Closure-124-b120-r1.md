# Zest – Closure-124 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 629566 (valid 47.01%) |
| Cycles | 71 |
| Corpus | 16 |
| Zest branch coverage (total / valid) | 130 / 127 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 16 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 7 / 81 = 8.64% |
| Branch coverage | 0 / 68 = 0.00% |
| Test suite length (statements) | 16 |
| Mutation score | 93 / 94 = 98.94% |
| Fuzz time | 131s |
| Pipeline time | 281s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/124/t12001/Closure-124f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_124/b120_r1/src/com/google/javascript/jscomp/ExploitAssigns_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_124/b120_r1`
