# Zest – Collections-17 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 385224 (valid 93.44%) |
| Cycles | 93 |
| Corpus | 17 |
| Zest branch coverage (total / valid) | 148 / 145 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 17 |
| Failing on fixed | 0 |
| Failing on buggy | 2 |
| Bug detection | Fail (triggering: 2) |
| **Fault detected** | **yes** |
| Line coverage | 14 / 16 = 87.50% |
| Branch coverage | 4 / 6 = 66.67% |
| Test suite length (statements) | 17 |
| Mutation score | 2 / 3 = 66.67% |
| Fuzz time | 126s |
| Pipeline time | 210s |

## Failing tests on buggy version

- org.apache.commons.collections.functors.EqualPredicate_Zest_Test::test10
- org.apache.commons.collections.functors.EqualPredicate_Zest_Test::test14

```
--- org.apache.commons.collections.functors.EqualPredicate_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<0=[!java.lang.NullPointerException]
> but was:<0=[java.lang.Boolean:true |this=org.apache.commons.collections.functors.EqualPredicate:@?
1=null |this=org.apache.commons.collections.functors.EqualPredicate:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.collections.functors.EqualPredicate_Zest_Test.test10(EqualPredicate_Zest_Test.java:77)
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
--- org.apache.commons.collections.functors.EqualPredicate_Zest_Test::test14
junit.framework.AssertionFailedError: expected:<....NullPredicate:@?
1=[!java.lang.NullPointerException]
> but was:<....NullPredicate:@?
1=[java.lang.Boolean:true |this=org.apache.commons.collections.functors.EqualPredicate:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.collections.functors.EqualPredicate_Zest_Test.test14(EqualPredicate_Zest_Test.java:105)
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

- Suite: `/home/user/suites/Collections/zest/17/t12001/Collections-17f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_17/b120_r1/src/org/apache/commons/collections/functors/EqualPredicate_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_17/b120_r1`
