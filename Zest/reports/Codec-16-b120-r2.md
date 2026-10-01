# Zest – Codec-16 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 122186 (valid 70.57%) |
| Cycles | 9 |
| Corpus | 48 |
| Zest branch coverage (total / valid) | 223 / 220 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 48 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 44 / 166 = 26.51% |
| Branch coverage | 20 / 75 = 26.67% |
| Test suite length (statements) | 48 |
| Mutation score | 562 / 912 = 61.62% |
| Fuzz time | 120s |
| Pipeline time | 206s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Codec/zest/16/t12002/Codec-16f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_16/b120_r2/src/org/apache/commons/codec/binary/Base32_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_16/b120_r2`
