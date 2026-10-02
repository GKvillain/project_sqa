# Zest – Collections-7 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 378332 (valid 85.72%) |
| Cycles | 19 |
| Corpus | 116 |
| Zest branch coverage (total / valid) | 328 / 325 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 116 |
| Failing on fixed | 0 |
| Failing on buggy | 42 |
| Bug detection | Fail (triggering: 42) |
| **Fault detected** | **yes** |
| Line coverage | 189 / 491 = 38.49% |
| Branch coverage | 100 / 280 = 35.71% |
| Test suite length (statements) | 116 |
| Mutation score | 402 / 448 = 89.73% |
| Fuzz time | 127s |
| Pipeline time | 201s |

## Failing tests on buggy version

- org.apache.commons.collections.ExtendedProperties_Zest_Test::test100
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test101
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test102
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test103
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test104
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test105
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test106
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test107
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test108
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test109
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test110
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test111
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test112
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test113
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test114
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test115
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test30
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test33
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test50
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test56
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test58
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test59
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test60
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test63
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test69
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test73
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test78
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test79
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test80
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test85
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test87
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test88
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test89
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test90
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test91
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test92
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test93
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test94
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test95
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test96
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test97
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test99

```
--- org.apache.commons.collections.ExtendedProperties_Zest_Test::test100
junit.framework.AssertionFailedError: expected:<0=[null |this=org.apache.commons.collections.ExtendedProperties:{󐤛󭞛𤾐񃵝𲖞횜򳹠񆀶񟣳򟿶=閐䧚᫱Σ瞌쎂蹸풛፥襳}
1=java.lang.Boolean:false |this=org.apache.commons.collections.ExtendedProperties:{󐤛󭞛𤾐񃵝𲖞횜򳹠񆀶񟣳򟿶=false}]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.collections.ExtendedProperties_Zest_Test.test100(ExtendedProperties_Zest_Test.java:707)
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
--- org.apache.commons.collections.ExtendedProperties_Zest_Test::test101
junit.framework.AssertionFailedError: expected:<0=[null |this=org.apache.commons.collections.ExtendedProperties:{󐤛󭞛𤾐򋸎󻞡화򳹠񃴶񟣳򢛶➮=郙᫱Σ瞌쎂蹸풛፥襳ꨢ꘾푊ꁴ㦜簘}
1=java.lang.Boolean:false |this=org.apache.commons.collections.ExtendedProperties:{󐤛󭞛𤾐򋸎󻞡화򳹠񃴶񟣳򢛶➮=false}]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.collections.ExtendedProperties_Zest_Test.test101(ExtendedProperties_Zest_Test.java:714)
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

- Suite: `/home/user/suites/Collections/zest/7/t12001/Collections-7f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_7/b120_r1/src/org/apache/commons/collections/ExtendedProperties_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_7/b120_r1`
