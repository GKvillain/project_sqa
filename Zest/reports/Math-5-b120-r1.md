# Zest – Math-5 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 194023 (valid 95.96%) |
| Cycles | 21 |
| Corpus | 58 |
| Zest branch coverage (total / valid) | 381 / 378 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 58 |
| Failing on fixed | 0 |
| Failing on buggy | 4 |
| Bug detection | Fail (triggering: 4) |
| **Fault detected** | **yes** |
| Line coverage | 106 / 226 = 46.90% |
| Branch coverage | 60 / 170 = 35.29% |
| Test suite length (statements) | 58 |
| Mutation score | 570 / 698 = 81.66% |
| Fuzz time | 122s |
| Pipeline time | 318s |

## Failing tests on buggy version

- org.apache.commons.math3.complex.Complex_Zest_Test::test39
- org.apache.commons.math3.complex.Complex_Zest_Test::test41
- org.apache.commons.math3.complex.Complex_Zest_Test::test44
- org.apache.commons.math3.complex.Complex_Zest_Test::test52

```
--- org.apache.commons.math3.complex.Complex_Zest_Test::test39
junit.framework.AssertionFailedError: expected:<...h3.complex.Complex:([Infinity, Infinity) |this=org.apache.commons.math3.complex.Complex:(0.0, 0.0)
1=org.apache.commons.math3.complex.Complex:(Infinity, Infinity]) |this=org.apache.c...> but was:<...h3.complex.Complex:([NaN, NaN) |this=org.apache.commons.math3.complex.Complex:(0.0, 0.0)
1=org.apache.commons.math3.complex.Complex:(NaN, NaN]) |this=org.apache.c...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.math3.complex.Complex_Zest_Test.test39(Complex_Zest_Test.java:280)
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
--- org.apache.commons.math3.complex.Complex_Zest_Test::test41
junit.framework.AssertionFailedError: expected:<...h3.complex.Complex:([Infinity, Infinity]) |this=org.apache.c...> but was:<...h3.complex.Complex:([NaN, NaN]) |this=org.apache.c...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.math3.complex.Complex_Zest_Test.test41(Complex_Zest_Test.java:294)
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
--- org.apache.commons.math3.complex.Complex_Zest_Test::test44
junit.framework.AssertionFailedError: expected:<...h3.complex.Complex:([Infinity, Infinity]) |this=org.apache.c...> but was:<...h3.complex.Complex:([NaN, NaN]) |this=org.apache.c...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
```

- Suite: `/home/user/suites/Math/zest/5/t12001/Math-5f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Math_5/b120_r1/src/org/apache/commons/math3/complex/Complex_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Math_5/b120_r1`
