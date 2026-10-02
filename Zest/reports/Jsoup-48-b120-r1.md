# Zest – Jsoup-48 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 410751 (valid 64.22%) |
| Cycles | 28 |
| Corpus | 51 |
| Zest branch coverage (total / valid) | 244 / 241 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 51 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 112 / 477 = 23.48% |
| Branch coverage | 14 / 176 = 7.95% |
| Test suite length (statements) | 51 |
| Mutation score | 380 / 437 = 86.96% |
| Fuzz time | 120s |
| Pipeline time | 192s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/48/t12001/Jsoup-48f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_48/b120_r1/src/org/jsoup/helper/HttpConnection_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_48/b120_r1`
