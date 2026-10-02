# Zest – JxPath-22 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 302261 (valid 72.53%) |
| Cycles | 18 |
| Corpus | 45 |
| Zest branch coverage (total / valid) | 195 / 192 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 45 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 54 / 354 = 15.25% |
| Branch coverage | 33 / 276 = 11.96% |
| Test suite length (statements) | 45 |
| Mutation score | 418 / 445 = 93.93% |
| Fuzz time | 120s |
| Pipeline time | 192s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JxPath/zest/22/t12001/JxPath-22f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_22/b120_r1/src/org/apache/commons/jxpath/ri/model/dom/DOMNodePointer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_22/b120_r1`
