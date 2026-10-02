# Zest – JxPath-21 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 449660 (valid 48.12%) |
| Cycles | 25 |
| Corpus | 37 |
| Zest branch coverage (total / valid) | 192 / 189 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 37 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 27 / 71 = 38.03% |
| Branch coverage | 7 / 54 = 12.96% |
| Test suite length (statements) | 37 |
| Mutation score | 70 / 92 = 76.09% |
| Fuzz time | 120s |
| Pipeline time | 185s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JxPath/zest/21/t12001/JxPath-21f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_21/b120_r1/src/org/apache/commons/jxpath/ri/model/beans/PropertyPointer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_21/b120_r1`
