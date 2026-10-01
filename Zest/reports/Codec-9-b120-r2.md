# Zest – Codec-9 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 109266 (valid 93.83%) |
| Cycles | 4 |
| Corpus | 130 |
| Zest branch coverage (total / valid) | 346 / 344 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 130 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 171 / 220 = 77.73% |
| Branch coverage | 113 / 154 = 73.38% |
| Test suite length (statements) | 130 |
| Mutation score | 577 / 966 = 59.73% |
| Fuzz time | 120s |
| Pipeline time | 233s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Codec/zest/9/t12002/Codec-9f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_9/b120_r2/src/org/apache/commons/codec/binary/Base64_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_9/b120_r2`
