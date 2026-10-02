# Zest – JxPath-11 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 463855 (valid 52.36%) |
| Cycles | 58 |
| Corpus | 15 |
| Zest branch coverage (total / valid) | 136 / 133 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 15 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 11 / 119 = 9.24% |
| Branch coverage | 4 / 94 = 4.26% |
| Test suite length (statements) | 15 |
| Mutation score | 163 / 178 = 91.57% |
| Fuzz time | 120s |
| Pipeline time | 192s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JxPath/zest/11/t12001/JxPath-11f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_11/b120_r1/src/org/apache/commons/jxpath/ri/model/dom/DOMAttributeIterator_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_11/b120_r1`
