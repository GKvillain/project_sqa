# Zest – Closure-51 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 398147 (valid 90.27%) |
| Cycles | 31 |
| Corpus | 45 |
| Zest branch coverage (total / valid) | 230 / 227 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 45 |
| Failing on fixed | 0 |
| Failing on buggy | 6 |
| Bug detection | Fail (triggering: 6) |
| **Fault detected** | **yes** |
| Line coverage | 69 / 109 = 63.30% |
| Branch coverage | 33 / 60 = 55.00% |
| Test suite length (statements) | 45 |
| Mutation score | 81 / 207 = 39.13% |
| Fuzz time | 123s |
| Pipeline time | 274s |

## Failing tests on buggy version

- com.google.javascript.jscomp.CodeConsumer_Zest_Test::test11
- com.google.javascript.jscomp.CodeConsumer_Zest_Test::test22
- com.google.javascript.jscomp.CodeConsumer_Zest_Test::test27
- com.google.javascript.jscomp.CodeConsumer_Zest_Test::test31
- com.google.javascript.jscomp.CodeConsumer_Zest_Test::test37
- com.google.javascript.jscomp.CodeConsumer_Zest_Test::test2

```
--- com.google.javascript.jscomp.CodeConsumer_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<0=[java.lang.Boolean:false
1=java.lang.Boolean:true |this=com.google.javascript.jscomp.CodePrinter$CompactCodePrinter:@?]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.google.javascript.jscomp.CodeConsumer_Zest_Test.test11(CodeConsumer_Zest_Test.java:84)
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
--- com.google.javascript.jscomp.CodeConsumer_Zest_Test::test22
junit.framework.AssertionFailedError: expected:<0=[java.lang.Boolean:false
1=java.lang.Boolean:false]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.google.javascript.jscomp.CodeConsumer_Zest_Test.test22(CodeConsumer_Zest_Test.java:161)
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

- Suite: `/home/user/suites/Closure/zest/51/t12001/Closure-51f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_51/b120_r1/src/com/google/javascript/jscomp/CodeConsumer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_51/b120_r1`
