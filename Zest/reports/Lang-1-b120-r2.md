# Zest – Lang-1 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.NumberUtilsFuzz#createNumber` |
| Executions | 217567 (valid 75.48%) |
| Cycles | 19 |
| Corpus | 56 |
| Zest branch coverage (total / valid) | 173 / 168 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 56 |
| Failing on fixed | 0 |
| Failing on buggy | 5 |
| Bug detection | Fail (triggering: 5) |
| **Fault detected** | **yes** |
| Line coverage | 136 / 380 = 35.79% |
| Branch coverage | 115 / 350 = 32.86% |
| Test suite length (statements) | 204 |
| Mutation score | 783 / 940 = 83.30% |
| Fuzz time | 120s |
| Pipeline time | 209s |

## Failing tests on buggy version

- org.apache.commons.lang3.math.NumberUtils_Zest_Test::test36
- org.apache.commons.lang3.math.NumberUtils_Zest_Test::test38
- org.apache.commons.lang3.math.NumberUtils_Zest_Test::test39
- org.apache.commons.lang3.math.NumberUtils_Zest_Test::test44
- org.apache.commons.lang3.math.NumberUtils_Zest_Test::test52

```
--- org.apache.commons.lang3.math.NumberUtils_Zest_Test::test36
java.lang.NumberFormatException: For input string: "a59EcEF4e5CDc1b5"
	at java.base/java.lang.NumberFormatException.forInputString(NumberFormatException.java:65)
	at java.base/java.lang.Long.parseLong(Long.java:692)
	at java.base/java.lang.Long.valueOf(Long.java:1117)
	at java.base/java.lang.Long.decode(Long.java:1268)
	at org.apache.commons.lang3.math.NumberUtils.createLong(NumberUtils.java:701)
	at org.apache.commons.lang3.math.NumberUtils.createNumber(NumberUtils.java:472)
	at org.apache.commons.lang3.math.NumberUtils_Zest_Test.test36(NumberUtils_Zest_Test.java:243)
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
--- org.apache.commons.lang3.math.NumberUtils_Zest_Test::test38
junit.framework.AssertionFailedError: expected:<java.lang.[Integer]> but was:<java.lang.[Long]>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.lang3.math.NumberUtils_Zest_Test.test38(NumberUtils_Zest_Test.java:259)
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
--- org.apache.commons.lang3.math.NumberUtils_Zest_Test::test39
java.lang.NumberFormatException: For input string: "E224aee5"
```

- Suite: `/home/pilaiphon/suites/Lang/zest/1/t12002/Lang-1f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Lang_1/b120_r2/src/org/apache/commons/lang3/math/NumberUtils_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Lang_1/b120_r2`
