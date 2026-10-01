# Zest – Codec-1 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 100431 (valid 97.37%) |
| Cycles | 2 |
| Corpus | 208 |
| Zest branch coverage (total / valid) | 351 / 349 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 208 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 201 / 256 = 78.52% |
| Branch coverage | 122 / 184 = 66.30% |
| Test suite length (statements) | 208 |
| Mutation score | 352 / 585 = 60.17% |
| Fuzz time | 120s |
| Pipeline time | 248s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Codec/zest/1/t12001/Codec-1f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_1/b120_r1/src/org/apache/commons/codec/language/Caverphone_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_1/b120_r1`
