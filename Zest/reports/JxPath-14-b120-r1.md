# Zest – JxPath-14 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 366882 (valid 44.29%) |
| Cycles | 4 |
| Corpus | 276 |
| Zest branch coverage (total / valid) | 377 / 374 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 276 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 177 / 361 = 49.03% |
| Branch coverage | 107 / 224 = 47.77% |
| Test suite length (statements) | 276 |
| Mutation score | 414 / 459 = 90.20% |
| Fuzz time | 120s |
| Pipeline time | 215s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JxPath/zest/14/t12001/JxPath-14f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_14/b120_r1/src/org/apache/commons/jxpath/ri/compiler/CoreFunction_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_14/b120_r1`
