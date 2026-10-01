# Zest – Codec-7 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 107199 (valid 90.13%) |
| Cycles | 4 |
| Corpus | 137 |
| Zest branch coverage (total / valid) | 364 / 362 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 137 |
| Failing on fixed | 0 |
| Failing on buggy | 9 |
| Bug detection | Fail (triggering: 9) |
| **Fault detected** | **yes** |
| Line coverage | 189 / 233 = 81.12% |
| Branch coverage | 118 / 162 = 72.84% |
| Test suite length (statements) | 137 |
| Mutation score | 594 / 990 = 60.00% |
| Fuzz time | 121s |
| Pipeline time | 233s |

## Failing tests on buggy version

- org.apache.commons.codec.binary.Base64_Zest_Test::test104
- org.apache.commons.codec.binary.Base64_Zest_Test::test114
- org.apache.commons.codec.binary.Base64_Zest_Test::test13
- org.apache.commons.codec.binary.Base64_Zest_Test::test32
- org.apache.commons.codec.binary.Base64_Zest_Test::test65
- org.apache.commons.codec.binary.Base64_Zest_Test::test66
- org.apache.commons.codec.binary.Base64_Zest_Test::test80
- org.apache.commons.codec.binary.Base64_Zest_Test::test81
- org.apache.commons.codec.binary.Base64_Zest_Test::test90

```
--- org.apache.commons.codec.binary.Base64_Zest_Test::test104
junit.framework.AssertionFailedError: expected:<...ava.lang.String:0A==[ |a0=[B:[[-48]]
1=java.lang.String:0A==] |a0=[B:[[-48]]
> but was:<...ava.lang.String:0A==[
 |a0=[B:[[-48]]
1=java.lang.String:0A==
] |a0=[B:[[-48]]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.binary.Base64_Zest_Test.test104(Base64_Zest_Test.java:735)
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
--- org.apache.commons.codec.binary.Base64_Zest_Test::test114
junit.framework.AssertionFailedError: expected:<...lang.String:0ESfcN2H[ |a0=[B:[[-48, 68, -97, 112, -35, -121]]
1=java.lang.String:0ESfcN2H] |a0=[B:[[-48, 68, -...> but was:<...lang.String:0ESfcN2H[
 |a0=[B:[[-48, 68, -97, 112, -35, -121]]
1=java.lang.String:0ESfcN2H
] |a0=[B:[[-48, 68, -...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.binary.Base64_Zest_Test.test114(Base64_Zest_Test.java:805)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
```

- Suite: `/home/pilaiphon/suites/Codec/zest/7/t12002/Codec-7f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_7/b120_r2/src/org/apache/commons/codec/binary/Base64_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_7/b120_r2`
