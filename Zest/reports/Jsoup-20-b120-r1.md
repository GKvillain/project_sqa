# Zest – Jsoup-20 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 276203 (valid 63.04%) |
| Cycles | 34 |
| Corpus | 19 |
| Zest branch coverage (total / valid) | 139 / 136 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 18 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 19 / 46 = 41.30% |
| Branch coverage | 4 / 24 = 16.67% |
| Test suite length (statements) | 18 |
| Mutation score | 48 / 52 = 92.31% |
| Fuzz time | 120s |
| Pipeline time | 186s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/20/t12001/Jsoup-20f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_20/b120_r1/src/org/jsoup/helper/DataUtil_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_20/b120_r1`
