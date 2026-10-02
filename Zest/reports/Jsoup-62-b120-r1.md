# Zest – Jsoup-62 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 432515 (valid 48.28%) |
| Cycles | 109 |
| Corpus | 12 |
| Zest branch coverage (total / valid) | 137 / 130 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 12 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 56 / 1103 = 5.08% |
| Branch coverage | 3 / 757 = 0.40% |
| Test suite length (statements) | 12 |
| Mutation score | 1364 / 1370 = 99.56% |
| Fuzz time | 120s |
| Pipeline time | 190s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/62/t12001/Jsoup-62f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_62/b120_r1/src/org/jsoup/parser/HtmlTreeBuilderState_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_62/b120_r1`
