# Zest – Codec-14 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 116194 (valid 90.02%) |
| Cycles | 12 |
| Corpus | 38 |
| Zest branch coverage (total / valid) | 197 / 194 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 7 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 21 / 489 = 4.29% |
| Branch coverage | 3 / 230 = 1.30% |
| Test suite length (statements) | 7 |
| Mutation score | 521 / 525 = 99.24% |
| Fuzz time | 121s |
| Pipeline time | 198s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Codec/zest/14/t12001/Codec-14f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_14/b120_r1/src/org/apache/commons/codec/language/bm/Lang_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_14/b120_r1`
