# Zest – Compress-23 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 19803 (valid 52.91%) |
| Cycles | 5 |
| Corpus | 15 |
| Zest branch coverage (total / valid) | 275 / 258 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 15 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 20 / 103 = 19.42% |
| Branch coverage | 4 / 34 = 11.76% |
| Test suite length (statements) | 15 |
| Mutation score | 201 / 204 = 98.53% |
| Fuzz time | 122s |
| Pipeline time | 212s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/23/t12001/Compress-23f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_23/b120_r1/src/org/apache/commons/compress/archivers/sevenz/Coders_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_23/b120_r1`
