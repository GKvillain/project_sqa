# Zest – Jsoup-61 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 255792 (valid 88.93%) |
| Cycles | 6 |
| Corpus | 299 |
| Zest branch coverage (total / valid) | 1128 / 1121 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 299 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 107 / 352 = 30.40% |
| Branch coverage | 46 / 184 = 25.00% |
| Test suite length (statements) | 417 |
| Mutation score | 322 / 415 = 77.59% |
| Fuzz time | 120s |
| Pipeline time | 227s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/61/t12001/Jsoup-61f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_61/b120_r1/src/org/jsoup/nodes/Element_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_61/b120_r1`
