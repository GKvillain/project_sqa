# Zest – Compress-5 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 197269 (valid 87.55%) |
| Cycles | 11 |
| Corpus | 60 |
| Zest branch coverage (total / valid) | 220 / 218 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 60 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 60 / 164 = 36.59% |
| Branch coverage | 32 / 96 = 33.33% |
| Test suite length (statements) | 60 |
| Mutation score | 260 / 304 = 85.53% |
| Fuzz time | 123s |
| Pipeline time | 232s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/5/t12001/Compress-5f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_5/b120_r1/src/org/apache/commons/compress/archivers/zip/ZipArchiveInputStream_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_5/b120_r1`
