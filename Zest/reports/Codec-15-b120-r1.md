# Zest – Codec-15 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 104194 (valid 63.14%) |
| Cycles | 5 |
| Corpus | 93 |
| Zest branch coverage (total / valid) | 254 / 252 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 93 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 51 / 51 = 100.00% |
| Branch coverage | 29 / 32 = 90.62% |
| Test suite length (statements) | 93 |
| Mutation score | 63 / 106 = 59.43% |
| Fuzz time | 120s |
| Pipeline time | 210s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Codec/zest/15/t12001/Codec-15f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_15/b120_r1/src/org/apache/commons/codec/language/Soundex_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_15/b120_r1`
