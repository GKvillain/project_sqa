# Zest – JacksonDatabind-91 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 399122 (valid 48.05%) |
| Cycles | 52 |
| Corpus | 18 |
| Zest branch coverage (total / valid) | 167 / 164 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 18 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 20 / 169 = 11.83% |
| Branch coverage | 3 / 126 = 2.38% |
| Test suite length (statements) | 18 |
| Mutation score | 143 / 148 = 96.62% |
| Fuzz time | 126s |
| Pipeline time | 232s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonDatabind/zest/91/t12001/JacksonDatabind-91f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_91/b120_r1/src/com/fasterxml/jackson/databind/deser/DeserializerCache_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_91/b120_r1`
