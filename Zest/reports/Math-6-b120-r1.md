# Zest – Math-6 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 303404 (valid 53.74%) |
| Cycles | 26 |
| Corpus | 48 |
| Zest branch coverage (total / valid) | 321 / 317 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 48 |
| Failing on fixed | 0 |
| Failing on buggy | 5 |
| Bug detection | Fail (triggering: 5) |
| **Fault detected** | **yes** |
| Line coverage | 113 / 1201 = 9.41% |
| Branch coverage | 16 / 587 = 2.73% |
| Test suite length (statements) | 56 |
| Mutation score | 3321 / 3360 = 98.84% |
| Fuzz time | 122s |
| Pipeline time | 322s |

## Failing tests on buggy version

- org.apache.commons.math3.optim.BaseOptimizer_Zest_Test::test12
- org.apache.commons.math3.optim.BaseOptimizer_Zest_Test::test17
- org.apache.commons.math3.optim.BaseOptimizer_Zest_Test::test29
- org.apache.commons.math3.optim.BaseOptimizer_Zest_Test::test31
- org.apache.commons.math3.optim.BaseOptimizer_Zest_Test::test33

```
--- org.apache.commons.math3.optim.BaseOptimizer_Zest_Test::test12
junit.framework.AssertionFailedError: expected:<0=[void |this=org.apache.commons.math3.optim.linear.SimplexSolver:@?]
> but was:<0=[!org.apache.commons.math3.exception.TooManyIterationsException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.math3.optim.BaseOptimizer_Zest_Test.test12(BaseOptimizer_Zest_Test.java:91)
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
--- org.apache.commons.math3.optim.BaseOptimizer_Zest_Test::test17
junit.framework.AssertionFailedError: expected:<0=java.lang.Integer:[2147483647] |this=org.apache.co...> but was:<0=java.lang.Integer:[0] |this=org.apache.co...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.math3.optim.BaseOptimizer_Zest_Test.test17(BaseOptimizer_Zest_Test.java:126)
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
--- org.apache.commons.math3.optim.BaseOptimizer_Zest_Test::test29
junit.framework.AssertionFailedError: expected:<0=java.lang.Integer:[2147483647 |this=org.apache.commons.math3.optim.linear.SimplexSolver:@?
1=java.lang.Integer:2147483647] |this=org.apache.co...> but was:<0=java.lang.Integer:[0 |this=org.apache.commons.math3.optim.linear.SimplexSolver:@?
1=java.lang.Integer:0] |this=org.apache.co...>
```

- Suite: `/home/user/suites/Math/zest/6/t12001/Math-6f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Math_6/b120_r1/src/org/apache/commons/math3/optim/BaseOptimizer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Math_6/b120_r1`
