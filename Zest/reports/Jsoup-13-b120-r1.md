# Zest – Jsoup-13 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 371957 (valid 65.88%) |
| Cycles | 23 |
| Corpus | 46 |
| Zest branch coverage (total / valid) | 267 / 261 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 46 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 67 / 198 = 33.84% |
| Branch coverage | 14 / 70 = 20.00% |
| Test suite length (statements) | 240 |
| Mutation score | 181 / 218 = 83.03% |
| Fuzz time | 120s |
| Pipeline time | 182s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/13/t12001/Jsoup-13f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_13/b120_r1/src/org/jsoup/nodes/Node_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_13/b120_r1`
