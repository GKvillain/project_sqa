# Zest – JxPath-5 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 326775 (valid 71.08%) |
| Cycles | 18 |
| Corpus | 44 |
| Zest branch coverage (total / valid) | 214 / 211 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 44 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 37 / 179 = 20.67% |
| Branch coverage | 13 / 130 = 10.00% |
| Test suite length (statements) | 44 |
| Mutation score | 238 / 269 = 88.48% |
| Fuzz time | 120s |
| Pipeline time | 191s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JxPath/zest/5/t12001/JxPath-5f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_5/b120_r1/src/org/apache/commons/jxpath/ri/model/NodePointer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_5/b120_r1`
