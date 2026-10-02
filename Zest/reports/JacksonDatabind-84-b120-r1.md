# Zest – JacksonDatabind-84 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 360816 (valid 95.43%) |
| Cycles | 39 |
| Corpus | 26 |
| Zest branch coverage (total / valid) | 169 / 166 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 26 |
| Failing on fixed | 0 |
| Failing on buggy | 3 |
| Bug detection | Fail (triggering: 3) |
| **Fault detected** | **yes** |
| Line coverage | 26 / 33 = 78.79% |
| Branch coverage | 6 / 16 = 37.50% |
| Test suite length (statements) | 87 |
| Mutation score | 15 / 22 = 68.18% |
| Fuzz time | 126s |
| Pipeline time | 238s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.type.ResolvedRecursiveType_Zest_Test::test15
- com.fasterxml.jackson.databind.type.ResolvedRecursiveType_Zest_Test::test16
- com.fasterxml.jackson.databind.type.ResolvedRecursiveType_Zest_Test::test3

```
--- com.fasterxml.jackson.databind.type.ResolvedRecursiveType_Zest_Test::test15
junit.framework.AssertionFailedError: expected:<0=[null |this=com.fasterxml.jackson.databind.type.ResolvedRecursiveType:[recursive type; UNRESOLVED
1=!java.lang.NullPointer]Exception
> but was:<0=[!H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.type.ResolvedRecursiveType_Zest_Test.test15(ResolvedRecursiveType_Zest_Test.java:112)
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
--- com.fasterxml.jackson.databind.type.ResolvedRecursiveType_Zest_Test::test16
junit.framework.AssertionFailedError: expected:<0=[null |this=com.fasterxml.jackson.databind.type.ResolvedRecursiveType:[recursive type; UNRESOLVED
1=null |this=com.fasterxml.jackson.databind.type.ResolvedRecursiveType:[recursive type; UNRESOLVED]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.type.ResolvedRecursiveType_Zest_Test.test16(ResolvedRecursiveType_Zest_Test.java:119)
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

- Suite: `/home/user/suites/JacksonDatabind/zest/84/t12001/JacksonDatabind-84f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_84/b120_r1/src/com/fasterxml/jackson/databind/type/ResolvedRecursiveType_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_84/b120_r1`
