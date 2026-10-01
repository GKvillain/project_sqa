# Zest – Codec-16 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 107808 (valid 73.21%) |
| Cycles | 6 |
| Corpus | 58 |
| Zest branch coverage (total / valid) | 223 / 220 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 58 |
| Failing on fixed | 0 |
| Failing on buggy | 4 |
| Bug detection | Fail (triggering: 4) |
| **Fault detected** | **yes** |
| Line coverage | 44 / 166 = 26.51% |
| Branch coverage | 20 / 75 = 26.67% |
| Test suite length (statements) | 58 |
| Mutation score | 559 / 912 = 61.29% |
| Fuzz time | 120s |
| Pipeline time | 223s |

## Failing tests on buggy version

- org.apache.commons.codec.binary.Base32_Zest_Test::test21
- org.apache.commons.codec.binary.Base32_Zest_Test::test25
- org.apache.commons.codec.binary.Base32_Zest_Test::test51
- org.apache.commons.codec.binary.Base32_Zest_Test::test53

```
--- org.apache.commons.codec.binary.Base32_Zest_Test::test21
junit.framework.AssertionFailedError: expected:<0=[new org.apache.commons.codec.binary.Base32:@? |a1=[B:[[-54, -11, -27, 92, 87, -38, 21, 106]]]
> but was:<0=[!java.lang.IllegalArgumentException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.binary.Base32_Zest_Test.test21(Base32_Zest_Test.java:154)
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
--- org.apache.commons.codec.binary.Base32_Zest_Test::test25
junit.framework.AssertionFailedError: expected:<0=[new org.apache.commons.codec.binary.Base32:@? |a1=[B:[[-54, -11, -27, 92, 87, 36, -43, -94]]]
> but was:<0=[!java.lang.IllegalArgumentException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.binary.Base32_Zest_Test.test25(Base32_Zest_Test.java:182)
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
--- org.apache.commons.codec.binary.Base32_Zest_Test::test51
junit.framework.AssertionFailedError: expected:<0=[new org.apache.commons.codec.binary.Base32:@? |a1=[B:[[-54, -11, -27, 92, 87, 36, -43]]
```

- Suite: `/home/pilaiphon/suites/Codec/zest/16/t12001/Codec-16f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_16/b120_r1/src/org/apache/commons/codec/binary/Base32_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_16/b120_r1`
