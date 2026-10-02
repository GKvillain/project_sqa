# Zest – Jsoup-27 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 295967 (valid 55.40%) |
| Cycles | 30 |
| Corpus | 21 |
| Zest branch coverage (total / valid) | 138 / 135 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 21 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 19 / 49 = 38.78% |
| Branch coverage | 4 / 28 = 14.29% |
| Test suite length (statements) | 21 |
| Mutation score | 53 / 57 = 92.98% |
| Fuzz time | 120s |
| Pipeline time | 196s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/27/t12001/Jsoup-27f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_27/b120_r1/src/org/jsoup/helper/DataUtil_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_27/b120_r1`
