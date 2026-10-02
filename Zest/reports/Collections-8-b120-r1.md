# Zest – Collections-8 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 379472 (valid 72.20%) |
| Cycles | 38 |
| Corpus | 37 |
| Zest branch coverage (total / valid) | 175 / 172 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 37 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 49 / 90 = 54.44% |
| Branch coverage | 20 / 36 = 55.56% |
| Test suite length (statements) | 37 |
| Mutation score | 134 / 162 = 82.72% |
| Fuzz time | 127s |
| Pipeline time | 200s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Collections/zest/8/t12001/Collections-8f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_8/b120_r1/src/org/apache/commons/collections/buffer/UnboundedFifoBuffer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_8/b120_r1`
