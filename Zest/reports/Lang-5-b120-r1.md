# Zest – Lang-5 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 282269 (valid 1.64%) |
| Cycles | 53 |
| Corpus | 15 |
| Zest branch coverage (total / valid) | 59 / 50 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 15 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 17 / 96 = 17.71% |
| Branch coverage | 13 / 70 = 18.57% |
| Test suite length (statements) | 32 |
| Mutation score | 159 / 177 = 89.83% |
| Fuzz time | 120s |
| Pipeline time | 89s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Lang/zest/5/t12001/Lang-5f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Lang_5/b120_r1/src/org/apache/commons/lang3/LocaleUtils_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Lang_5/b120_r1`
