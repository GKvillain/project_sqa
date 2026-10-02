# Zest – Jsoup-10 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 303714 (valid 82.77%) |
| Cycles | 26 |
| Corpus | 44 |
| Zest branch coverage (total / valid) | 281 / 274 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 44 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 72 / 156 = 46.15% |
| Branch coverage | 18 / 50 = 36.00% |
| Test suite length (statements) | 247 |
| Mutation score | 122 / 162 = 75.31% |
| Fuzz time | 121s |
| Pipeline time | 177s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/10/t12001/Jsoup-10f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_10/b120_r1/src/org/jsoup/nodes/Node_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_10/b120_r1`
