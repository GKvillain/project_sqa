# Zest – Codec-2 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 96906 (valid 92.06%) |
| Cycles | 3 |
| Corpus | 127 |
| Zest branch coverage (total / valid) | 344 / 342 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 127 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 170 / 214 = 79.44% |
| Branch coverage | 116 / 148 = 78.38% |
| Test suite length (statements) | 127 |
| Mutation score | 613 / 920 = 66.63% |
| Fuzz time | 120s |
| Pipeline time | 232s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Codec/zest/2/t12001/Codec-2f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_2/b120_r1/src/org/apache/commons/codec/binary/Base64_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_2/b120_r1`
