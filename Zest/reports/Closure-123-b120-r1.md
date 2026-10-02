# Zest – Closure-123 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 614174 (valid 68.03%) |
| Cycles | 22 |
| Corpus | 111 |
| Zest branch coverage (total / valid) | 215 / 211 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 111 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 98 / 650 = 15.08% |
| Branch coverage | 48 / 485 = 9.90% |
| Test suite length (statements) | 111 |
| Mutation score | 1044 / 1157 = 90.23% |
| Fuzz time | 128s |
| Pipeline time | 295s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/123/t12001/Closure-123f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_123/b120_r1/src/com/google/javascript/jscomp/CodeGenerator_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_123/b120_r1`
