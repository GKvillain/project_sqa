# Zest – Compress-31 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 279726 (valid 41.47%) |
| Cycles | 8 |
| Corpus | 91 |
| Zest branch coverage (total / valid) | 209 / 206 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 91 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 88 / 174 = 50.57% |
| Branch coverage | 48 / 114 = 42.11% |
| Test suite length (statements) | 91 |
| Mutation score | 414 / 610 = 67.87% |
| Fuzz time | 122s |
| Pipeline time | 255s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/31/t12001/Compress-31f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_31/b120_r1/src/org/apache/commons/compress/archivers/tar/TarUtils_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_31/b120_r1`
