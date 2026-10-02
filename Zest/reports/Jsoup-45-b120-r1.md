# Zest – Jsoup-45 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 383787 (valid 63.81%) |
| Cycles | 30 |
| Corpus | 39 |
| Zest branch coverage (total / valid) | 175 / 171 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 38 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 43 / 376 = 11.44% |
| Branch coverage | 5 / 202 = 2.48% |
| Test suite length (statements) | 38 |
| Mutation score | 495 / 519 = 95.38% |
| Fuzz time | 120s |
| Pipeline time | 184s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/45/t12001/Jsoup-45f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_45/b120_r1/src/org/jsoup/parser/HtmlTreeBuilder_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_45/b120_r1`
