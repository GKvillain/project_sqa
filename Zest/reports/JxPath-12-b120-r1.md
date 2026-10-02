# Zest – JxPath-12 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 294730 (valid 72.98%) |
| Cycles | 18 |
| Corpus | 38 |
| Zest branch coverage (total / valid) | 184 / 181 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 38 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 40 / 362 = 11.05% |
| Branch coverage | 21 / 273 = 7.69% |
| Test suite length (statements) | 38 |
| Mutation score | 446 / 468 = 95.30% |
| Fuzz time | 120s |
| Pipeline time | 194s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JxPath/zest/12/t12001/JxPath-12f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_12/b120_r1/src/org/apache/commons/jxpath/ri/model/dom/DOMNodePointer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_12/b120_r1`
