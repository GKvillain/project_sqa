# Zest – Gson-6 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 449685 (valid 62.30%) |
| Cycles | 79 |
| Corpus | 11 |
| Zest branch coverage (total / valid) | 126 / 123 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 11 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 5 / 21 = 23.81% |
| Branch coverage | 0 / 8 = 0.00% |
| Test suite length (statements) | 11 |
| Mutation score | 7 / 7 = 100.00% |
| Fuzz time | 128s |
| Pipeline time | 179s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Gson/zest/6/t12001/Gson-6f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Gson_6/b120_r1/src/com/google/gson/internal/bind/JsonAdapterAnnotationTypeAdapterFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Gson_6/b120_r1`
