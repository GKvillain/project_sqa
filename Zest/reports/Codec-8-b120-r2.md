# Zest – Codec-8 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 119360 (valid 85.05%) |
| Cycles | 4 |
| Corpus | 128 |
| Zest branch coverage (total / valid) | 350 / 348 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 128 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 191 / 254 = 75.20% |
| Branch coverage | 117 / 178 = 65.73% |
| Test suite length (statements) | 128 |
| Mutation score | 639 / 1071 = 59.66% |
| Fuzz time | 120s |
| Pipeline time | 250s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Codec/zest/8/t12002/Codec-8f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_8/b120_r2/src/org/apache/commons/codec/binary/Base64_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_8/b120_r2`
