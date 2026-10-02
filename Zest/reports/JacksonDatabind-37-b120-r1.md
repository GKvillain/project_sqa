# Zest – JacksonDatabind-37 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 574568 (valid 53.22%) |
| Cycles | 92 |
| Corpus | 17 |
| Zest branch coverage (total / valid) | 137 / 131 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 16 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 20 / 67 = 29.85% |
| Branch coverage | 4 / 32 = 12.50% |
| Test suite length (statements) | 16 |
| Mutation score | 53 / 64 = 82.81% |
| Fuzz time | 124s |
| Pipeline time | 255s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/37/t12001/JacksonDatabind-37f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_37/b120_r1/src/com/fasterxml/jackson/databind/type/SimpleType_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_37/b120_r1`
