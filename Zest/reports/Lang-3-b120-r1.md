# Zest – Lang-3 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 229992 (valid 0.74%) |
| Cycles | 20 |
| Corpus | 42 |
| Zest branch coverage (total / valid) | 151 / 75 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 42 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 121 / 375 = 32.27% |
| Branch coverage | 76 / 338 = 22.49% |
| Test suite length (statements) | 86 |
| Mutation score | 674 / 898 = 75.06% |
| Fuzz time | 120s |
| Pipeline time | 108s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Lang/zest/3/t12001/Lang-3f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Lang_3/b120_r1/src/org/apache/commons/lang3/math/NumberUtils_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Lang_3/b120_r1`
