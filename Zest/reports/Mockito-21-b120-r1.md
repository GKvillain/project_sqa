# Zest – Mockito-21 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 294920 (valid 80.56%) |
| Cycles | 25 |
| Corpus | 32 |
| Zest branch coverage (total / valid) | 183 / 180 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 33 |
| Failing on fixed | 0 |
| Failing on buggy | 20 |
| Bug detection | Fail (triggering: 20) |
| **Fault detected** | **yes** |
| Line coverage | 26 / 26 = 100.00% |
| Branch coverage | 12 / 12 = 100.00% |
| Test suite length (statements) | 87 |
| Mutation score | 0 / 0 = NA% |
| Fuzz time | 121s |
| Pipeline time | 821s |

## Failing tests on buggy version

- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test10
- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test11
- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test12
- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test14
- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test15
- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test16
- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test18
- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test20
- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test26
- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test29
- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test30
- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test31
- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test0
- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test1
- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test2
- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test4
- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test5
- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test6
- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test7
- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test9

```
--- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<0=[java.lang.Boolean:false |a0=[Ljava.lang.Class;:[[class java.lang.Integer, int, class [I]] |a1=[Ljava.lang.Object;:[[곌뿑瑼팗绤ҫĚ㨇챆Ṑ]]
1=!org.mockito.internal.creation.instance.Instantation]Exception
> but was:<0=[!H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test.test10(ConstructorInstantiator_Zest_Test.java:77)
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
--- org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<...less constructor.
1=[java.lang.Boolean:false |a0=[Ljava.lang.Class;:[[class java.lang.Thread$State]] |a1=[Ljava.lang.Object;:[[곌뿑瑼팗绤ҫĚ㨇챆Ṑ璀芝럐밎̈́㏐ꢥ鄝뎖আ큷뾹]]]
> but was:<...less constructor.
1=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.mockito.internal.creation.instance.ConstructorInstantiator_Zest_Test.test11(ConstructorInstantiator_Zest_Test.java:84)
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

- Suite: `/home/user/suites/Mockito/zest/21/t12001/Mockito-21f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Mockito_21/b120_r1/src/org/mockito/internal/creation/instance/ConstructorInstantiator_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Mockito_21/b120_r1`
