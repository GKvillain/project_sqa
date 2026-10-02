# Zest – Closure-65 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 402771 (valid 63.47%) |
| Cycles | 13 |
| Corpus | 107 |
| Zest branch coverage (total / valid) | 215 / 211 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 107 |
| Failing on fixed | 0 |
| Failing on buggy | 30 |
| Bug detection | Fail (triggering: 30) |
| **Fault detected** | **yes** |
| Line coverage | 107 / 595 = 17.98% |
| Branch coverage | 47 / 451 = 10.42% |
| Test suite length (statements) | 107 |
| Mutation score | 1010 / 1130 = 89.38% |
| Fuzz time | 122s |
| Pipeline time | 240s |

## Failing tests on buggy version


```
--- com.google.javascript.jscomp.CodeGenerator_Zest_Test::test100
junit.framework.AssertionFailedError: expected:<...java.lang.String:䨀\0[00\000\000\ud9a3\uddbb\udacb\udcb1\udba1\udd04\000\000\000\000\000\000\000\000\000\ud865\udf56\ud8a1\udf05\000\000\000\000c\t\ud90d\udc25\udb9e\udc66\000\000\ud957\udc2b\ud95d\ude69\udb2e\udd09䨀
1=java.lang.String:䨀\000\000\000\ud9a3\uddbb\udacb\udcb1\udba1\udd04\000\000\000\000\000\000\000\000\000\ud865\udf56\ud8a1\udf05\000\000\000\000c\t\ud90d\udc25\udb9e\udc66\000\00]0\ud957\udc2b\ud95d\...> but was:<...java.lang.String:䨀\0[\0\0\ud9a3\uddbb\udacb\udcb1\udba1\udd04\0\0\0\0\0\0\0\0\0\ud865\udf56\ud8a1\udf05\0\0\0\0c\t\ud90d\udc25\udb9e\udc66\0\0\ud957\udc2b\ud95d\ude69\udb2e\udd09䨀
1=java.lang.String:䨀\0\0\0\ud9a3\uddbb\udacb\udcb1\udba1\udd04\0\0\0\0\0\0\0\0\0\ud865\udf56\ud8a1\udf05\0\0\0\0c\t\ud90d\udc25\udb9e\udc66\0\]0\ud957\udc2b\ud95d\...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.google.javascript.jscomp.CodeGenerator_Zest_Test.test100(CodeGenerator_Zest_Test.java:707)
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
--- com.google.javascript.jscomp.CodeGenerator_Zest_Test::test101
junit.framework.AssertionFailedError: expected:<...ava.lang.String:䇖>\0[00\ue5e4\udba9\udf00\uda5b\ude0a\ud8c0\udc00\uda7d\ude5a\udbe1\ude1a>\udbfd\udede\uda0a\udef0\udad5\udd96\u00b7\000\u1a1f\ud865\udf56\ud8a1\udf05\ud97b\ude0b\ud835\udca2\ud9f7\udc35\ud91b\uddfa\udb04\udfdc\n\ud87f\udefe\uda53\udf00\udb6c\udc97\uf6b9\ud957\udcf2\udbd0\ude5e\udb3c\udcbd䇖
1=java.lang.String:䇖>\000\ue5e4\udba9\udf00\uda5b\ude0a\ud8c0\udc00\uda7d\ude5a\udbe1\ude1a>\udbfd\udede\uda0a\udef0\udad5\udd96\u00b7\00]0\u1a1f\ud865\udf56\...> but was:<...ava.lang.String:䇖>\0[\ue5e4\udba9\udf00\uda5b\ude0a\ud8c0\udc00\uda7d\ude5a\udbe1\ude1a>\udbfd\udede\uda0a\udef0\udad5\udd96\u00b7\0\u1a1f\ud865\udf56\ud8a1\udf05\ud97b\ude0b\ud835\udca2\ud9f7\udc35\ud91b\uddfa\udb04\udfdc\n\ud87f\udefe\uda53\udf00\udb6c\udc97\uf6b9\ud957\udcf2\udbd0\ude5e\udb3c\udcbd䇖
1=java.lang.String:䇖>\0\ue5e4\udba9\udf00\uda5b\ude0a\ud8c0\udc00\uda7d\ude5a\udbe1\ude1a>\udbfd\udede\uda0a\udef0\udad5\udd96\u00b7\]0\u1a1f\ud865\udf56\...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.google.javascript.jscomp.CodeGenerator_Zest_Test.test101(CodeGenerator_Zest_Test.java:714)
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
--- com.google.javascript.jscomp.CodeGenerator_Zest_Test::test102
junit.framework.AssertionFailedError: expected:<...ng:嗗\uda41\udf11\t\0[00\ud9a3\uddbb\udacb\udcb1\udba1\udd04\udae0\udeea\udbe1\ude1a\ud99e\udcd5\udbfd\udede\udab2\udf37\ud8a9\udf0a\u00b7\000\u1a04\ud8c5\ude46\uda86\udfb0\ud97b\ude0b\ud835\udca2\udbe0\udd8a\ud91b\uddfa\ud8e5\ude9a\t\ud90d\udc25\ud843\uded9\ud984\ude05\uda45\udc8c\ud916\ude8c\ud94d\udcd...
```

- Suite: `/home/user/suites/Closure/zest/65/t12001/Closure-65f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_65/b120_r1/src/com/google/javascript/jscomp/CodeGenerator_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_65/b120_r1`
