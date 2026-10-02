# Zest – JxPath-2 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 326710 (valid 86.66%) |
| Cycles | 47 |
| Corpus | 23 |
| Zest branch coverage (total / valid) | 156 / 153 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 23 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 16 / 43 = 37.21% |
| Branch coverage | 6 / 16 = 37.50% |
| Test suite length (statements) | 23 |
| Mutation score | 10 / 15 = 66.67% |
| Fuzz time | 120s |
| Pipeline time | 191s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JxPath/zest/2/t12001/JxPath-2f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_2/b120_r1/src/org/apache/commons/jxpath/ri/compiler/Expression_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_2/b120_r1`
