# Zest – Compress-17 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 318066 (valid 37.60%) |
| Cycles | 8 |
| Corpus | 133 |
| Zest branch coverage (total / valid) | 242 / 239 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 133 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 131 / 180 = 72.78% |
| Branch coverage | 78 / 118 = 66.10% |
| Test suite length (statements) | 133 |
| Mutation score | 529 / 646 = 81.89% |
| Fuzz time | 122s |
| Pipeline time | 251s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/17/t12001/Compress-17f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_17/b120_r1/src/org/apache/commons/compress/archivers/tar/TarUtils_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_17/b120_r1`
