# Zest – Compress-18 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 216223 (valid 70.86%) |
| Cycles | 13 |
| Corpus | 71 |
| Zest branch coverage (total / valid) | 339 / 337 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 71 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 134 / 204 = 65.69% |
| Branch coverage | 55 / 86 = 63.95% |
| Test suite length (statements) | 71 |
| Mutation score | 171 / 321 = 53.27% |
| Fuzz time | 122s |
| Pipeline time | 212s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/18/t12001/Compress-18f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_18/b120_r1/src/org/apache/commons/compress/archivers/tar/TarArchiveOutputStream_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_18/b120_r1`
