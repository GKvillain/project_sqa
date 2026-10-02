# Zest – JacksonDatabind-41 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 327628 (valid 82.34%) |
| Cycles | 29 |
| Corpus | 63 |
| Zest branch coverage (total / valid) | 482 / 479 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 63 |
| Failing on fixed | 0 |
| Failing on buggy | 5 |
| Bug detection | Fail (triggering: 5) |
| **Fault detected** | **yes** |
| Line coverage | 149 / 343 = 43.44% |
| Branch coverage | 90 / 217 = 41.47% |
| Test suite length (statements) | 80 |
| Mutation score | 173 / 235 = 73.62% |
| Fuzz time | 128s |
| Pipeline time | 225s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test25
- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test42
- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test47
- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test48
- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test9

```
--- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test25
junit.framework.AssertionFailedError: expected:<0=[com.fasterxml.jackson.databind.type.SimpleType:[simple type, class java.lang.Comparable] |this=com.fasterxml.jackson.databind.type.TypeFactory:@?]
> but was:<0=[!java.lang.IllegalArgumentException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test.test25(TypeFactory_Zest_Test.java:182)
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
--- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test42
junit.framework.AssertionFailedError: expected:<0=[com.fasterxml.jackson.databind.type.CollectionType:[collection type; class java.util.ArrayList, contains [simple type, class java.lang.Object]] |this=com.fasterxml.jackson.databind.type.TypeFactory:@?
1=!java.lang.NullPointer]Exception
> but was:<0=[!java.lang.IllegalArgument]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test.test42(TypeFactory_Zest_Test.java:301)
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
--- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test47
```

- Suite: `/home/user/suites/JacksonDatabind/zest/41/t12001/JacksonDatabind-41f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_41/b120_r1/src/com/fasterxml/jackson/databind/type/TypeFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_41/b120_r1`
