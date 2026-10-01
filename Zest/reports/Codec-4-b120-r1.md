# Zest – Codec-4 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 117122 (valid 87.56%) |
| Cycles | 5 |
| Corpus | 133 |
| Zest branch coverage (total / valid) | 343 / 341 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 133 |
| Failing on fixed | 0 |
| Failing on buggy | 13 |
| Bug detection | Fail (triggering: 13) |
| **Fault detected** | **yes** |
| Line coverage | 171 / 231 = 74.03% |
| Branch coverage | 101 / 158 = 63.92% |
| Test suite length (statements) | 133 |
| Mutation score | 613 / 977 = 62.74% |
| Fuzz time | 120s |
| Pipeline time | 248s |

## Failing tests on buggy version

- org.apache.commons.codec.binary.Base64_Zest_Test::test111
- org.apache.commons.codec.binary.Base64_Zest_Test::test112
- org.apache.commons.codec.binary.Base64_Zest_Test::test115
- org.apache.commons.codec.binary.Base64_Zest_Test::test24
- org.apache.commons.codec.binary.Base64_Zest_Test::test30
- org.apache.commons.codec.binary.Base64_Zest_Test::test52
- org.apache.commons.codec.binary.Base64_Zest_Test::test53
- org.apache.commons.codec.binary.Base64_Zest_Test::test62
- org.apache.commons.codec.binary.Base64_Zest_Test::test68
- org.apache.commons.codec.binary.Base64_Zest_Test::test73
- org.apache.commons.codec.binary.Base64_Zest_Test::test77
- org.apache.commons.codec.binary.Base64_Zest_Test::test91
- org.apache.commons.codec.binary.Base64_Zest_Test::test93

```
--- org.apache.commons.codec.binary.Base64_Zest_Test::test111
junit.framework.AssertionFailedError: expected:<...=[B:[[87, 54, 99, 61[]] |a0=[B:[[91, -89]] |this=org.apache.commons.codec.binary.Base64:@?
1=[B:[[87, 54, 99, 61]]] |a0=[B:[[91, -89]...> but was:<...=[B:[[87, 54, 99, 61[, 13, 10]] |a0=[B:[[91, -89]] |this=org.apache.commons.codec.binary.Base64:@?
1=[B:[[87, 54, 99, 61, 13, 10]]] |a0=[B:[[91, -89]...>
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
--- org.apache.commons.codec.binary.Base64_Zest_Test::test112
junit.framework.AssertionFailedError: expected:<... 66, 108, 56, 99, 61[]] |a0=[B:[[111, -59, -3, 44, -87, -127, -105, -57]] |this=org.apache.commons.codec.binary.Base64:@?
1=[B:[[98, 56, 88, 57, 76, 75, 109, 66, 108, 56, 99, 61]]] |a0=[B:[[111, -59...> but was:<... 66, 108, 56, 99, 61[, 13, 10]] |a0=[B:[[111, -59, -3, 44, -87, -127, -105, -57]] |this=org.apache.commons.codec.binary.Base64:@?
1=[B:[[98, 56, 88, 57, 76, 75, 109, 66, 108, 56, 99, 61, 13, 10]]] |a0=[B:[[111, -59...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.binary.Base64_Zest_Test.test112(Base64_Zest_Test.java:791)
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
--- org.apache.commons.codec.binary.Base64_Zest_Test::test115
junit.framework.AssertionFailedError: expected:<...115, 87, 111, 97, 66[]] |a0=[B:[[101, 40, 44, 90, -122, -127]] |this=org.apache.commons.codec.binary.Base64:@?
```

- Suite: `/home/pilaiphon/suites/Codec/zest/4/t12001/Codec-4f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_4/b120_r1/src/org/apache/commons/codec/binary/Base64_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_4/b120_r1`
