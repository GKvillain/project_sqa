# Zest – Gson-5 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 174805 (valid 76.31%) |
| Cycles | 14 |
| Corpus | 64 |
| Zest branch coverage (total / valid) | 188 / 185 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 64 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 57 / 151 = 37.75% |
| Branch coverage | 31 / 91 = 34.07% |
| Test suite length (statements) | 64 |
| Mutation score | 314 / 361 = 86.98% |
| Fuzz time | 129s |
| Pipeline time | 191s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Gson/zest/5/t12001/Gson-5f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Gson_5/b120_r1/src/com/google/gson/internal/bind/util/ISO8601Utils_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Gson_5/b120_r1`
