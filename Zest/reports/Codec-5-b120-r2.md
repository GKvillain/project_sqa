# Zest – Codec-5 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 119929 (valid 90.87%) |
| Cycles | 5 |
| Corpus | 130 |
| Zest branch coverage (total / valid) | 367 / 365 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 130 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 191 / 233 = 81.97% |
| Branch coverage | 126 / 162 = 77.78% |
| Test suite length (statements) | 130 |
| Mutation score | 611 / 990 = 61.72% |
| Fuzz time | 120s |
| Pipeline time | 240s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Codec/zest/5/t12002/Codec-5f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_5/b120_r2/src/org/apache/commons/codec/binary/Base64_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_5/b120_r2`
