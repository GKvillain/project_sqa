# Zest – JacksonDatabind-79 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 399243 (valid 68.71%) |
| Cycles | 28 |
| Corpus | 46 |
| Zest branch coverage (total / valid) | 238 / 235 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 46 |
| Failing on fixed | 0 |
| Failing on buggy | 11 |
| Bug detection | Fail (triggering: 11) |
| **Fault detected** | **yes** |
| Line coverage | 53 / 834 = 6.35% |
| Branch coverage | 16 / 562 = 2.85% |
| Test suite length (statements) | 46 |
| Mutation score | 603 / 623 = 96.79% |
| Fuzz time | 126s |
| Pipeline time | 237s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test10
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test18
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test22
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test23
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test24
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test28
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test29
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test30
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test33
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test2
- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test4

```
--- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<0=[com.fasterxml.jackson.databind.introspect.ObjectIdInfo:ObjectIdInfo: propName=, scope=java.lang.Object, generatorType=null, alwaysAsId=false
1=com.fasterxml.jackson.databind.introspect.ObjectIdInfo:ObjectIdInfo: propName=, scope=java.lang.Object, generatorType=null, alwaysAsId=false]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test.test10(JacksonAnnotationIntrospector_Zest_Test.java:77)
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
--- com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test::test18
junit.framework.AssertionFailedError: expected:<0=[com.fasterxml.jackson.databind.introspect.ObjectIdInfo:ObjectIdInfo: propName=, scope=java.lang.Object, generatorType=null, alwaysAsId=false
1=!java.lang.NullPointer]Exception
> but was:<0=[!H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector_Zest_Test.test18(JacksonAnnotationIntrospector_Zest_Test.java:133)
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

- Suite: `/home/user/suites/JacksonDatabind/zest/79/t12001/JacksonDatabind-79f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_79/b120_r1/src/com/fasterxml/jackson/databind/introspect/JacksonAnnotationIntrospector_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_79/b120_r1`
