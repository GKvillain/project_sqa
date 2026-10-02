# Zest – JacksonDatabind-56 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 406310 (valid 100.00%) |
| Cycles | 79 |
| Corpus | 15 |
| Zest branch coverage (total / valid) | 126 / 126 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 15 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 15 / 117 = 12.82% |
| Branch coverage | 12 / 87 = 13.79% |
| Test suite length (statements) | 20 |
| Mutation score | 151 / 163 = 92.64% |
| Fuzz time | 125s |
| Pipeline time | 234s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/56/t12001/JacksonDatabind-56f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_56/b120_r1/src/com/fasterxml/jackson/databind/deser/std/FromStringDeserializer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_56/b120_r1`
