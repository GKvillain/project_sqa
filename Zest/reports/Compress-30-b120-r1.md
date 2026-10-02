# Zest – Compress-30 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 66883 (valid 57.91%) |
| Cycles | 9 |
| Corpus | 48 |
| Zest branch coverage (total / valid) | 137 / 110 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 48 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 15 / 492 = 3.05% |
| Branch coverage | 8 / 267 = 3.00% |
| Test suite length (statements) | 48 |
| Mutation score | 1326 / 1346 = 98.51% |
| Fuzz time | 123s |
| Pipeline time | 230s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/30/t12001/Compress-30f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_30/b120_r1/src/org/apache/commons/compress/compressors/bzip2/BZip2CompressorInputStream_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_30/b120_r1`
