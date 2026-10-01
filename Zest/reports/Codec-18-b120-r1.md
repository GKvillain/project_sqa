# Zest – Codec-18 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 86331 (valid 78.09%) |
| Cycles | 5 |
| Corpus | 61 |
| Zest branch coverage (total / valid) | 206 / 203 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 61 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 30 / 39 = 76.92% |
| Branch coverage | 14 / 24 = 58.33% |
| Test suite length (statements) | 61 |
| Mutation score | 19 / 30 = 63.33% |
| Fuzz time | 121s |
| Pipeline time | 197s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Codec/zest/18/t12001/Codec-18f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_18/b120_r1/src/org/apache/commons/codec/binary/StringUtils_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_18/b120_r1`
