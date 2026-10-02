# Zest – Closure-42 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 396706 (valid 51.22%) |
| Cycles | 46 |
| Corpus | 18 |
| Zest branch coverage (total / valid) | 160 / 157 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 18 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 28 / 603 = 4.64% |
| Branch coverage | 3 / 281 = 1.07% |
| Test suite length (statements) | 18 |
| Mutation score | 408 / 412 = 99.03% |
| Fuzz time | 123s |
| Pipeline time | 280s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/42/t12001/Closure-42f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_42/b120_r1/src/com/google/javascript/jscomp/parsing/IRFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_42/b120_r1`
