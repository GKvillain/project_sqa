# Zest – Jsoup-63 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 357140 (valid 73.92%) |
| Cycles | 26 |
| Corpus | 32 |
| Zest branch coverage (total / valid) | 198 / 195 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 32 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 59 / 535 = 11.03% |
| Branch coverage | 5 / 292 = 1.71% |
| Test suite length (statements) | 32 |
| Mutation score | 713 / 742 = 96.09% |
| Fuzz time | 120s |
| Pipeline time | 191s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/63/t12001/Jsoup-63f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_63/b120_r1/src/org/jsoup/parser/HtmlTreeBuilder_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_63/b120_r1`
