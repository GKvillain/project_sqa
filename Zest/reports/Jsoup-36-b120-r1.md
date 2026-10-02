# Zest – Jsoup-36 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 401281 (valid 49.51%) |
| Cycles | 28 |
| Corpus | 29 |
| Zest branch coverage (total / valid) | 150 / 147 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 29 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 23 / 72 = 31.94% |
| Branch coverage | 8 / 46 = 17.39% |
| Test suite length (statements) | 29 |
| Mutation score | 89 / 100 = 89.00% |
| Fuzz time | 120s |
| Pipeline time | 189s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/36/t12001/Jsoup-36f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_36/b120_r1/src/org/jsoup/helper/DataUtil_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_36/b120_r1`
