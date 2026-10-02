# Zest – Compress-42 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 222153 (valid 90.66%) |
| Cycles | 30 |
| Corpus | 28 |
| Zest branch coverage (total / valid) | 176 / 173 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 28 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 69 / 244 = 28.28% |
| Branch coverage | 21 / 132 = 15.91% |
| Test suite length (statements) | 52 |
| Mutation score | 278 / 311 = 89.39% |
| Fuzz time | 120s |
| Pipeline time | 221s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/42/t12001/Compress-42f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_42/b120_r1/src/org/apache/commons/compress/archivers/zip/UnixStat_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_42/b120_r1`
