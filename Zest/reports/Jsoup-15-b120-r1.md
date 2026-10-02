# Zest – Jsoup-15 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 366396 (valid 63.60%) |
| Cycles | 113 |
| Corpus | 11 |
| Zest branch coverage (total / valid) | 123 / 120 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 11 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 51 / 1079 = 4.73% |
| Branch coverage | 0 / 747 = 0.00% |
| Test suite length (statements) | 21 |
| Mutation score | 1324 / 1329 = 99.62% |
| Fuzz time | 120s |
| Pipeline time | 193s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/15/t12001/Jsoup-15f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_15/b120_r1/src/org/jsoup/parser/TreeBuilderState_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_15/b120_r1`
