# Zest – Compress-6 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 180716 (valid 83.99%) |
| Cycles | 29 |
| Corpus | 23 |
| Zest branch coverage (total / valid) | 141 / 138 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 23 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 49 / 139 = 35.25% |
| Branch coverage | 12 / 60 = 20.00% |
| Test suite length (statements) | 25 |
| Mutation score | 122 / 150 = 81.33% |
| Fuzz time | 123s |
| Pipeline time | 202s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/6/t12001/Compress-6f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_6/b120_r1/src/org/apache/commons/compress/archivers/zip/ZipArchiveEntry_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_6/b120_r1`
