# Zest – Closure-128 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 427583 (valid 69.61%) |
| Cycles | 24 |
| Corpus | 71 |
| Zest branch coverage (total / valid) | 190 / 186 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 71 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 62 / 640 = 9.69% |
| Branch coverage | 26 / 479 = 5.43% |
| Test suite length (statements) | 71 |
| Mutation score | 1086 / 1144 = 94.93% |
| Fuzz time | 132s |
| Pipeline time | 291s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/128/t12001/Closure-128f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_128/b120_r1/src/com/google/javascript/jscomp/CodeGenerator_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_128/b120_r1`
