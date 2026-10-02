# Zest – Time-7 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 342133 (valid 65.52%) |
| Cycles | 36 |
| Corpus | 17 |
| Zest branch coverage (total / valid) | 138 / 135 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 17 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 26 / 208 = 12.50% |
| Branch coverage | 3 / 88 = 3.41% |
| Test suite length (statements) | 17 |
| Mutation score | 219 / 219 = 100.00% |
| Fuzz time | 125s |
| Pipeline time | 236s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Time/zest/7/t12001/Time-7f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Time_7/b120_r1/src/org/joda/time/format/DateTimeFormatter_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Time_7/b120_r1`
