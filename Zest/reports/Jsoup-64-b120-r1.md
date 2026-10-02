# Zest – Jsoup-64 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 390885 (valid 64.45%) |
| Cycles | 87 |
| Corpus | 15 |
| Zest branch coverage (total / valid) | 140 / 137 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 15 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 56 / 1100 = 5.09% |
| Branch coverage | 3 / 783 = 0.38% |
| Test suite length (statements) | 22 |
| Mutation score | 1345 / 1351 = 99.56% |
| Fuzz time | 120s |
| Pipeline time | 187s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/64/t12001/Jsoup-64f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_64/b120_r1/src/org/jsoup/parser/HtmlTreeBuilderState_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_64/b120_r1`
