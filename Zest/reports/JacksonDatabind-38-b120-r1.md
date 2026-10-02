# Zest – JacksonDatabind-38 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 437511 (valid 52.69%) |
| Cycles | 28 |
| Corpus | 36 |
| Zest branch coverage (total / valid) | 197 / 194 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 36 |
| Failing on fixed | 0 |
| Failing on buggy | 21 |
| Bug detection | Fail (triggering: 21) |
| **Fault detected** | **yes** |
| Line coverage | 42 / 137 = 30.66% |
| Branch coverage | 14 / 54 = 25.93% |
| Test suite length (statements) | 37 |
| Mutation score | 71 / 101 = 70.30% |
| Fuzz time | 127s |
| Pipeline time | 232s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test15
- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test16
- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test21
- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test22
- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test24
- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test25
- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test26
- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test27
- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test28
- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test29
- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test30
- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test31
- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test32
- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test33
- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test35
- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test0
- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test1
- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test2
- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test3
- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test5
- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test8

```
--- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test15
junit.framework.AssertionFailedError: expected:<0=[com.fasterxml.jackson.databind.type.SimpleType:[simple type, class java.lang.Long]
1=!java.lang.NullPointer]Exception
> but was:<0=[!H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.type.CollectionType_Zest_Test.test15(CollectionType_Zest_Test.java:112)
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
--- com.fasterxml.jackson.databind.type.CollectionType_Zest_Test::test16
junit.framework.AssertionFailedError: expected:<0=[com.fasterxml.jackson.databind.type.SimpleType:[simple type, class [Ljava.lang.String;]
1=!java.lang.NullPointer]Exception
> but was:<0=[!H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.type.CollectionType_Zest_Test.test16(CollectionType_Zest_Test.java:119)
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

- Suite: `/home/user/suites/JacksonDatabind/zest/38/t12001/JacksonDatabind-38f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_38/b120_r1/src/com/fasterxml/jackson/databind/type/CollectionType_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_38/b120_r1`
