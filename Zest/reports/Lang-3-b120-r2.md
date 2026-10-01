# Zest – Lang-3 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 257539 (valid 1.99%) |
| Cycles | 20 |
| Corpus | 44 |
| Zest branch coverage (total / valid) | 161 / 87 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 44 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 125 / 375 = 33.33% |
| Branch coverage | 85 / 338 = 25.15% |
| Test suite length (statements) | 96 |
| Mutation score | 649 / 898 = 72.27% |
| Fuzz time | 120s |
| Pipeline time | 113s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Lang/zest/3/t12002/Lang-3f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Lang_3/b120_r2/src/org/apache/commons/lang3/math/NumberUtils_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Lang_3/b120_r2`
