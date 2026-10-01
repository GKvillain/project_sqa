# Zest – Lang-5 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 271251 (valid 1.32%) |
| Cycles | 40 |
| Corpus | 27 |
| Zest branch coverage (total / valid) | 70 / 55 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 27 |
| Failing on fixed | 0 |
| Failing on buggy | 1 |
| Bug detection | Fail (triggering: 1) |
| **Fault detected** | **yes** |
| Line coverage | 28 / 96 = 29.17% |
| Branch coverage | 24 / 70 = 34.29% |
| Test suite length (statements) | 58 |
| Mutation score | 150 / 177 = 84.75% |
| Fuzz time | 120s |
| Pipeline time | 94s |

## Failing tests on buggy version

- org.apache.commons.lang3.LocaleUtils_Zest_Test::test25

```
--- org.apache.commons.lang3.LocaleUtils_Zest_Test::test25
java.lang.IllegalArgumentException: Invalid locale format: _PṤ
	at org.apache.commons.lang3.LocaleUtils.toLocale(LocaleUtils.java:99)
	at org.apache.commons.lang3.LocaleUtils_Zest_Test.test25(LocaleUtils_Zest_Test.java:134)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.internal.runners.statements.FailOnTimeout$CallableStatement.call(FailOnTimeout.java:298)
	at org.junit.internal.runners.statements.FailOnTimeout$CallableStatement.call(FailOnTimeout.java:292)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at java.base/java.lang.Thread.run(Thread.java:829)
```

- Suite: `/home/pilaiphon/suites/Lang/zest/5/t12002/Lang-5f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Lang_5/b120_r2/src/org/apache/commons/lang3/LocaleUtils_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Lang_5/b120_r2`
