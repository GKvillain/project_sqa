# Zest – Jsoup-78 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 384453 (valid 83.60%) |
| Cycles | 32 |
| Corpus | 37 |
| Zest branch coverage (total / valid) | 232 / 230 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 35 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 30 / 112 = 26.79% |
| Branch coverage | 10 / 88 = 11.36% |
| Test suite length (statements) | 35 |
| Mutation score | 269 / 287 = 93.73% |
| Fuzz time | 120s |
| Pipeline time | 182s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/78/t12001/Jsoup-78f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_78/b120_r1/src/org/jsoup/helper/DataUtil_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_78/b120_r1`
