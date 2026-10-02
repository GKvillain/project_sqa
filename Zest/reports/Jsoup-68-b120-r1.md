# Zest – Jsoup-68 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 392088 (valid 61.74%) |
| Cycles | 35 |
| Corpus | 35 |
| Zest branch coverage (total / valid) | 174 / 170 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 35 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 32 / 380 = 8.42% |
| Branch coverage | 3 / 202 = 1.49% |
| Test suite length (statements) | 35 |
| Mutation score | 511 / 529 = 96.60% |
| Fuzz time | 120s |
| Pipeline time | 187s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/68/t12001/Jsoup-68f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_68/b120_r1/src/org/jsoup/parser/HtmlTreeBuilder_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_68/b120_r1`
