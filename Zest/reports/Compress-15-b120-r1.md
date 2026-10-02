# Zest – Compress-15 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 185954 (valid 89.46%) |
| Cycles | 28 |
| Corpus | 24 |
| Zest branch coverage (total / valid) | 152 / 149 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 24 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 65 / 203 = 32.02% |
| Branch coverage | 21 / 110 = 19.09% |
| Test suite length (statements) | 35 |
| Mutation score | 212 / 260 = 81.54% |
| Fuzz time | 123s |
| Pipeline time | 216s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/15/t12001/Compress-15f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_15/b120_r1/src/org/apache/commons/compress/archivers/zip/ZipArchiveEntry_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_15/b120_r1`
