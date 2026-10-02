# Zest – JxPath-18 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 439041 (valid 100.00%) |
| Cycles | 68 |
| Corpus | 22 |
| Zest branch coverage (total / valid) | 156 / 156 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 22 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 26 / 35 = 74.29% |
| Branch coverage | 11 / 20 = 55.00% |
| Test suite length (statements) | 22 |
| Mutation score | 27 / 49 = 55.10% |
| Fuzz time | 120s |
| Pipeline time | 189s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JxPath/zest/18/t12001/JxPath-18f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_18/b120_r1/src/org/apache/commons/jxpath/ri/axes/AttributeContext_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_18/b120_r1`
