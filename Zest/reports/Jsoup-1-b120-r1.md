# Zest – Jsoup-1 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 199593 (valid 80.94%) |
| Cycles | 17 |
| Corpus | 61 |
| Zest branch coverage (total / valid) | 440 / 433 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 61 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 37 / 46 = 80.43% |
| Branch coverage | 12 / 18 = 66.67% |
| Test suite length (statements) | 984 |
| Mutation score | 20 / 25 = 80.00% |
| Fuzz time | 120s |
| Pipeline time | 200s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/1/t12001/Jsoup-1f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_1/b120_r1/src/org/jsoup/nodes/Document_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_1/b120_r1`
