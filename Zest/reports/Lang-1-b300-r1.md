# Zest – Lang-1 (budget 300s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.NumberUtilsFuzz#createNumber` |
| Executions | 539647 (valid 76.14%) |
| Cycles | 47 |
| Corpus | 60 |
| Zest branch coverage (total / valid) | 172 / 167 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 60 |
| Failing on fixed | 0 |
| Failing on buggy | 6 |
| Bug detection | Fail (triggering: 6) |
| **Fault detected** | **yes** |
| Line coverage | 136 / 380 = 35.79% |
| Branch coverage | 115 / 350 = 32.86% |
| Test suite length (statements) | 214 |
| Mutation score | Lang,1f,zest,30001,940,0,940,784 / 940,Lang,1f,zest,30001,940,0,940 = 0.00% |
| Fuzz time | 299s |
| Pipeline time | 145s |

## Failing tests on buggy version

- org.apache.commons.lang3.math.NumberUtils_Zest_Test::test18
- org.apache.commons.lang3.math.NumberUtils_Zest_Test::test30
- org.apache.commons.lang3.math.NumberUtils_Zest_Test::test37
- org.apache.commons.lang3.math.NumberUtils_Zest_Test::test50
- org.apache.commons.lang3.math.NumberUtils_Zest_Test::test51
- org.apache.commons.lang3.math.NumberUtils_Zest_Test::test59

```
--- org.apache.commons.lang3.math.NumberUtils_Zest_Test::test18
java.lang.NumberFormatException: For input string: "Ec1d7BAFfa9Ba2e5"
	at java.base/java.lang.NumberFormatException.forInputString(NumberFormatException.java:65)
	at java.base/java.lang.Long.parseLong(Long.java:692)
	at java.base/java.lang.Long.valueOf(Long.java:1117)
	at java.base/java.lang.Long.decode(Long.java:1268)
	at org.apache.commons.lang3.math.NumberUtils.createLong(NumberUtils.java:701)
	at org.apache.commons.lang3.math.NumberUtils.createNumber(NumberUtils.java:472)
	at org.apache.commons.lang3.math.NumberUtils_Zest_Test.test18(NumberUtils_Zest_Test.java:127)
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
--- org.apache.commons.lang3.math.NumberUtils_Zest_Test::test30
java.lang.NumberFormatException: For input string: "-C5Ad2CF9"
	at java.base/java.lang.NumberFormatException.forInputString(NumberFormatException.java:65)
	at java.base/java.lang.Integer.parseInt(Integer.java:652)
	at java.base/java.lang.Integer.valueOf(Integer.java:957)
	at java.base/java.lang.Integer.decode(Integer.java:1436)
	at org.apache.commons.lang3.math.NumberUtils.createInteger(NumberUtils.java:684)
	at org.apache.commons.lang3.math.NumberUtils.createNumber(NumberUtils.java:474)
	at org.apache.commons.lang3.math.NumberUtils_Zest_Test.test30(NumberUtils_Zest_Test.java:201)
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
```

- Suite: `/home/pilaiphon/suites/Lang/zest/1/t30001/Lang-1f-zest.30001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Lang_1/b300_r1/src/org/apache/commons/lang3/math/NumberUtils_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Lang_1/b300_r1`
