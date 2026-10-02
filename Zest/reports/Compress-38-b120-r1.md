# Zest – Compress-38 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 270645 (valid 78.70%) |
| Cycles | 32 |
| Corpus | 31 |
| Zest branch coverage (total / valid) | 181 / 178 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 31 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 42 / 304 = 13.82% |
| Branch coverage | 14 / 147 = 9.52% |
| Test suite length (statements) | 34 |
| Mutation score | 402 / 447 = 89.93% |
| Fuzz time | 122s |
| Pipeline time | 214s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/38/t12001/Compress-38f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_38/b120_r1/src/org/apache/commons/compress/archivers/tar/TarArchiveEntry_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_38/b120_r1`
