# Zest – Jsoup-85 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 351511 (valid 91.88%) |
| Cycles | 22 |
| Corpus | 116 |
| Zest branch coverage (total / valid) | 382 / 379 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 116 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 47 / 66 = 71.21% |
| Branch coverage | 22 / 50 = 44.00% |
| Test suite length (statements) | 198 |
| Mutation score | 74 / 103 = 71.84% |
| Fuzz time | 120s |
| Pipeline time | 191s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/85/t12001/Jsoup-85f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_85/b120_r1/src/org/jsoup/nodes/Attribute_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_85/b120_r1`
