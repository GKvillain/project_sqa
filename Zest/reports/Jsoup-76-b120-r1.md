# Zest – Jsoup-76 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 384706 (valid 71.66%) |
| Cycles | 73 |
| Corpus | 27 |
| Zest branch coverage (total / valid) | 145 / 141 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 27 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 56 / 1106 = 5.06% |
| Branch coverage | 4 / 787 = 0.51% |
| Test suite length (statements) | 40 |
| Mutation score | 1353 / 1359 = 99.56% |
| Fuzz time | 120s |
| Pipeline time | 180s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/76/t12001/Jsoup-76f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_76/b120_r1/src/org/jsoup/parser/HtmlTreeBuilderState_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_76/b120_r1`
