# Zest – Codec-6 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 103138 (valid 56.31%) |
| Cycles | 5 |
| Corpus | 62 |
| Zest branch coverage (total / valid) | 249 / 246 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 62 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 26 / 38 = 68.42% |
| Branch coverage | 15 / 30 = 50.00% |
| Test suite length (statements) | 62 |
| Mutation score | 95 / 116 = 81.90% |
| Fuzz time | 120s |
| Pipeline time | 201s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Codec/zest/6/t12001/Codec-6f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_6/b120_r1/src/org/apache/commons/codec/binary/Base64InputStream_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_6/b120_r1`
