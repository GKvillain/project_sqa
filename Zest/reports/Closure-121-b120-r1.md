# Zest – Closure-121 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 437618 (valid 62.07%) |
| Cycles | 104 |
| Corpus | 10 |
| Zest branch coverage (total / valid) | 171 / 163 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 10 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 23 / 299 = 7.69% |
| Branch coverage | 3 / 240 = 1.25% |
| Test suite length (statements) | 10 |
| Mutation score | 410 / 410 = 100.00% |
| Fuzz time | 131s |
| Pipeline time | 310s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/121/t12001/Closure-121f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_121/b120_r1/src/com/google/javascript/jscomp/InlineVariables_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_121/b120_r1`
