# Zest – Jsoup-81 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 330395 (valid 87.75%) |
| Cycles | 40 |
| Corpus | 32 |
| Zest branch coverage (total / valid) | 234 / 232 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 30 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 29 / 121 = 23.97% |
| Branch coverage | 10 / 98 = 10.20% |
| Test suite length (statements) | 30 |
| Mutation score | 273 / 290 = 94.14% |
| Fuzz time | 120s |
| Pipeline time | 185s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/81/t12001/Jsoup-81f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_81/b120_r1/src/org/jsoup/helper/DataUtil_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_81/b120_r1`
