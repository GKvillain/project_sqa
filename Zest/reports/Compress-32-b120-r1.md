# Zest – Compress-32 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 315117 (valid 82.68%) |
| Cycles | 31 |
| Corpus | 36 |
| Zest branch coverage (total / valid) | 198 / 195 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 36 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 44 / 220 = 20.00% |
| Branch coverage | 13 / 126 = 10.32% |
| Test suite length (statements) | 36 |
| Mutation score | 297 / 327 = 90.83% |
| Fuzz time | 123s |
| Pipeline time | 241s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/32/t12001/Compress-32f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_32/b120_r1/src/org/apache/commons/compress/archivers/tar/TarArchiveInputStream_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_32/b120_r1`
