# Zest – Compress-29 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 207900 (valid 59.09%) |
| Cycles | 7 |
| Corpus | 84 |
| Zest branch coverage (total / valid) | 213 / 211 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 84 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 144 / 1619 = 8.89% |
| Branch coverage | 7 / 889 = 0.79% |
| Test suite length (statements) | 84 |
| Mutation score | 3113 / 3162 = 98.45% |
| Fuzz time | 121s |
| Pipeline time | 237s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/29/t12001/Compress-29f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_29/b120_r1/src/org/apache/commons/compress/archivers/ArchiveStreamFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_29/b120_r1`
