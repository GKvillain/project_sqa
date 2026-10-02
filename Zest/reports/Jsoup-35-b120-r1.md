# Zest – Jsoup-35 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 378325 (valid 50.31%) |
| Cycles | 72 |
| Corpus | 13 |
| Zest branch coverage (total / valid) | 123 / 119 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 13 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 51 / 1079 = 4.73% |
| Branch coverage | 0 / 749 = 0.00% |
| Test suite length (statements) | 21 |
| Mutation score | 1340 / 1343 = 99.78% |
| Fuzz time | 120s |
| Pipeline time | 189s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/35/t12001/Jsoup-35f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_35/b120_r1/src/org/jsoup/parser/HtmlTreeBuilderState_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_35/b120_r1`
