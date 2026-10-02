# Zest – Jsoup-2 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 155317 (valid 82.83%) |
| Cycles | 6 |
| Corpus | 171 |
| Zest branch coverage (total / valid) | 700 / 676 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 171 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 122 / 163 = 74.85% |
| Branch coverage | 55 / 86 = 63.95% |
| Test suite length (statements) | 5329 |
| Mutation score | 127 / 185 = 68.65% |
| Fuzz time | 120s |
| Pipeline time | 243s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/2/t12001/Jsoup-2f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_2/b120_r1/src/org/jsoup/parser/Parser_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_2/b120_r1`
