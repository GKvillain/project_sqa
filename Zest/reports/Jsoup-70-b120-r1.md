# Zest – Jsoup-70 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 354566 (valid 88.38%) |
| Cycles | 8 |
| Corpus | 296 |
| Zest branch coverage (total / valid) | 1120 / 1117 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 296 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 102 / 397 = 25.69% |
| Branch coverage | 37 / 206 = 17.96% |
| Test suite length (statements) | 405 |
| Mutation score | 435 / 487 = 89.32% |
| Fuzz time | 120s |
| Pipeline time | 219s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/70/t12001/Jsoup-70f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_70/b120_r1/src/org/jsoup/nodes/Element_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_70/b120_r1`
