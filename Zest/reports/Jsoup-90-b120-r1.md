# Zest – Jsoup-90 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 332538 (valid 75.65%) |
| Cycles | 21 |
| Corpus | 48 |
| Zest branch coverage (total / valid) | 259 / 256 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 48 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 129 / 598 = 21.57% |
| Branch coverage | 20 / 236 = 8.47% |
| Test suite length (statements) | 48 |
| Mutation score | 544 / 641 = 84.87% |
| Fuzz time | 120s |
| Pipeline time | 186s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/90/t12001/Jsoup-90f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_90/b120_r1/src/org/jsoup/helper/HttpConnection_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_90/b120_r1`
