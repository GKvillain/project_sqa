# Zest – Jsoup-59 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 355735 (valid 85.11%) |
| Cycles | 44 |
| Corpus | 18 |
| Zest branch coverage (total / valid) | 122 / 119 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 18 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 27 / 161 = 16.77% |
| Branch coverage | 8 / 46 = 17.39% |
| Test suite length (statements) | 18 |
| Mutation score | 117 / 124 = 94.35% |
| Fuzz time | 120s |
| Pipeline time | 187s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/59/t12001/Jsoup-59f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_59/b120_r1/src/org/jsoup/parser/Token_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_59/b120_r1`
