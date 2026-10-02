# Zest – Math-1 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 232727 (valid 88.81%) |
| Cycles | 14 |
| Corpus | 102 |
| Zest branch coverage (total / valid) | 276 / 273 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 102 |
| Failing on fixed | 0 |
| Failing on buggy | 13 |
| Bug detection | Fail (triggering: 13) |
| **Fault detected** | **yes** |
| Line coverage | 201 / 429 = 46.85% |
| Branch coverage | 78 / 200 = 39.00% |
| Test suite length (statements) | 102 |
| Mutation score | 696 / 868 = 80.18% |
| Fuzz time | 122s |
| Pipeline time | 365s |

## Failing tests on buggy version

- org.apache.commons.math3.fraction.BigFraction_Zest_Test::test56
- org.apache.commons.math3.fraction.BigFraction_Zest_Test::test65
- org.apache.commons.math3.fraction.BigFraction_Zest_Test::test72
- org.apache.commons.math3.fraction.BigFraction_Zest_Test::test75
- org.apache.commons.math3.fraction.BigFraction_Zest_Test::test79
- org.apache.commons.math3.fraction.BigFraction_Zest_Test::test82
- org.apache.commons.math3.fraction.BigFraction_Zest_Test::test83
- org.apache.commons.math3.fraction.BigFraction_Zest_Test::test86
- org.apache.commons.math3.fraction.BigFraction_Zest_Test::test88
- org.apache.commons.math3.fraction.BigFraction_Zest_Test::test90
- org.apache.commons.math3.fraction.BigFraction_Zest_Test::test91
- org.apache.commons.math3.fraction.BigFraction_Zest_Test::test94
- org.apache.commons.math3.fraction.BigFraction_Zest_Test::test98

```
--- org.apache.commons.math3.fraction.BigFraction_Zest_Test::test56
junit.framework.AssertionFailedError: expected:<0=[new org.apache.commons.math3.fraction.BigFraction:614727 / 926129755
1=new org.apache.commons.math3.fraction.BigFraction:0]
> but was:<0=[!org.apache.commons.math3.fraction.FractionConversionException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.math3.fraction.BigFraction_Zest_Test.test56(BigFraction_Zest_Test.java:399)
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
--- org.apache.commons.math3.fraction.BigFraction_Zest_Test::test65
junit.framework.AssertionFailedError: expected:<...4364122021 / 1928
1=[new org.apache.commons.math3.fraction.BigFraction:0]
> but was:<...4364122021 / 1928
1=[!org.apache.commons.math3.fraction.FractionConversionException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.math3.fraction.BigFraction_Zest_Test.test65(BigFraction_Zest_Test.java:462)
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
```

- Suite: `/home/user/suites/Math/zest/1/t12001/Math-1f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Math_1/b120_r1/src/org/apache/commons/math3/fraction/BigFraction_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Math_1/b120_r1`
