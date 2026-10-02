# Zest – Jsoup-21 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 302549 (valid 54.15%) |
| Cycles | 16 |
| Corpus | 132 |
| Zest branch coverage (total / valid) | 358 / 351 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 132 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 111 / 192 = 57.81% |
| Branch coverage | 49 / 96 = 51.04% |
| Test suite length (statements) | 135 |
| Mutation score | 159 / 190 = 83.68% |
| Fuzz time | 120s |
| Pipeline time | 220s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/21/t12001/Jsoup-21f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_21/b120_r1/src/org/jsoup/select/CombiningEvaluator_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_21/b120_r1`
