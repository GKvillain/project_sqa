# Zest – Compress-3 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 273355 (valid 87.17%) |
| Cycles | 36 |
| Corpus | 29 |
| Zest branch coverage (total / valid) | 185 / 183 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 29 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 34 / 95 = 35.79% |
| Branch coverage | 10 / 30 = 33.33% |
| Test suite length (statements) | 29 |
| Mutation score | 107 / 139 = 76.98% |
| Fuzz time | 123s |
| Pipeline time | 207s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/3/t12001/Compress-3f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_3/b120_r1/src/org/apache/commons/compress/archivers/tar/TarArchiveOutputStream_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_3/b120_r1`
