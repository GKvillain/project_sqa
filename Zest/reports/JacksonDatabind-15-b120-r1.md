# Zest – JacksonDatabind-15 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 444463 (valid 63.91%) |
| Cycles | 40 |
| Corpus | 23 |
| Zest branch coverage (total / valid) | 150 / 147 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 23 |
| Failing on fixed | 0 |
| Failing on buggy | 11 |
| Bug detection | Fail (triggering: 11) |
| **Fault detected** | **yes** |
| Line coverage | 34 / 779 = 4.36% |
| Branch coverage | 3 / 494 = 0.61% |
| Test suite length (statements) | 23 |
| Mutation score | 629 / 634 = 99.21% |
| Fuzz time | 128s |
| Pipeline time | 225s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.JavaType_Zest_Test::test10
- com.fasterxml.jackson.databind.JavaType_Zest_Test::test11
- com.fasterxml.jackson.databind.JavaType_Zest_Test::test12
- com.fasterxml.jackson.databind.JavaType_Zest_Test::test13
- com.fasterxml.jackson.databind.JavaType_Zest_Test::test15
- com.fasterxml.jackson.databind.JavaType_Zest_Test::test16
- com.fasterxml.jackson.databind.JavaType_Zest_Test::test17
- com.fasterxml.jackson.databind.JavaType_Zest_Test::test18
- com.fasterxml.jackson.databind.JavaType_Zest_Test::test19
- com.fasterxml.jackson.databind.JavaType_Zest_Test::test20
- com.fasterxml.jackson.databind.JavaType_Zest_Test::test22

```
--- com.fasterxml.jackson.databind.JavaType_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<0=[void |this=com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer:@?
1=]!java.lang.NullPoint...> but was:<0=[]!java.lang.NullPoint...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.JavaType_Zest_Test.test10(JavaType_Zest_Test.java:77)
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
--- com.fasterxml.jackson.databind.JavaType_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<0=[void |this=com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer:@?
1=]!java.lang.NullPoint...> but was:<0=[]!java.lang.NullPoint...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.JavaType_Zest_Test.test11(JavaType_Zest_Test.java:84)
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
--- com.fasterxml.jackson.databind.JavaType_Zest_Test::test12
junit.framework.AssertionFailedError: expected:<0=[void |this=com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer:@?
1=]!java.lang.NullPoint...> but was:<0=[]!java.lang.NullPoint...>
	at org.junit.Assert.assertEquals(Assert.java:115)
```

- Suite: `/home/user/suites/JacksonDatabind/zest/15/t12001/JacksonDatabind-15f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_15/b120_r1/src/com/fasterxml/jackson/databind/JavaType_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_15/b120_r1`
