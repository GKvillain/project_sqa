# Zest – Compress-16 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 254753 (valid 54.07%) |
| Cycles | 37 |
| Corpus | 13 |
| Zest branch coverage (total / valid) | 129 / 126 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 12 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 9 / 71 = 12.68% |
| Branch coverage | 5 / 50 = 10.00% |
| Test suite length (statements) | 12 |
| Mutation score | 60 / 62 = 96.77% |
| Fuzz time | 123s |
| Pipeline time | 214s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/16/t12001/Compress-16f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_16/b120_r1/src/org/apache/commons/compress/archivers/ArchiveStreamFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_16/b120_r1`
