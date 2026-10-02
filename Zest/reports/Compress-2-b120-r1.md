# Zest – Compress-2 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 225632 (valid 49.64%) |
| Cycles | 16 |
| Corpus | 31 |
| Zest branch coverage (total / valid) | 169 / 166 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 31 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 28 / 92 = 30.43% |
| Branch coverage | 9 / 56 = 16.07% |
| Test suite length (statements) | 31 |
| Mutation score | 181 / 215 = 84.19% |
| Fuzz time | 123s |
| Pipeline time | 214s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/2/t12001/Compress-2f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_2/b120_r1/src/org/apache/commons/compress/archivers/ar/ArArchiveInputStream_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_2/b120_r1`
