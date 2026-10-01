# Zest – Codec-4 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 117490 (valid 91.39%) |
| Cycles | 6 |
| Corpus | 116 |
| Zest branch coverage (total / valid) | 343 / 341 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 116 |
| Failing on fixed | 0 |
| Failing on buggy | 14 |
| Bug detection | Fail (triggering: 14) |
| **Fault detected** | **yes** |
| Line coverage | 171 / 231 = 74.03% |
| Branch coverage | 101 / 158 = 63.92% |
| Test suite length (statements) | 116 |
| Mutation score | 627 / 977 = 64.18% |
| Fuzz time | 120s |
| Pipeline time | 228s |

## Failing tests on buggy version

- org.apache.commons.codec.binary.Base64_Zest_Test::test100
- org.apache.commons.codec.binary.Base64_Zest_Test::test103
- org.apache.commons.codec.binary.Base64_Zest_Test::test111
- org.apache.commons.codec.binary.Base64_Zest_Test::test114
- org.apache.commons.codec.binary.Base64_Zest_Test::test24
- org.apache.commons.codec.binary.Base64_Zest_Test::test30
- org.apache.commons.codec.binary.Base64_Zest_Test::test37
- org.apache.commons.codec.binary.Base64_Zest_Test::test38
- org.apache.commons.codec.binary.Base64_Zest_Test::test41
- org.apache.commons.codec.binary.Base64_Zest_Test::test64
- org.apache.commons.codec.binary.Base64_Zest_Test::test78
- org.apache.commons.codec.binary.Base64_Zest_Test::test84
- org.apache.commons.codec.binary.Base64_Zest_Test::test91
- org.apache.commons.codec.binary.Base64_Zest_Test::test93

```
--- org.apache.commons.codec.binary.Base64_Zest_Test::test100
junit.framework.AssertionFailedError: expected:<..., 77, 50, 97, 99, 61[]] |a0=[B:[[-52, -52, -48, -84, -1, 76, -39, -89]] |this=org.apache.commons.codec.binary.Base64:@?
1=[B:[[122, 77, 122, 81, 114, 80, 57, 77, 50, 97, 99, 61]]] |a0=[B:[[-52, -52...> but was:<..., 77, 50, 97, 99, 61[, 13, 10]] |a0=[B:[[-52, -52, -48, -84, -1, 76, -39, -89]] |this=org.apache.commons.codec.binary.Base64:@?
1=[B:[[122, 77, 122, 81, 114, 80, 57, 77, 50, 97, 99, 61, 13, 10]]] |a0=[B:[[-52, -52...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.binary.Base64_Zest_Test.test100(Base64_Zest_Test.java:707)
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
--- org.apache.commons.codec.binary.Base64_Zest_Test::test103
junit.framework.AssertionFailedError: expected:<... 52, 100, 81, 61, 61[]] |a0=[B:[[28, -20, 23, 1, 76, 120, 117]] |this=org.apache.commons.codec.binary.Base64:@?
1=[B:[[72, 79, 119, 88, 65, 85, 120, 52, 100, 81, 61, 61]]] |a0=[B:[[28, -20,...> but was:<... 52, 100, 81, 61, 61[, 13, 10]] |a0=[B:[[28, -20, 23, 1, 76, 120, 117]] |this=org.apache.commons.codec.binary.Base64:@?
1=[B:[[72, 79, 119, 88, 65, 85, 120, 52, 100, 81, 61, 61, 13, 10]]] |a0=[B:[[28, -20,...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.binary.Base64_Zest_Test.test103(Base64_Zest_Test.java:728)
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
--- org.apache.commons.codec.binary.Base64_Zest_Test::test111
junit.framework.AssertionFailedError: expected:<...=[B:[[72, 70, 52, 86[]] |a0=[B:[[28, 94, 21]] |this=org.apache.commons.codec.binary.Base64:@?
```

- Suite: `/home/pilaiphon/suites/Codec/zest/4/t12002/Codec-4f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_4/b120_r2/src/org/apache/commons/codec/binary/Base64_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_4/b120_r2`
