# Zest – Codec-1 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 106199 (valid 97.09%) |
| Cycles | 2 |
| Corpus | 184 |
| Zest branch coverage (total / valid) | 352 / 350 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 184 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 199 / 256 = 77.73% |
| Branch coverage | 122 / 184 = 66.30% |
| Test suite length (statements) | 184 |
| Mutation score | 334 / 585 = 57.09% |
| Fuzz time | 120s |
| Pipeline time | 228s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Codec/zest/1/t12002/Codec-1f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_1/b120_r2/src/org/apache/commons/codec/language/Caverphone_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_1/b120_r2`
