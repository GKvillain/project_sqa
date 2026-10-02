# Zest – JxPath-17 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 354718 (valid 53.34%) |
| Cycles | 40 |
| Corpus | 17 |
| Zest branch coverage (total / valid) | 136 / 133 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 17 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 11 / 110 = 10.00% |
| Branch coverage | 4 / 86 = 4.65% |
| Test suite length (statements) | 17 |
| Mutation score | 160 / 175 = 91.43% |
| Fuzz time | 120s |
| Pipeline time | 197s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JxPath/zest/17/t12001/JxPath-17f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_17/b120_r1/src/org/apache/commons/jxpath/ri/model/dom/DOMAttributeIterator_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_17/b120_r1`
