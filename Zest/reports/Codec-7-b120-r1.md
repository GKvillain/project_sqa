# Zest – Codec-7 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 109376 (valid 84.61%) |
| Cycles | 4 |
| Corpus | 139 |
| Zest branch coverage (total / valid) | 364 / 362 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 139 |
| Failing on fixed | 0 |
| Failing on buggy | 7 |
| Bug detection | Fail (triggering: 7) |
| **Fault detected** | **yes** |
| Line coverage | 189 / 233 = 81.12% |
| Branch coverage | 119 / 162 = 73.46% |
| Test suite length (statements) | 139 |
| Mutation score | 613 / 990 = 61.92% |
| Fuzz time | 121s |
| Pipeline time | 237s |

## Failing tests on buggy version

- org.apache.commons.codec.binary.Base64_Zest_Test::test112
- org.apache.commons.codec.binary.Base64_Zest_Test::test137
- org.apache.commons.codec.binary.Base64_Zest_Test::test138
- org.apache.commons.codec.binary.Base64_Zest_Test::test32
- org.apache.commons.codec.binary.Base64_Zest_Test::test69
- org.apache.commons.codec.binary.Base64_Zest_Test::test82
- org.apache.commons.codec.binary.Base64_Zest_Test::test98

```
--- org.apache.commons.codec.binary.Base64_Zest_Test::test112
junit.framework.AssertionFailedError: expected:<...ava.lang.String:/SrQ[ |a0=[B:[[-3, 42, -48]]
1=java.lang.String:/SrQ] |a0=[B:[[-3, 42, -4...> but was:<...ava.lang.String:/SrQ[
 |a0=[B:[[-3, 42, -48]]
1=java.lang.String:/SrQ
] |a0=[B:[[-3, 42, -4...>
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
--- org.apache.commons.codec.binary.Base64_Zest_Test::test137
junit.framework.AssertionFailedError: expected:<....String:ucnskOmO/9A=[ |a0=[B:[[-71, -55, -20, -112, -23, -114, -1, -48]]
1=java.lang.String:ucnskOmO/9A=] |a0=[B:[[-71, -55, ...> but was:<....String:ucnskOmO/9A=[
 |a0=[B:[[-71, -55, -20, -112, -23, -114, -1, -48]]
1=java.lang.String:ucnskOmO/9A=
] |a0=[B:[[-71, -55, ...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.binary.Base64_Zest_Test.test137(Base64_Zest_Test.java:966)
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
```

- Suite: `/home/pilaiphon/suites/Codec/zest/7/t12001/Codec-7f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_7/b120_r1/src/org/apache/commons/codec/binary/Base64_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_7/b120_r1`
