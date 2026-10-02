# Zest – Closure-106 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 392925 (valid 79.79%) |
| Cycles | 36 |
| Corpus | 40 |
| Zest branch coverage (total / valid) | 247 / 244 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 40 |
| Failing on fixed | 0 |
| Failing on buggy | 1 |
| Bug detection | Fail (triggering: 1) |
| **Fault detected** | **yes** |
| Line coverage | 68 / 630 = 10.79% |
| Branch coverage | 19 / 435 = 4.37% |
| Test suite length (statements) | 40 |
| Mutation score | 832 / 882 = 94.33% |
| Fuzz time | 120s |
| Pipeline time | 200s |

## Failing tests on buggy version

- com.google.javascript.jscomp.GlobalNamespace_Zest_Test::test32

```
--- com.google.javascript.jscomp.GlobalNamespace_Zest_Test::test32
junit.framework.AssertionFailedError: expected:<...DocInfoBuilder:@?
1=[com.google.javascript.rhino.JSDocInfo:JSDocInfo] |this=com.google.ja...> but was:<...DocInfoBuilder:@?
1=[null] |this=com.google.ja...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.google.javascript.jscomp.GlobalNamespace_Zest_Test.test32(GlobalNamespace_Zest_Test.java:231)
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

- Suite: `/home/user/suites/Closure/zest/106/t12001/Closure-106f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_106/b120_r1/src/com/google/javascript/jscomp/GlobalNamespace_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_106/b120_r1`
