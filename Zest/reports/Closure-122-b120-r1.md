# Zest – Closure-122 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 458228 (valid 51.82%) |
| Cycles | 63 |
| Corpus | 17 |
| Zest branch coverage (total / valid) | 152 / 149 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 17 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 29 / 692 = 4.19% |
| Branch coverage | 3 / 356 = 0.84% |
| Test suite length (statements) | 17 |
| Mutation score | 516 / 519 = 99.42% |
| Fuzz time | 132s |
| Pipeline time | 293s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/122/t12001/Closure-122f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_122/b120_r1/src/com/google/javascript/jscomp/parsing/IRFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_122/b120_r1`
