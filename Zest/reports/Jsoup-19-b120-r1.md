# Zest – Jsoup-19 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 340442 (valid 85.87%) |
| Cycles | 28 |
| Corpus | 74 |
| Zest branch coverage (total / valid) | 345 / 342 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 74 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 130 / 149 = 87.25% |
| Branch coverage | 24 / 54 = 44.44% |
| Test suite length (statements) | 356 |
| Mutation score | 60 / 96 = 62.50% |
| Fuzz time | 120s |
| Pipeline time | 196s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/19/t12001/Jsoup-19f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_19/b120_r1/src/org/jsoup/safety/Whitelist_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_19/b120_r1`
