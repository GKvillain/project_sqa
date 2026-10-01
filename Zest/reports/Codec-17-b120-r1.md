# Zest – Codec-17 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 88849 (valid 71.53%) |
| Cycles | 6 |
| Corpus | 62 |
| Zest branch coverage (total / valid) | 206 / 203 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 62 |
| Failing on fixed | 0 |
| Failing on buggy | 3 |
| Bug detection | Fail (triggering: 3) |
| **Fault detected** | **yes** |
| Line coverage | 30 / 39 = 76.92% |
| Branch coverage | 14 / 20 = 70.00% |
| Test suite length (statements) | 62 |
| Mutation score | 12 / 23 = 52.17% |
| Fuzz time | 120s |
| Pipeline time | 204s |

## Failing tests on buggy version

- org.apache.commons.codec.binary.StringUtils_Zest_Test::test40
- org.apache.commons.codec.binary.StringUtils_Zest_Test::test50
- org.apache.commons.codec.binary.StringUtils_Zest_Test::test54

```
--- org.apache.commons.codec.binary.StringUtils_Zest_Test::test40
junit.framework.AssertionFailedError: expected:<0=[null
1=null]
> but was:<0=[!java.lang.NullPointerException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.binary.StringUtils_Zest_Test.test40(StringUtils_Zest_Test.java:287)
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
--- org.apache.commons.codec.binary.StringUtils_Zest_Test::test50
junit.framework.AssertionFailedError: expected:<0=[null]
> but was:<0=[!java.lang.NullPointerException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.binary.StringUtils_Zest_Test.test50(StringUtils_Zest_Test.java:357)
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
--- org.apache.commons.codec.binary.StringUtils_Zest_Test::test54
```

- Suite: `/home/pilaiphon/suites/Codec/zest/17/t12001/Codec-17f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_17/b120_r1/src/org/apache/commons/codec/binary/StringUtils_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_17/b120_r1`
