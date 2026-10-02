# Zest – Compress-13 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 286943 (valid 89.20%) |
| Cycles | 47 |
| Corpus | 30 |
| Zest branch coverage (total / valid) | 170 / 167 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 30 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 67 / 203 = 33.00% |
| Branch coverage | 22 / 110 = 20.00% |
| Test suite length (statements) | 40 |
| Mutation score | 223 / 266 = 83.83% |
| Fuzz time | 123s |
| Pipeline time | 217s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/13/t12001/Compress-13f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_13/b120_r1/src/org/apache/commons/compress/archivers/zip/ZipArchiveEntry_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_13/b120_r1`
