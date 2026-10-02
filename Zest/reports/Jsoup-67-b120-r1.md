# Zest – Jsoup-67 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 376573 (valid 61.52%) |
| Cycles | 31 |
| Corpus | 40 |
| Zest branch coverage (total / valid) | 174 / 170 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 40 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 31 / 383 = 8.09% |
| Branch coverage | 3 / 202 = 1.49% |
| Test suite length (statements) | 40 |
| Mutation score | 509 / 527 = 96.58% |
| Fuzz time | 120s |
| Pipeline time | 183s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/67/t12001/Jsoup-67f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_67/b120_r1/src/org/jsoup/parser/HtmlTreeBuilder_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_67/b120_r1`
