# Zest – Jsoup-82 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 416485 (valid 81.01%) |
| Cycles | 36 |
| Corpus | 38 |
| Zest branch coverage (total / valid) | 234 / 232 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 33 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 27 / 115 = 23.48% |
| Branch coverage | 12 / 98 = 12.24% |
| Test suite length (statements) | 33 |
| Mutation score | 271 / 287 = 94.43% |
| Fuzz time | 120s |
| Pipeline time | 185s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/82/t12001/Jsoup-82f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_82/b120_r1/src/org/jsoup/helper/DataUtil_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_82/b120_r1`
