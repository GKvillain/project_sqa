# Zest – Compress-39 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 310096 (valid 63.58%) |
| Cycles | 17 |
| Corpus | 80 |
| Zest branch coverage (total / valid) | 190 / 187 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 80 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 49 / 66 = 74.24% |
| Branch coverage | 32 / 42 = 76.19% |
| Test suite length (statements) | 80 |
| Mutation score | 93 / 128 = 72.66% |
| Fuzz time | 120s |
| Pipeline time | 225s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/39/t12001/Compress-39f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_39/b120_r1/src/org/apache/commons/compress/utils/ArchiveUtils_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_39/b120_r1`
