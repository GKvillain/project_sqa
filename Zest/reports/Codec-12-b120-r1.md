# Zest – Codec-12 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 105535 (valid 51.65%) |
| Cycles | 5 |
| Corpus | 51 |
| Zest branch coverage (total / valid) | 225 / 222 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 51 |
| Failing on fixed | 0 |
| Failing on buggy | 28 |
| Bug detection | Fail (triggering: 28) |
| **Fault detected** | **yes** |
| Line coverage | 30 / 49 = 61.22% |
| Branch coverage | 19 / 36 = 52.78% |
| Test suite length (statements) | 51 |
| Mutation score | 105 / 128 = 82.03% |
| Fuzz time | 121s |
| Pipeline time | 206s |

## Failing tests on buggy version

- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test11
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test12
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test13
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test15
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test16
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test18
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test19
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test23
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test24
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test25
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test26
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test28
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test33
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test38
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test39
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test42
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test44
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test45
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test46
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test0
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test1
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test2
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test3
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test4
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test5
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test7
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test8
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test9

```
--- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<0=![java.lang.NullPointer]Exception
> but was:<0=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test.test11(BaseNCodecInputStream_Zest_Test.java:84)
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
--- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test12
junit.framework.AssertionFailedError: expected:<0=![java.lang.IllegalArgument]Exception
> but was:<0=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test.test12(BaseNCodecInputStream_Zest_Test.java:91)
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
--- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test13
junit.framework.AssertionFailedError: expected:<0=![java.lang.NullPointer]Exception
```

- Suite: `/home/pilaiphon/suites/Codec/zest/12/t12001/Codec-12f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_12/b120_r1/src/org/apache/commons/codec/binary/BaseNCodecInputStream_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_12/b120_r1`
