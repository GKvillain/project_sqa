# Zest – Codec-8 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 119690 (valid 77.09%) |
| Cycles | 4 |
| Corpus | 134 |
| Zest branch coverage (total / valid) | 350 / 348 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 134 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 191 / 254 = 75.20% |
| Branch coverage | 117 / 178 = 65.73% |
| Test suite length (statements) | 134 |
| Mutation score | 622 / 1071 = 58.08% |
| Fuzz time | 120s |
| Pipeline time | 254s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Codec/zest/8/t12001/Codec-8f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_8/b120_r1/src/org/apache/commons/codec/binary/Base64_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_8/b120_r1`
