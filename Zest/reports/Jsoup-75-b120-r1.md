# Zest – Jsoup-75 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 337916 (valid 95.04%) |
| Cycles | 21 |
| Corpus | 78 |
| Zest branch coverage (total / valid) | 304 / 301 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 78 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 94 / 167 = 56.29% |
| Branch coverage | 42 / 78 = 53.85% |
| Test suite length (statements) | 110 |
| Mutation score | 189 / 228 = 82.89% |
| Fuzz time | 120s |
| Pipeline time | 189s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Jsoup/zest/75/t12001/Jsoup-75f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_75/b120_r1/src/org/jsoup/nodes/Attributes_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_75/b120_r1`
