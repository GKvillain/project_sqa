# Zest – Compress-12 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 206518 (valid 95.10%) |
| Cycles | 18 |
| Corpus | 37 |
| Zest branch coverage (total / valid) | 193 / 191 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 37 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 44 / 200 = 22.00% |
| Branch coverage | 10 / 110 = 9.09% |
| Test suite length (statements) | 37 |
| Mutation score | 287 / 318 = 90.25% |
| Fuzz time | 123s |
| Pipeline time | 220s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/12/t12001/Compress-12f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_12/b120_r1/src/org/apache/commons/compress/archivers/tar/TarArchiveInputStream_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_12/b120_r1`
