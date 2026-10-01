# Zest – Chart-18 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 102261 (valid 66.68%) |
| Cycles | 4 |
| Corpus | 58 |
| Zest branch coverage (total / valid) | 201 / 198 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 58 |
| Failing on fixed | 0 |
| Failing on buggy | 6 |
| Bug detection | Fail (triggering: 6) |
| **Fault detected** | **yes** |
| Line coverage | 72 / 271 = 26.57% |
| Branch coverage | 19 / 126 = 15.08% |
| Test suite length (statements) | 58 |
| Mutation score | 291 / 318 = 91.51% |
| Fuzz time | 120s |
| Pipeline time | 316s |

## Failing tests on buggy version

- org.jfree.data.DefaultKeyedValues_Zest_Test::test11
- org.jfree.data.DefaultKeyedValues_Zest_Test::test27
- org.jfree.data.DefaultKeyedValues_Zest_Test::test33
- org.jfree.data.DefaultKeyedValues_Zest_Test::test46
- org.jfree.data.DefaultKeyedValues_Zest_Test::test50
- org.jfree.data.DefaultKeyedValues_Zest_Test::test7

```
--- org.jfree.data.DefaultKeyedValues_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<0=[!org.jfree.data.UnknownKeyException]
> but was:<0=[void |this=org.jfree.data.DefaultKeyedValues2D:org.jfree.data.DefaultKeyedValues2D@367]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.data.DefaultKeyedValues_Zest_Test.test11(DefaultKeyedValues_Zest_Test.java:84)
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
--- org.jfree.data.DefaultKeyedValues_Zest_Test::test27
junit.framework.AssertionFailedError: expected:<0=[!java.lang.IllegalArgumentException]
> but was:<0=[void |this=org.jfree.data.DefaultKeyedValues2D:org.jfree.data.DefaultKeyedValues2D@367]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.data.DefaultKeyedValues_Zest_Test.test27(DefaultKeyedValues_Zest_Test.java:196)
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
--- org.jfree.data.DefaultKeyedValues_Zest_Test::test33
junit.framework.AssertionFailedError: expected:<...aultKeyedValues@1
```

- Suite: `/home/pilaiphon/suites/Chart/zest/18/t12002/Chart-18f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_18/b120_r2/src/org/jfree/data/DefaultKeyedValues_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_18/b120_r2`
