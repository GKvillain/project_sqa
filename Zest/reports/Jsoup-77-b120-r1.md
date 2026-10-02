# Zest – Jsoup-77 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 277281 (valid 78.88%) |
| Cycles | 5 |
| Corpus | 347 |
| Zest branch coverage (total / valid) | 898 / 895 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 347 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 41 / 69 = 59.42% |
| Branch coverage | 11 / 31 = 35.48% |
| Test suite length (statements) | 477 |
| Mutation score | 65 / 80 = 81.25% |
| Fuzz time | 120s |
| Pipeline time | 200s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/77/t12001/Jsoup-77f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_77/b120_r1/src/org/jsoup/parser/XmlTreeBuilder_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_77/b120_r1`
