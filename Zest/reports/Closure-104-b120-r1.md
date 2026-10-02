# Zest – Closure-104 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 458348 (valid 63.12%) |
| Cycles | 26 |
| Corpus | 31 |
| Zest branch coverage (total / valid) | 168 / 165 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 30 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 36 / 192 = 18.75% |
| Branch coverage | 12 / 124 = 9.68% |
| Test suite length (statements) | 30 |
| Mutation score | 109 / 113 = 96.46% |
| Fuzz time | 120s |
| Pipeline time | 202s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/104/t12001/Closure-104f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_104/b120_r1/src/com/google/javascript/rhino/jstype/UnionType_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_104/b120_r1`
