# Zest – Lang-4 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 201199 (valid 42.01%) |
| Cycles | 27 |
| Corpus | 24 |
| Zest branch coverage (total / valid) | 203 / 194 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 24 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 15 / 25 = 60.00% |
| Branch coverage | 6 / 14 = 42.86% |
| Test suite length (statements) | 24 |
| Mutation score | 24 / 41 = 58.54% |
| Fuzz time | 120s |
| Pipeline time | 271s |

## Failing tests on buggy version

(none)

- Suite: `/home/pilaiphon/suites/Lang/zest/4/t12001/Lang-4f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Lang_4/b120_r1/src/org/apache/commons/lang3/text/translate/LookupTranslator_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Lang_4/b120_r1`
