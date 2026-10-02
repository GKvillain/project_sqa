# Zest – JxPath-1 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 303656 (valid 81.45%) |
| Cycles | 20 |
| Corpus | 40 |
| Zest branch coverage (total / valid) | 190 / 187 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 40 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 49 / 773 = 6.34% |
| Branch coverage | 25 / 562 = 4.45% |
| Test suite length (statements) | 40 |
| Mutation score | 811 / 832 = 97.48% |
| Fuzz time | 120s |
| Pipeline time | 191s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JxPath/zest/1/t12001/JxPath-1f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_1/b120_r1/src/org/apache/commons/jxpath/ri/model/dom/DOMNodePointer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_1/b120_r1`
