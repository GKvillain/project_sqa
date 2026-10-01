# Zest – Codec-6 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 116602 (valid 58.29%) |
| Cycles | 6 |
| Corpus | 60 |
| Zest branch coverage (total / valid) | 248 / 245 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 60 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 25 / 38 = 65.79% |
| Branch coverage | 14 / 30 = 46.67% |
| Test suite length (statements) | 60 |
| Mutation score | 89 / 116 = 76.72% |
| Fuzz time | 121s |
| Pipeline time | 195s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Codec/zest/6/t12002/Codec-6f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_6/b120_r2/src/org/apache/commons/codec/binary/Base64InputStream_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_6/b120_r2`
