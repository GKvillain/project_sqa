# Zest – Codec-3 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 117674 (valid 93.43%) |
| Cycles | 4 |
| Corpus | 177 |
| Zest branch coverage (total / valid) | 553 / 543 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 177 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 272 / 453 = 60.04% |
| Branch coverage | 210 / 450 = 46.67% |
| Test suite length (statements) | 180 |
| Mutation score | 1283 / 2145 = 59.81% |
| Fuzz time | 121s |
| Pipeline time | 348s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Codec/zest/3/t12002/Codec-3f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_3/b120_r2/src/org/apache/commons/codec/language/DoubleMetaphone_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_3/b120_r2`
