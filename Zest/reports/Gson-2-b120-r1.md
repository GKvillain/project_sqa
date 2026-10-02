# Zest – Gson-2 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 225404 (valid 89.50%) |
| Cycles | 29 |
| Corpus | 20 |
| Zest branch coverage (total / valid) | 132 / 129 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 20 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 98 / 454 = 21.59% |
| Branch coverage | 0 / 192 = 0.00% |
| Test suite length (statements) | 20 |
| Mutation score | 266 / 266 = 100.00% |
| Fuzz time | 128s |
| Pipeline time | 191s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Gson/zest/2/t12001/Gson-2f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Gson_2/b120_r1/src/com/google/gson/internal/bind/TypeAdapters_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Gson_2/b120_r1`
