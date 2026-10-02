# Zest – JacksonDatabind-100 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 343018 (valid 95.61%) |
| Cycles | 24 |
| Corpus | 29 |
| Zest branch coverage (total / valid) | 197 / 195 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 29 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 4 / 119 = 3.36% |
| Branch coverage | 0 / 77 = 0.00% |
| Test suite length (statements) | 29 |
| Mutation score | 100 / 105 = 95.24% |
| Fuzz time | 125s |
| Pipeline time | 250s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/100/t12001/JacksonDatabind-100f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_100/b120_r1/src/com/fasterxml/jackson/databind/node/TreeTraversingParser_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_100/b120_r1`
