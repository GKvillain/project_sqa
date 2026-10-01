# Zest – Codec-9 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 116821 (valid 93.11%) |
| Cycles | 4 |
| Corpus | 129 |
| Zest branch coverage (total / valid) | 346 / 344 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 129 |
| Failing on fixed | 0 |
| Failing on buggy | 2 |
| Bug detection | Fail (triggering: 2) |
| **Fault detected** | **yes** |
| Line coverage | 171 / 220 = 77.73% |
| Branch coverage | 112 / 154 = 72.73% |
| Test suite length (statements) | 129 |
| Mutation score | 597 / 966 = 61.80% |
| Fuzz time | 120s |
| Pipeline time | 231s |

## Failing tests on buggy version

- org.apache.commons.codec.binary.Base64_Zest_Test::test111
- org.apache.commons.codec.binary.Base64_Zest_Test::test27

```
--- org.apache.commons.codec.binary.Base64_Zest_Test::test111
junit.framework.AssertionFailedError: expected:<0=[[B:[[100, 81, 90, 115, 65, 88, 74, 111, 65, 88, 103]] |a0=[B:[[117, 6, 108, 1, 114, 104, 1, 120]]
1=[B:[[100, 81, 90, 115, 65, 88, 74, 111, 65, 88, 103, 61]] |a0=[B:[[117, 6, 108, 1, 114, 104, 1, 120]]]
> but was:<0=[!java.lang.IllegalArgumentException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.binary.Base64_Zest_Test.test111(Base64_Zest_Test.java:784)
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
--- org.apache.commons.codec.binary.Base64_Zest_Test::test27
junit.framework.AssertionFailedError: expected:<0=[[B:[[47, 73, 110, 72, 66, 97, 119, 61]] |a0=[B:[[-4, -119, -57, 5, -84]]]
> but was:<0=[!java.lang.IllegalArgumentException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.binary.Base64_Zest_Test.test27(Base64_Zest_Test.java:196)
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

- Suite: `/home/pilaiphon/suites/Codec/zest/9/t12001/Codec-9f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_9/b120_r1/src/org/apache/commons/codec/binary/Base64_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_9/b120_r1`
