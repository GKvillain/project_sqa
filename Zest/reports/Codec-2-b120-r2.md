# Zest – Codec-2 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 104780 (valid 89.91%) |
| Cycles | 3 |
| Corpus | 129 |
| Zest branch coverage (total / valid) | 344 / 342 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 129 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 170 / 214 = 79.44% |
| Branch coverage | 116 / 148 = 78.38% |
| Test suite length (statements) | 129 |
| Mutation score | 632 / 920 = 68.70% |
| Fuzz time | 120s |
| Pipeline time | 223s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Codec/zest/2/t12002/Codec-2f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_2/b120_r2/src/org/apache/commons/codec/binary/Base64_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_2/b120_r2`
