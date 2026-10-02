# Zest – Compress-10 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 283195 (valid 53.08%) |
| Cycles | 46 |
| Corpus | 12 |
| Zest branch coverage (total / valid) | 123 / 120 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 12 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 12 / 307 = 3.91% |
| Branch coverage | 1 / 113 = 0.88% |
| Test suite length (statements) | 12 |
| Mutation score | 385 / 386 = 99.74% |
| Fuzz time | 123s |
| Pipeline time | 218s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/10/t12001/Compress-10f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_10/b120_r1/src/org/apache/commons/compress/archivers/zip/ZipFile_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_10/b120_r1`
