# Zest – Compress-22 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 292017 (valid 100.00%) |
| Cycles | 63 |
| Corpus | 8 |
| Zest branch coverage (total / valid) | 116 / 116 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 8 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 9 / 490 = 1.84% |
| Branch coverage | 1 / 265 = 0.38% |
| Test suite length (statements) | 8 |
| Mutation score | 1336 / 1339 = 99.78% |
| Fuzz time | 122s |
| Pipeline time | 215s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/22/t12001/Compress-22f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_22/b120_r1/src/org/apache/commons/compress/compressors/bzip2/BZip2CompressorInputStream_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_22/b120_r1`
