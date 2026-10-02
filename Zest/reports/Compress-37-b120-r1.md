# Zest – Compress-37 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 221527 (valid 76.90%) |
| Cycles | 17 |
| Corpus | 37 |
| Zest branch coverage (total / valid) | 199 / 196 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 37 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 49 / 239 = 20.50% |
| Branch coverage | 15 / 154 = 9.74% |
| Test suite length (statements) | 37 |
| Mutation score | 336 / 375 = 89.60% |
| Fuzz time | 121s |
| Pipeline time | 251s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/37/t12001/Compress-37f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_37/b120_r1/src/org/apache/commons/compress/archivers/tar/TarArchiveInputStream_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_37/b120_r1`
