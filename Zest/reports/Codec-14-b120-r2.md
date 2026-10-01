# Zest – Codec-14 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 114340 (valid 79.31%) |
| Cycles | 11 |
| Corpus | 41 |
| Zest branch coverage (total / valid) | 198 / 194 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 18 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 22 / 489 = 4.50% |
| Branch coverage | 4 / 230 = 1.74% |
| Test suite length (statements) | 18 |
| Mutation score | 521 / 525 = 99.24% |
| Fuzz time | 120s |
| Pipeline time | 189s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Codec/zest/14/t12002/Codec-14f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_14/b120_r2/src/org/apache/commons/codec/language/bm/Lang_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_14/b120_r2`
