# Zest – Collections-27 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 370451 (valid 91.06%) |
| Cycles | 23 |
| Corpus | 51 |
| Zest branch coverage (total / valid) | 219 / 216 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 51 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 77 / 131 = 58.78% |
| Branch coverage | 27 / 54 = 50.00% |
| Test suite length (statements) | 51 |
| Mutation score | 69 / 87 = 79.31% |
| Fuzz time | 125s |
| Pipeline time | 211s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Collections/zest/27/t12001/Collections-27f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_27/b120_r1/src/org/apache/commons/collections4/map/MultiValueMap_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_27/b120_r1`
