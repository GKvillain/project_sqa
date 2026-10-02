# Zest – Jsoup-31 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 364117 (valid 65.63%) |
| Cycles | 47 |
| Corpus | 26 |
| Zest branch coverage (total / valid) | 142 / 138 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 26 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 164 / 1364 = 12.02% |
| Branch coverage | 6 / 417 = 1.44% |
| Test suite length (statements) | 26 |
| Mutation score | 867 / 872 = 99.43% |
| Fuzz time | 120s |
| Pipeline time | 191s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/31/t12001/Jsoup-31f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_31/b120_r1/src/org/jsoup/parser/Token_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_31/b120_r1`
