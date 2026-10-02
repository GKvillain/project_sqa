# Zest – Compress-27 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 224846 (valid 41.70%) |
| Cycles | 7 |
| Corpus | 89 |
| Zest branch coverage (total / valid) | 209 / 206 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 89 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 85 / 175 = 48.57% |
| Branch coverage | 48 / 114 = 42.11% |
| Test suite length (statements) | 89 |
| Mutation score | 455 / 615 = 73.98% |
| Fuzz time | 122s |
| Pipeline time | 242s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/27/t12001/Compress-27f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_27/b120_r1/src/org/apache/commons/compress/archivers/tar/TarUtils_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_27/b120_r1`
