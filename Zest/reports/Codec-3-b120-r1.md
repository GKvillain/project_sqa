# Zest – Codec-3 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 115111 (valid 87.39%) |
| Cycles | 4 |
| Corpus | 164 |
| Zest branch coverage (total / valid) | 563 / 553 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 164 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 272 / 453 = 60.04% |
| Branch coverage | 213 / 450 = 47.33% |
| Test suite length (statements) | 167 |
| Mutation score | 1320 / 2145 = 61.54% |
| Fuzz time | 120s |
| Pipeline time | 333s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Codec/zest/3/t12001/Codec-3f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_3/b120_r1/src/org/apache/commons/codec/language/DoubleMetaphone_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_3/b120_r1`
