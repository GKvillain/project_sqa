# Zest – Lang-6 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 135343 (valid 84.03%) |
| Cycles | 17 |
| Corpus | 41 |
| Zest branch coverage (total / valid) | 240 / 237 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 40 |
| Failing on fixed | 0 |
| Failing on buggy | 19 |
| Bug detection | Fail (triggering: 19) |
| **Fault detected** | **yes** |
| Line coverage | 23 / 30 = 76.67% |
| Branch coverage | 10 / 12 = 83.33% |
| Test suite length (statements) | 49 |
| Mutation score | 29 / 42 = 69.05% |
| Fuzz time | 120s |
| Pipeline time | 262s |

## Failing tests on buggy version

- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test10
- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test12
- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test14
- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test15
- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test16
- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test18
- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test19
- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test22
- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test23
- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test26
- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test31
- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test36
- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test37
- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test38
- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test39
- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test2
- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test4
- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test8
- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test9

```
--- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<...va.lang.String:2A
1=[void |a1=java.io.StringWriter:􆚁 |this=org.apache.commons.lang3.StringEscapeUtils$CsvEscaper:@?]
> but was:<...va.lang.String:2A
1=[!java.lang.StringIndexOutOfBoundsException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test.test10(CharSequenceTranslator_Zest_Test.java:77)
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
--- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test12
junit.framework.AssertionFailedError: expected:<0=[java.lang.String:􋝬񖛏 |this=org.apache.commons.lang3.StringEscapeUtils$CsvEscaper:@?
1=!java.lang.IllegalState]Exception
> but was:<0=[!java.lang.StringIndexOutOfBounds]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test.test12(CharSequenceTranslator_Zest_Test.java:91)
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

- Suite: `/home/pilaiphon/suites/Lang/zest/6/t12001/Lang-6f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Lang_6/b120_r1/src/org/apache/commons/lang3/text/translate/CharSequenceTranslator_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Lang_6/b120_r1`
