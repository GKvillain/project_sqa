# Zest – Jsoup-38 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 354576 (valid 48.78%) |
| Cycles | 72 |
| Corpus | 14 |
| Zest branch coverage (total / valid) | 123 / 119 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 14 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 50 / 1099 = 4.55% |
| Branch coverage | 0 / 751 = 0.00% |
| Test suite length (statements) | 23 |
| Mutation score | 1342 / 1344 = 99.85% |
| Fuzz time | 120s |
| Pipeline time | 185s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/38/t12001/Jsoup-38f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_38/b120_r1/src/org/jsoup/parser/HtmlTreeBuilderState_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_38/b120_r1`
