# Zest – Jsoup-80 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 387711 (valid 83.13%) |
| Cycles | 10 |
| Corpus | 277 |
| Zest branch coverage (total / valid) | 850 / 847 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 277 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 32 / 70 = 45.71% |
| Branch coverage | 5 / 33 = 15.15% |
| Test suite length (statements) | 378 |
| Mutation score | 82 / 85 = 96.47% |
| Fuzz time | 120s |
| Pipeline time | 195s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/80/t12001/Jsoup-80f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_80/b120_r1/src/org/jsoup/parser/XmlTreeBuilder_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_80/b120_r1`
