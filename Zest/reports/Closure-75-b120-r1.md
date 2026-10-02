# Zest – Closure-75 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 452876 (valid 56.88%) |
| Cycles | 27 |
| Corpus | 52 |
| Zest branch coverage (total / valid) | 183 / 180 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 52 |
| Failing on fixed | 0 |
| Failing on buggy | 4 |
| Bug detection | Fail (triggering: 4) |
| **Fault detected** | **yes** |
| Line coverage | 63 / 922 = 6.83% |
| Branch coverage | 29 / 893 = 3.25% |
| Test suite length (statements) | 52 |
| Mutation score | 1197 / 1268 = 94.40% |
| Fuzz time | 121s |
| Pipeline time | 252s |

## Failing tests on buggy version

- com.google.javascript.jscomp.NodeUtil_Zest_Test::test17
- com.google.javascript.jscomp.NodeUtil_Zest_Test::test36
- com.google.javascript.jscomp.NodeUtil_Zest_Test::test43
- com.google.javascript.jscomp.NodeUtil_Zest_Test::test46

```
--- com.google.javascript.jscomp.NodeUtil_Zest_Test::test17
junit.framework.AssertionFailedError: expected:<...jstype.TernaryValue$[3:unknown]
> but was:<...jstype.TernaryValue$[2:true]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.google.javascript.jscomp.NodeUtil_Zest_Test.test17(NodeUtil_Zest_Test.java:126)
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
--- com.google.javascript.jscomp.NodeUtil_Zest_Test::test36
junit.framework.AssertionFailedError: expected:<...jstype.TernaryValue$[3:unknown
1=com.google.javascript.rhino.jstype.TernaryValue$3:unknown]
> but was:<...jstype.TernaryValue$[2:true
1=com.google.javascript.rhino.jstype.TernaryValue$2:true]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.google.javascript.jscomp.NodeUtil_Zest_Test.test36(NodeUtil_Zest_Test.java:259)
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

- Suite: `/home/user/suites/Closure/zest/75/t12001/Closure-75f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_75/b120_r1/src/com/google/javascript/jscomp/NodeUtil_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_75/b120_r1`
