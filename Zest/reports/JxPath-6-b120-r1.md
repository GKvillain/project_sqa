# Zest – JxPath-6 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 349115 (valid 86.38%) |
| Cycles | 49 |
| Corpus | 19 |
| Zest branch coverage (total / valid) | 171 / 168 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 19 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 26 / 53 = 49.06% |
| Branch coverage | 20 / 66 = 30.30% |
| Test suite length (statements) | 19 |
| Mutation score | 46 / 61 = 75.41% |
| Fuzz time | 120s |
| Pipeline time | 193s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JxPath/zest/6/t12001/JxPath-6f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_6/b120_r1/src/org/apache/commons/jxpath/ri/compiler/CoreOperationCompare_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_6/b120_r1`
