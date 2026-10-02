# Zest – Jsoup-33 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 352199 (valid 54.02%) |
| Cycles | 20 |
| Corpus | 47 |
| Zest branch coverage (total / valid) | 186 / 182 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 47 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 31 / 394 = 7.87% |
| Branch coverage | 4 / 202 = 1.98% |
| Test suite length (statements) | 47 |
| Mutation score | 336 / 351 = 95.73% |
| Fuzz time | 120s |
| Pipeline time | 200s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/33/t12001/Jsoup-33f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_33/b120_r1/src/org/jsoup/parser/HtmlTreeBuilder_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_33/b120_r1`
