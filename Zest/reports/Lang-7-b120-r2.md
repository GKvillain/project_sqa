# Zest – Lang-7 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 250915 (valid 2.03%) |
| Cycles | 52 |
| Corpus | 17 |
| Zest branch coverage (total / valid) | 47 / 43 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 17 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 23 / 380 = 6.05% |
| Branch coverage | 4 / 366 = 1.09% |
| Test suite length (statements) | 38 |
| Mutation score | 853 / 882 = 96.71% |
| Fuzz time | 120s |
| Pipeline time | 97s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Lang/zest/7/t12002/Lang-7f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Lang_7/b120_r2/src/org/apache/commons/lang3/math/NumberUtils_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Lang_7/b120_r2`
