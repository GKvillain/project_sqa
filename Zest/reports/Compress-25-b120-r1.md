# Zest – Compress-25 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 170508 (valid 86.06%) |
| Cycles | 10 |
| Corpus | 55 |
| Zest branch coverage (total / valid) | 220 / 218 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 55 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 73 / 402 = 18.16% |
| Branch coverage | 23 / 252 = 9.13% |
| Test suite length (statements) | 55 |
| Mutation score | 835 / 905 = 92.27% |
| Fuzz time | 122s |
| Pipeline time | 223s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/25/t12001/Compress-25f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_25/b120_r1/src/org/apache/commons/compress/archivers/zip/ZipArchiveInputStream_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_25/b120_r1`
