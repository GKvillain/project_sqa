# Zest – JxPath-7 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 354536 (valid 94.03%) |
| Cycles | 28 |
| Corpus | 50 |
| Zest branch coverage (total / valid) | 209 / 206 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 51 |
| Failing on fixed | 0 |
| Failing on buggy | 43 |
| Bug detection | Fail (triggering: 43) |
| **Fault detected** | **yes** |
| Line coverage | 44 / 56 = 78.57% |
| Branch coverage | 28 / 40 = 70.00% |
| Test suite length (statements) | 51 |
| Mutation score | 40 / 60 = 66.67% |
| Fuzz time | 120s |
| Pipeline time | 194s |

## Failing tests on buggy version

- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test10
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test11
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test13
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test14
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test16
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test17
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test18
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test21
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test22
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test23
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test24
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test25
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test27
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test28
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test29
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test30
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test31
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test32
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test33
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test34
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test36
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test37
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test38
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test40
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test41
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test42
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test43
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test44
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test45
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test46
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test47
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test48
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test49
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test50
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test0
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test1
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test2
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test4
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test5
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test6
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test7
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test8
- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test9

```
--- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<...lPointerException
1=[java.lang.Boolean:false |this=org.apache.commons.jxpath.ri.compiler.CoreOperationLessThan:!toString:java.lang.NullPointer]Exception
> but was:<...lPointerException
1=[!H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test.test10(CoreOperationGreaterThan_Zest_Test.java:77)
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
--- org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<0=![java.lang.NullPointer]Exception
> but was:<0=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan_Zest_Test.test11(CoreOperationGreaterThan_Zest_Test.java:84)
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

- Suite: `/home/user/suites/JxPath/zest/7/t12001/JxPath-7f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_7/b120_r1/src/org/apache/commons/jxpath/ri/compiler/CoreOperationGreaterThan_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_7/b120_r1`
