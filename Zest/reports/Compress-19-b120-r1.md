# Zest – Compress-19 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 307167 (valid 77.49%) |
| Cycles | 24 |
| Corpus | 36 |
| Zest branch coverage (total / valid) | 183 / 181 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 36 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 38 / 105 = 36.19% |
| Branch coverage | 15 / 60 = 25.00% |
| Test suite length (statements) | 36 |
| Mutation score | 196 / 224 = 87.50% |
| Fuzz time | 123s |
| Pipeline time | 214s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/19/t12001/Compress-19f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_19/b120_r1/src/org/apache/commons/compress/archivers/zip/Zip64ExtendedInformationExtraField_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_19/b120_r1`
