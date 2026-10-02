# Zest – Closure-131 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 441854 (valid 93.87%) |
| Cycles | 15 |
| Corpus | 204 |
| Zest branch coverage (total / valid) | 210 / 207 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 204 |
| Failing on fixed | 0 |
| Failing on buggy | 3 |
| Bug detection | Fail (triggering: 3) |
| **Fault detected** | **yes** |
| Line coverage | 69 / 114 = 60.53% |
| Branch coverage | 76 / 169 = 44.97% |
| Test suite length (statements) | 204 |
| Mutation score | 330 / 463 = 71.27% |
| Fuzz time | 129s |
| Pipeline time | 298s |

## Failing tests on buggy version

- com.google.javascript.rhino.TokenStream_Zest_Test::test117
- com.google.javascript.rhino.TokenStream_Zest_Test::test161
- com.google.javascript.rhino.TokenStream_Zest_Test::test181

```
--- com.google.javascript.rhino.TokenStream_Zest_Test::test117
junit.framework.AssertionFailedError: expected:<0=java.lang.Boolean:[fals]e
1=java.lang.Boolea...> but was:<0=java.lang.Boolean:[tru]e
1=java.lang.Boolea...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.google.javascript.rhino.TokenStream_Zest_Test.test117(TokenStream_Zest_Test.java:826)
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
--- com.google.javascript.rhino.TokenStream_Zest_Test::test161
junit.framework.AssertionFailedError: expected:<...1=java.lang.Boolean:[fals]e
> but was:<...1=java.lang.Boolean:[tru]e
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.google.javascript.rhino.TokenStream_Zest_Test.test161(TokenStream_Zest_Test.java:1134)
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
--- com.google.javascript.rhino.TokenStream_Zest_Test::test181
junit.framework.AssertionFailedError: expected:<...1=java.lang.Boolean:[fals]e
```

- Suite: `/home/user/suites/Closure/zest/131/t12001/Closure-131f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_131/b120_r1/src/com/google/javascript/rhino/TokenStream_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_131/b120_r1`
