# Zest – Compress-41 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 277116 (valid 75.33%) |
| Cycles | 18 |
| Corpus | 56 |
| Zest branch coverage (total / valid) | 220 / 218 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 56 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 86 / 413 = 20.82% |
| Branch coverage | 31 / 260 = 11.92% |
| Test suite length (statements) | 56 |
| Mutation score | 832 / 920 = 90.43% |
| Fuzz time | 120s |
| Pipeline time | 237s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/41/t12001/Compress-41f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_41/b120_r1/src/org/apache/commons/compress/archivers/zip/ZipArchiveInputStream_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_41/b120_r1`
