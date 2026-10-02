# Zest – JacksonDatabind-106 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 365715 (valid 95.16%) |
| Cycles | 29 |
| Corpus | 25 |
| Zest branch coverage (total / valid) | 197 / 195 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 25 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 4 / 125 = 3.20% |
| Branch coverage | 0 / 81 = 0.00% |
| Test suite length (statements) | 25 |
| Mutation score | 106 / 111 = 95.50% |
| Fuzz time | 125s |
| Pipeline time | 258s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/106/t12001/JacksonDatabind-106f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_106/b120_r1/src/com/fasterxml/jackson/databind/node/TreeTraversingParser_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_106/b120_r1`
