# Zest – Compress-26 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 300767 (valid 62.63%) |
| Cycles | 22 |
| Corpus | 32 |
| Zest branch coverage (total / valid) | 165 / 162 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 32 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 23 / 45 = 51.11% |
| Branch coverage | 12 / 24 = 50.00% |
| Test suite length (statements) | 32 |
| Mutation score | 67 / 94 = 71.28% |
| Fuzz time | 123s |
| Pipeline time | 229s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/26/t12001/Compress-26f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_26/b120_r1/src/org/apache/commons/compress/utils/IOUtils_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_26/b120_r1`
