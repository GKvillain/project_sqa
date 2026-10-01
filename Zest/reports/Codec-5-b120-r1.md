# Zest – Codec-5 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 98029 (valid 91.30%) |
| Cycles | 3 |
| Corpus | 135 |
| Zest branch coverage (total / valid) | 367 / 365 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 135 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 191 / 233 = 81.97% |
| Branch coverage | 126 / 162 = 77.78% |
| Test suite length (statements) | 135 |
| Mutation score | 615 / 990 = 62.12% |
| Fuzz time | 121s |
| Pipeline time | 246s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Codec/zest/5/t12001/Codec-5f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_5/b120_r1/src/org/apache/commons/codec/binary/Base64_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_5/b120_r1`
