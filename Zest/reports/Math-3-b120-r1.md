# Zest – Math-3 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 285276 (valid 67.29%) |
| Cycles | 11 |
| Corpus | 115 |
| Zest branch coverage (total / valid) | 259 / 256 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 115 |
| Failing on fixed | 0 |
| Failing on buggy | 1 |
| Bug detection | Fail (triggering: 1) |
| **Fault detected** | **yes** |
| Line coverage | 168 / 423 = 39.72% |
| Branch coverage | 58 / 236 = 24.58% |
| Test suite length (statements) | 115 |
| Mutation score | 1471 / 1746 = 84.25% |
| Fuzz time | 123s |
| Pipeline time | 334s |

## Failing tests on buggy version

- org.apache.commons.math3.util.MathArrays_Zest_Test::test97

```
--- org.apache.commons.math3.util.MathArrays_Zest_Test::test97
junit.framework.AssertionFailedError: expected:<0=[java.lang.Double:5.44915739E8 |a0=[D:[[26458.0]] |a1=[D:[[20595.5]]]
> but was:<0=[!java.lang.ArrayIndexOutOfBoundsException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.math3.util.MathArrays_Zest_Test.test97(MathArrays_Zest_Test.java:686)
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

- Suite: `/home/user/suites/Math/zest/3/t12001/Math-3f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Math_3/b120_r1/src/org/apache/commons/math3/util/MathArrays_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Math_3/b120_r1`
