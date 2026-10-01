# Zest – Codec-15 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 106999 (valid 81.57%) |
| Cycles | 6 |
| Corpus | 89 |
| Zest branch coverage (total / valid) | 256 / 254 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 89 |
| Failing on fixed | 0 |
| Failing on buggy | 4 |
| Bug detection | Fail (triggering: 4) |
| **Fault detected** | **yes** |
| Line coverage | 51 / 51 = 100.00% |
| Branch coverage | 31 / 32 = 96.88% |
| Test suite length (statements) | 89 |
| Mutation score | 72 / 106 = 67.92% |
| Fuzz time | 120s |
| Pipeline time | 198s |

## Failing tests on buggy version

- org.apache.commons.codec.language.Soundex_Zest_Test::test67
- org.apache.commons.codec.language.Soundex_Zest_Test::test71
- org.apache.commons.codec.language.Soundex_Zest_Test::test86
- org.apache.commons.codec.language.Soundex_Zest_Test::test87

```
--- org.apache.commons.codec.language.Soundex_Zest_Test::test67
junit.framework.AssertionFailedError: expected:<...nguage.Soundex:@?
1=[!java.lang.IllegalArgumentException]
> but was:<...nguage.Soundex:@?
1=[java.lang.Character:6 |this=org.apache.commons.codec.language.Soundex:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.language.Soundex_Zest_Test.test67(Soundex_Zest_Test.java:476)
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
--- org.apache.commons.codec.language.Soundex_Zest_Test::test71
junit.framework.AssertionFailedError: expected:<...nguage.Soundex:@?
1=[!java.lang.IllegalArgumentException]
> but was:<...nguage.Soundex:@?
1=[java.lang.Character:6 |this=org.apache.commons.codec.language.Soundex:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.language.Soundex_Zest_Test.test71(Soundex_Zest_Test.java:504)
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

- Suite: `/home/pilaiphon/suites/Codec/zest/15/t12002/Codec-15f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_15/b120_r2/src/org/apache/commons/codec/language/Soundex_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_15/b120_r2`
