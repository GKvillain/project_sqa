# Zest – Closure-98 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 430050 (valid 65.58%) |
| Cycles | 51 |
| Corpus | 18 |
| Zest branch coverage (total / valid) | 158 / 155 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 18 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 27 / 181 = 14.92% |
| Branch coverage | 3 / 143 = 2.10% |
| Test suite length (statements) | 18 |
| Mutation score | 258 / 265 = 97.36% |
| Fuzz time | 120s |
| Pipeline time | 202s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/98/t12001/Closure-98f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_98/b120_r1/src/com/google/javascript/jscomp/ReferenceCollectingCallback_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_98/b120_r1`
