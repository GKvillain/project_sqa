# Zest – JacksonDatabind-81 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 345892 (valid 72.63%) |
| Cycles | 21 |
| Corpus | 60 |
| Zest branch coverage (total / valid) | 241 / 238 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 60 |
| Failing on fixed | 0 |
| Failing on buggy | 20 |
| Bug detection | Fail (triggering: 20) |
| **Fault detected** | **yes** |
| Line coverage | 38 / 551 = 6.90% |
| Branch coverage | 21 / 391 = 5.37% |
| Test suite length (statements) | 60 |
| Mutation score | 358 / 378 = 94.71% |
| Fuzz time | 125s |
| Pipeline time | 251s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test12
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test20
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test21
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test23
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test24
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test26
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test27
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test28
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test29
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test30
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test33
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test41
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test48
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test49
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test52
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test53
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test55
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test56
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test2
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test9

```
--- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test12
junit.framework.AssertionFailedError: expected:<0=![java.lang.NullPointer]Exception
> but was:<0=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test.test12(JacksonAnnotationIntrospector_Zest_Test.java:91)
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
--- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test20
junit.framework.AssertionFailedError: expected:<0=[java.lang.Boolean:false |this=com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector:@?]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test.test20(JacksonAnnotationIntrospector_Zest_Test.java:147)
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
--- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test21
junit.framework.AssertionFailedError: expected:<0=[java.lang.Boolean:false |this=com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector:@?]
```

- Suite: `/home/user/suites/JacksonDatabind/zest/81/t12001/JacksonDatabind-81f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_81/b120_r1/src/com/fasterxml/jackson/databind/introspect/JacksonAnnotationIntrospector_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_81/b120_r1`
