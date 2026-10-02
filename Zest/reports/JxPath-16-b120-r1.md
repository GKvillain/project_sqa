# Zest – JxPath-16 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 296916 (valid 77.95%) |
| Cycles | 24 |
| Corpus | 48 |
| Zest branch coverage (total / valid) | 200 / 197 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 48 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 59 / 767 = 7.69% |
| Branch coverage | 35 / 584 = 5.99% |
| Test suite length (statements) | 58 |
| Mutation score | 824 / 858 = 96.04% |
| Fuzz time | 120s |
| Pipeline time | 208s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JxPath/zest/16/t12001/JxPath-16f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_16/b120_r1/src/org/apache/commons/jxpath/ri/model/dom/DOMNodePointer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_16/b120_r1`
