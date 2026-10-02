# Zest – Collections-26 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 467847 (valid 82.58%) |
| Cycles | 38 |
| Corpus | 33 |
| Zest branch coverage (total / valid) | 175 / 172 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 33 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 32 / 37 = 86.49% |
| Branch coverage | 10 / 12 = 83.33% |
| Test suite length (statements) | 57 |
| Mutation score | 4 / 17 = 23.53% |
| Fuzz time | 125s |
| Pipeline time | 205s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Collections/zest/26/t12001/Collections-26f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_26/b120_r1/src/org/apache/commons/collections4/keyvalue/MultiKey_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_26/b120_r1`
