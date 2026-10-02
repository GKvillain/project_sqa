# Zest – Closure-52 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 420085 (valid 63.62%) |
| Cycles | 14 |
| Corpus | 97 |
| Zest branch coverage (total / valid) | 221 / 217 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 97 |
| Failing on fixed | 0 |
| Failing on buggy | 1 |
| Bug detection | Fail (triggering: 1) |
| **Fault detected** | **yes** |
| Line coverage | 112 / 595 = 18.82% |
| Branch coverage | 57 / 453 = 12.58% |
| Test suite length (statements) | 97 |
| Mutation score | 963 / 1139 = 84.55% |
| Fuzz time | 122s |
| Pipeline time | 282s |

## Failing tests on buggy version

- com.google.javascript.jscomp.CodeGenerator_Zest_Test::test96

```
--- com.google.javascript.jscomp.CodeGenerator_Zest_Test::test96
junit.framework.AssertionFailedError: expected:<0=java.lang.Boolean:[fals]e
> but was:<0=java.lang.Boolean:[tru]e
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.google.javascript.jscomp.CodeGenerator_Zest_Test.test96(CodeGenerator_Zest_Test.java:679)
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

- Suite: `/home/user/suites/Closure/zest/52/t12001/Closure-52f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_52/b120_r1/src/com/google/javascript/jscomp/CodeGenerator_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_52/b120_r1`
