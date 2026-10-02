# Zest – JacksonXml-5 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 432130 (valid 50.18%) |
| Cycles | 31 |
| Corpus | 24 |
| Zest branch coverage (total / valid) | 180 / 177 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 24 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 25 / 96 = 26.04% |
| Branch coverage | 6 / 48 = 12.50% |
| Test suite length (statements) | 24 |
| Mutation score | 59 / 63 = 93.65% |
| Fuzz time | 127s |
| Pipeline time | 190s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/JacksonXml/zest/5/t12001/JacksonXml-5f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonXml_5/b120_r1/src/com/fasterxml/jackson/dataformat/xml/ser/XmlSerializerProvider_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonXml_5/b120_r1`
