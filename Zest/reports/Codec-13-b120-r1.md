# Zest – Codec-13 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 97625 (valid 91.49%) |
| Cycles | 3 |
| Corpus | 190 |
| Zest branch coverage (total / valid) | 576 / 573 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 190 |
| Failing on fixed | 0 |
| Failing on buggy | 35 |
| Bug detection | Fail (triggering: 35) |
| **Fault detected** | **yes** |
| Line coverage | 301 / 506 = 59.49% |
| Branch coverage | 214 / 482 = 44.40% |
| Test suite length (statements) | 190 |
| Mutation score | 1232 / 2185 = 56.38% |
| Fuzz time | 120s |
| Pipeline time | 414s |

## Failing tests on buggy version

- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test100
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test101
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test102
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test104
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test105
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test130
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test131
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test133
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test150
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test180
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test12
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test16
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test24
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test26
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test29
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test37
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test39
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test48
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test49
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test50
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test51
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test52
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test53
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test56
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test57
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test58
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test59
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test60
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test66
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test69
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test71
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test83
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test88
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test98
- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test4

```
--- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test100
junit.framework.AssertionFailedError: expected:<0=[java.lang.Boolean:false |this=org.apache.commons.codec.language.DoubleMetaphone:@?
1=java.lang.Boolean:false |this=org.apache.commons.codec.language.DoubleMetaphone:@?]
> but was:<0=[!java.lang.NullPointerException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test.test100(CharSequenceUtils_Zest_Test.java:707)
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
--- org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test::test101
junit.framework.AssertionFailedError: expected:<0=[java.lang.Boolean:true |this=org.apache.commons.codec.language.DoubleMetaphone:@?
1=java.lang.Boolean:true |this=org.apache.commons.codec.language.DoubleMetaphone:@?]
> but was:<0=[!java.lang.NullPointerException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.binary.CharSequenceUtils_Zest_Test.test101(CharSequenceUtils_Zest_Test.java:714)
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

- Suite: `/home/pilaiphon/suites/Codec/zest/13/t12001/Codec-13f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_13/b120_r1/src/org/apache/commons/codec/binary/CharSequenceUtils_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_13/b120_r1`
