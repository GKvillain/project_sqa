# Zest – Jsoup-25 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 277307 (valid 76.60%) |
| Cycles | 39 |
| Corpus | 26 |
| Zest branch coverage (total / valid) | 149 / 146 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 26 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 64 / 90 = 71.11% |
| Branch coverage | 15 / 46 = 32.61% |
| Test suite length (statements) | 26 |
| Mutation score | 170 / 202 = 84.16% |
| Fuzz time | 120s |
| Pipeline time | 193s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/25/t12001/Jsoup-25f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_25/b120_r1/src/org/jsoup/parser/Tag_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_25/b120_r1`
