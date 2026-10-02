# Zest – Jsoup-24 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 358663 (valid 59.92%) |
| Cycles | 144 |
| Corpus | 9 |
| Zest branch coverage (total / valid) | 77 / 73 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 9 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 136 / 1194 = 11.39% |
| Branch coverage | 0 / 354 = 0.00% |
| Test suite length (statements) | 18 |
| Mutation score | 768 / 768 = 100.00% |
| Fuzz time | 120s |
| Pipeline time | 187s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/24/t12001/Jsoup-24f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_24/b120_r1/src/org/jsoup/parser/TokeniserState_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_24/b120_r1`
