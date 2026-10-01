# Zest – Codec-12 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 116961 (valid 50.29%) |
| Cycles | 6 |
| Corpus | 50 |
| Zest branch coverage (total / valid) | 221 / 218 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 50 |
| Failing on fixed | 0 |
| Failing on buggy | 27 |
| Bug detection | Fail (triggering: 27) |
| **Fault detected** | **yes** |
| Line coverage | 29 / 49 = 59.18% |
| Branch coverage | 18 / 36 = 50.00% |
| Test suite length (statements) | 50 |
| Mutation score | 101 / 128 = 78.91% |
| Fuzz time | 120s |
| Pipeline time | 199s |

## Failing tests on buggy version

- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test10
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test11
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test15
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test16
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test17
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test19
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test21
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test25
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test29
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test33
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test38
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test39
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test40
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test42
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test43
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test45
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test46
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test49
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test0
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test1
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test2
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test3
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test4
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test5
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test6
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test8
- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test9

```
--- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<...ecInputStream:@?
1=![java.lang.NullPointer]Exception
> but was:<...ecInputStream:@?
1=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test.test10(BaseNCodecInputStream_Zest_Test.java:77)
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
--- org.apache.commons.codec.binary.BaseNCodecInputStream_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<...ecInputStream:@?
1=![java.lang.NullPointer]Exception
> but was:<...ecInputStream:@?
1=![H:java.lang.NoSuchMethod]Exception
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
```

- Suite: `/home/pilaiphon/suites/Codec/zest/12/t12002/Codec-12f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_12/b120_r2/src/org/apache/commons/codec/binary/BaseNCodecInputStream_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_12/b120_r2`
