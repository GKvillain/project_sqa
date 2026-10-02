# Zest – Jsoup-55 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 466168 (valid 53.53%) |
| Cycles | 90 |
| Corpus | 15 |
| Zest branch coverage (total / valid) | 128 / 124 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 15 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 148 / 1149 = 12.88% |
| Branch coverage | 0 / 325 = 0.00% |
| Test suite length (statements) | 24 |
| Mutation score | 731 / 740 = 98.78% |
| Fuzz time | 120s |
| Pipeline time | 186s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/55/t12001/Jsoup-55f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_55/b120_r1/src/org/jsoup/parser/TokeniserState_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_55/b120_r1`
