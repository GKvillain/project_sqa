# Zest – JacksonCore-14 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 449451 (valid 54.86%) |
| Cycles | 23 |
| Corpus | 43 |
| Zest branch coverage (total / valid) | 196 / 193 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 43 |
| Failing on fixed | 0 |
| Failing on buggy | 4 |
| Bug detection | Fail (triggering: 4) |
| **Fault detected** | **yes** |
| Line coverage | 33 / 68 = 48.53% |
| Branch coverage | 8 / 22 = 36.36% |
| Test suite length (statements) | 43 |
| Mutation score | 36 / 52 = 69.23% |
| Fuzz time | 128s |
| Pipeline time | 192s |

## Failing tests on buggy version

- com.fasterxml.jackson.core.io.IOContext_Zest_Test::test10
- com.fasterxml.jackson.core.io.IOContext_Zest_Test::test20
- com.fasterxml.jackson.core.io.IOContext_Zest_Test::test21
- com.fasterxml.jackson.core.io.IOContext_Zest_Test::test29

```
--- com.fasterxml.jackson.core.io.IOContext_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<0=[void |a0=[B:[[7, -48, -88, 122, 22, -18, -66, 16]] |a1=[B:[[-98, 108, 104, 36, -100, 66, 95, 93]] |this=com.fasterxml.jackson.core.io.IOContext:@?
1=void |a0=[B:[[-77, 14, 121, -85, -21, -18, 95, 116]] |a1=[B:[[7, -48, -88, 122, 22, -18, -66, 16]] |this=com.fasterxml.jackson.core.io.IOContext:@?]
> but was:<0=[!java.lang.IllegalArgumentException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.core.io.IOContext_Zest_Test.test10(IOContext_Zest_Test.java:77)
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
--- com.fasterxml.jackson.core.io.IOContext_Zest_Test::test20
junit.framework.AssertionFailedError: expected:<...g to release buffer [smaller than original] |this=com.fasterxml...> but was:<...g to release buffer [not owned by the context] |this=com.fasterxml...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.core.io.IOContext_Zest_Test.test20(IOContext_Zest_Test.java:147)
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
--- com.fasterxml.jackson.core.io.IOContext_Zest_Test::test21
junit.framework.AssertionFailedError: expected:<0=[void |a0=[B:[[7, -123, 102, 122, 22, -18, -66, -41]] |a1=[B:[[-98, 108, 104, 36, -100, 66, 95, 93]] |this=com.fasterxml.jackson.core.io.IOContext:@?
1=void |a0=[C:[[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?]] |a1=[C:[[퐷, ⺵, ⎖, 禪, 聆, 뀞, ᳮ, ⼇, 蕤, 냓, に, 璱, ꄦ, 詶, ꑦ]] |this=com.fasterxml.jackson.core.io.IOContext:@?]
```

- Suite: `/home/user/suites/JacksonCore/zest/14/t12001/JacksonCore-14f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_14/b120_r1/src/com/fasterxml/jackson/core/io/IOContext_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_14/b120_r1`
