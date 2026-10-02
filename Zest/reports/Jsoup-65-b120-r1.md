# Zest – Jsoup-65 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 377279 (valid 60.58%) |
| Cycles | 33 |
| Corpus | 36 |
| Zest branch coverage (total / valid) | 179 / 176 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 36 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 78 / 1485 = 5.25% |
| Branch coverage | 3 / 987 = 0.30% |
| Test suite length (statements) | 36 |
| Mutation score | 1864 / 1881 = 99.10% |
| Fuzz time | 120s |
| Pipeline time | 183s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/65/t12001/Jsoup-65f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_65/b120_r1/src/org/jsoup/parser/HtmlTreeBuilder_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_65/b120_r1`
