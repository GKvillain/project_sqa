# Zest – Compress-24 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 385413 (valid 41.25%) |
| Cycles | 12 |
| Corpus | 92 |
| Zest branch coverage (total / valid) | 209 / 206 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 92 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 85 / 178 = 47.75% |
| Branch coverage | 48 / 116 = 41.38% |
| Test suite length (statements) | 92 |
| Mutation score | 448 / 618 = 72.49% |
| Fuzz time | 123s |
| Pipeline time | 238s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/24/t12001/Compress-24f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_24/b120_r1/src/org/apache/commons/compress/archivers/tar/TarUtils_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_24/b120_r1`
