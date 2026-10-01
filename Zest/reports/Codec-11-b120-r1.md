# Zest – Codec-11 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 101604 (valid 88.12%) |
| Cycles | 5 |
| Corpus | 98 |
| Zest branch coverage (total / valid) | 305 / 303 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 98 |
| Failing on fixed | 0 |
| Failing on buggy | 29 |
| Bug detection | Fail (triggering: 29) |
| **Fault detected** | **yes** |
| Line coverage | 95 / 107 = 88.79% |
| Branch coverage | 62 / 70 = 88.57% |
| Test suite length (statements) | 98 |
| Mutation score | 165 / 199 = 82.91% |
| Fuzz time | 120s |
| Pipeline time | 211s |

## Failing tests on buggy version

- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test19
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test22
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test23
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test24
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test31
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test34
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test41
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test46
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test47
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test48
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test50
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test53
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test54
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test55
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test58
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test66
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test68
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test69
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test70
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test76
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test77
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test78
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test79
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test85
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test86
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test89
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test90
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test91
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test94

```
--- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test19
junit.framework.AssertionFailedError: expected:<...7=F1=99=97=B7=F2=B8=[
=]BB=95=F3=B6=87=8C=F1...> but was:<...7=F1=99=97=B7=F2=B8=[]BB=95=F3=B6=87=8C=F1...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test.test19(QuotedPrintableCodec_Zest_Test.java:140)
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
--- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test22
junit.framework.AssertionFailedError: expected:<0=![java.lang.NullPointer]Exception
> but was:<0=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test.test22(QuotedPrintableCodec_Zest_Test.java:161)
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
--- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test23
junit.framework.AssertionFailedError: expected:<...7=F1=99=97=B7=F1=BD=[
=]A8=91=F1=9E=92=8E=F0...> but was:<...7=F1=99=97=B7=F1=BD=[]A8=91=F1=9E=92=8E=F0...>
```

- Suite: `/home/pilaiphon/suites/Codec/zest/11/t12001/Codec-11f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_11/b120_r1/src/org/apache/commons/codec/net/QuotedPrintableCodec_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_11/b120_r1`
