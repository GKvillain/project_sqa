# Zest – JacksonDatabind-36 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 372235 (valid 78.57%) |
| Cycles | 30 |
| Corpus | 107 |
| Zest branch coverage (total / valid) | 562 / 560 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 107 |
| Failing on fixed | 0 |
| Failing on buggy | 5 |
| Bug detection | Fail (triggering: 5) |
| **Fault detected** | **yes** |
| Line coverage | 108 / 200 = 54.00% |
| Branch coverage | 57 / 134 = 42.54% |
| Test suite length (statements) | 107 |
| Mutation score | 256 / 364 = 70.33% |
| Fuzz time | 128s |
| Pipeline time | 253s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.util.StdDateFormat_Zest_Test::test26
- com.fasterxml.jackson.databind.util.StdDateFormat_Zest_Test::test39
- com.fasterxml.jackson.databind.util.StdDateFormat_Zest_Test::test48
- com.fasterxml.jackson.databind.util.StdDateFormat_Zest_Test::test58
- com.fasterxml.jackson.databind.util.StdDateFormat_Zest_Test::test65

```
--- com.fasterxml.jackson.databind.util.StdDateFormat_Zest_Test::test26
junit.framework.AssertionFailedError: expected:<0=[void |this=com.fasterxml.jackson.databind.util.StdDateFormat:DateFormat com.fasterxml.jackson.databind.util.StdDateFormat(locale: en_US)]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.util.StdDateFormat_Zest_Test.test26(StdDateFormat_Zest_Test.java:189)
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
--- com.fasterxml.jackson.databind.util.StdDateFormat_Zest_Test::test39
junit.framework.AssertionFailedError: expected:<0=[void |this=com.fasterxml.jackson.databind.util.StdDateFormat:DateFormat com.fasterxml.jackson.databind.util.StdDateFormat(locale: en_US)
1=void |this=com.fasterxml.jackson.databind.util.StdDateFormat:DateFormat com.fasterxml.jackson.databind.util.StdDateFormat(locale: en_US)]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.util.StdDateFormat_Zest_Test.test39(StdDateFormat_Zest_Test.java:280)
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
--- com.fasterxml.jackson.databind.util.StdDateFormat_Zest_Test::test48
```

- Suite: `/home/user/suites/JacksonDatabind/zest/36/t12001/JacksonDatabind-36f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_36/b120_r1/src/com/fasterxml/jackson/databind/util/StdDateFormat_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_36/b120_r1`
