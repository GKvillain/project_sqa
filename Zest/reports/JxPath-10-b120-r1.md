# Zest – JxPath-10 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 347429 (valid 90.90%) |
| Cycles | 26 |
| Corpus | 38 |
| Zest branch coverage (total / valid) | 198 / 195 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 38 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 35 / 43 = 81.40% |
| Branch coverage | 25 / 36 = 69.44% |
| Test suite length (statements) | 38 |
| Mutation score | 36 / 46 = 78.26% |
| Fuzz time | 120s |
| Pipeline time | 198s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JxPath/zest/10/t12001/JxPath-10f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_10/b120_r1/src/org/apache/commons/jxpath/ri/compiler/CoreOperationRelationalExpression_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_10/b120_r1`
