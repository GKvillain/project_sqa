# Zest – Codec-13 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 114680 (valid 89.91%) |
| Cycles | 4 |
| Corpus | 186 |
| Zest branch coverage (total / valid) | 576 / 573 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 186 |
| Failing on fixed | 0 |
| Failing on buggy | 21 |
| Bug detection | Fail (triggering: 21) |
| **Fault detected** | **yes** |
| Line coverage | 300 / 506 = 59.29% |
| Branch coverage | 212 / 482 = 43.98% |
| Test suite length (statements) | 186 |
| Mutation score | 1222 / 2185 = 55.93% |
| Fuzz time | 121s |
| Pipeline time | 385s |

## Failing tests on buggy version

- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test127
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test128
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test143
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test173
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test12
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test20
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test23
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test27
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test28
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test44
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test45
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test47
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test53
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test55
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test56
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test58
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test62
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test66
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test68
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test95
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test4

```
--- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test127
junit.framework.AssertionFailedError: expected:<0=[java.lang.Boolean:true |this=org.apache.commons.codec.language.DoubleMetaphone:@?
1=java.lang.Boolean:true |this=org.apache.commons.codec.language.DoubleMetaphone:@?]
> but was:<0=[!java.lang.NullPointerException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test.test127(CharSequenceUtils_Zest_Test.java:896)
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
--- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test128
junit.framework.AssertionFailedError: expected:<0=[java.lang.Boolean:true |this=org.apache.commons.codec.language.DoubleMetaphone:@?
1=java.lang.Boolean:true |this=org.apache.commons.codec.language.DoubleMetaphone:@?]
> but was:<0=[!java.lang.NullPointerException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test.test128(CharSequenceUtils_Zest_Test.java:903)
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

- Suite: `/home/pilaiphon/suites/Codec/zest/13/t12002/Codec-13f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_13/b120_r2/src/org/apache/commons/codec/binary/CharSequenceUtils_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_13/b120_r2`
