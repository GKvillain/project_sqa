# Zest – JxPath-15 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 321900 (valid 81.85%) |
| Cycles | 50 |
| Corpus | 20 |
| Zest branch coverage (total / valid) | 190 / 187 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 20 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 14 / 20 = 70.00% |
| Branch coverage | 7 / 14 = 50.00% |
| Test suite length (statements) | 33 |
| Mutation score | 18 / 21 = 85.71% |
| Fuzz time | 120s |
| Pipeline time | 186s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JxPath/zest/15/t12001/JxPath-15f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_15/b120_r1/src/org/apache/commons/jxpath/ri/axes/UnionContext_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_15/b120_r1`
