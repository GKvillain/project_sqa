package zestd4j;

// Generic call harness shared by the fuzz driver (AutoFuzz) and the generated JUnit tests
// (the test writer copies this class into each test as a nested class "Z").
// Keep it Java-6 compatible and free of imports: no diamond, lambdas, String switch, multi-catch.
//
// A target descriptor is one of
//   M|decl.Class|method|t1,t2|RECEIVER     RECEIVER = static | new:Cls:t1,t2 | fac:Cls:method:t1,t2 | fld:Cls:FIELD
//   C|decl.Class|<init>|t1,t2
// run() executes up to 3 targets in sequence (same receiver objects reused) and returns an
// observation string: return values, exceptions, mutated arguments and receiver state.
public class Harness {
    public static boolean invalid;
    static final int MAXS = 300;

    public static String run(String[] targets, int n, int w0, int w1, int w2,
                             String s0, String s1, String s2, int i0, int i1, int i2, long l0, long l1,
                             boolean b0, boolean b1, char c0, char c1, double d0, double d1) throws Throwable {
        Pool p = new Pool(new String[]{s0, s1, s2}, new int[]{i0, i1, i2}, new long[]{l0, l1},
                          new boolean[]{b0, b1}, new char[]{c0, c1}, new double[]{d0, d1});
        int[] ws = {w0, w1, w2};
        java.util.Map<String, Object> recv = new java.util.HashMap<String, Object>();
        StringBuilder obs = new StringBuilder();
        invalid = false;
        if (n < 1) n = 1;
        if (n > 3) n = 3;
        for (int k = 0; k < n; k++) {
            String desc = targets[idx(ws[k], targets.length)];
            p.reset();
            String r;
            try {
                r = call(desc, p, recv);
            } catch (java.lang.reflect.InvocationTargetException e) {
                Throwable t = e.getCause() == null ? e : e.getCause();
                if (t instanceof Error) throw t;
                if (k == 0 && t instanceof RuntimeException) invalid = true;
                r = "!" + t.getClass().getName();
            }
            obs.append(k).append('=').append(r).append('\n');
            if (r.startsWith("!")) break;
        }
        return obs.toString();
    }

    static String call(String desc, Pool p, java.util.Map<String, Object> recv) throws Throwable {
        String[] f = desc.split("\\|", -1);
        Class<?> c;
        Class<?>[] ts;
        try {
            c = cls(f[1]);
            ts = types(f[3]);
        } catch (Exception e) {
            return "!H:" + e.getClass().getName();
        }
        try {
            if (f[0].equals("C")) {
                java.lang.reflect.Constructor<?> k = c.getDeclaredConstructor(ts);
                k.setAccessible(true);
                Object[] a = args(ts, p);
                Object o = k.newInstance(a);
                return "new " + show(o) + mut(a);
            }
            java.lang.reflect.Method m = c.getDeclaredMethod(f[2], ts);
            m.setAccessible(true);
            Object target = null;
            if (!java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
                target = recv.get(f[4]);
                if (target == null) {
                    target = receiver(f[4], p);
                    if (target == null) return "!H:norecv";
                    recv.put(f[4], target);
                }
            }
            Object[] a = args(ts, p);
            Object r = m.invoke(target, a);
            return (m.getReturnType() == void.class ? "void" : show(r)) + mut(a)
                   + (target == null ? "" : " |this=" + show(target));
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Exception e) {   // reflection problems: wrong arg type, abstract class, ...
            return "!H:" + e.getClass().getName();
        }
    }

    static Object receiver(String spec, Pool p) throws Exception {
        String[] r = spec.split(":", -1);
        if (r[0].equals("new")) {
            Class<?>[] ts = types(r[2]);
            java.lang.reflect.Constructor<?> k = cls(r[1]).getDeclaredConstructor(ts);
            k.setAccessible(true);
            return k.newInstance(args(ts, p));
        }
        if (r[0].equals("fac")) {
            Class<?>[] ts = types(r[3]);
            java.lang.reflect.Method m = cls(r[1]).getDeclaredMethod(r[2], ts);
            m.setAccessible(true);
            return m.invoke(null, args(ts, p));
        }
        if (r[0].equals("fld")) {
            java.lang.reflect.Field fl = cls(r[1]).getDeclaredField(r[2]);
            fl.setAccessible(true);
            return fl.get(null);
        }
        return null;
    }

    // ---------------------------------------------------------------- arguments
    static final class Pool {
        String[] s; int[] i; long[] l; boolean[] b; char[] c; double[] d;
        int si, ii, li, bi, ci, di;
        Pool(String[] s, int[] i, long[] l, boolean[] b, char[] c, double[] d) {
            this.s = s; this.i = i; this.l = l; this.b = b; this.c = c; this.d = d;
        }
        void reset() { si = ii = li = bi = ci = di = 0; }
        String s() { return s[si++ % s.length]; }
        int i() { return i[ii++ % i.length]; }
        long l() { return l[li++ % l.length]; }
        boolean b() { return b[bi++ % b.length]; }
        char c() { return c[ci++ % c.length]; }
        double d() { return d[di++ % d.length]; }
    }

    static final java.util.Locale[] LOCALES = {
        java.util.Locale.US, java.util.Locale.FRANCE, java.util.Locale.GERMANY, java.util.Locale.JAPAN,
        java.util.Locale.CHINA, new java.util.Locale("th", "TH"), new java.util.Locale("en", "GB"), new java.util.Locale("")};
    static final String[] TZS = {"UTC", "GMT", "America/Los_Angeles", "Asia/Bangkok", "Europe/London",
        "GMT+05:30", "Australia/Sydney"};
    static final Class<?>[] CLASSES = {String.class, Integer.class, int.class, Object.class, java.util.List.class,
        int[].class, String[].class, Number.class, Comparable.class, java.util.ArrayList.class,
        java.util.Date.class, Thread.State.class, Long.class, char.class};

    static int idx(int x, int n) { return n <= 0 ? 0 : ((x % n) + n) % n; }

    static Class<?>[] types(String s) throws ClassNotFoundException {
        if (s == null || s.length() == 0) return new Class<?>[0];
        String[] parts = s.split(",");
        Class<?>[] r = new Class<?>[parts.length];
        for (int j = 0; j < parts.length; j++) r[j] = cls(parts[j]);
        return r;
    }

    static Class<?> cls(String n) throws ClassNotFoundException {
        if (n.equals("int")) return int.class;
        if (n.equals("long")) return long.class;
        if (n.equals("boolean")) return boolean.class;
        if (n.equals("char")) return char.class;
        if (n.equals("double")) return double.class;
        if (n.equals("float")) return float.class;
        if (n.equals("short")) return short.class;
        if (n.equals("byte")) return byte.class;
        return Class.forName(n, false, Harness.class.getClassLoader());
    }

    static Object[] args(Class<?>[] ts, Pool p) {
        Object[] a = new Object[ts.length];
        for (int j = 0; j < ts.length; j++) a[j] = arg(ts[j], p);
        // ~1/7 of calls: pass null for the last reference parameter (null-handling bugs)
        if (idx(p.i[2], 7) == 0) {
            for (int j = ts.length - 1; j >= 0; j--) if (!ts[j].isPrimitive()) { a[j] = null; break; }
        }
        return a;
    }

    static String[] split(String s) {
        String[] parts = s.split(",", -1);
        if (parts.length > 8) {
            String[] r = new String[8];
            System.arraycopy(parts, 0, r, 0, 8);
            return r;
        }
        return parts;
    }

    static Object arg(Class<?> t, Pool p) {
        if (t == String.class || t == CharSequence.class || t == Object.class || t == Comparable.class
            || t == java.io.Serializable.class) return p.s();
        if (t == int.class || t == Integer.class) return Integer.valueOf(p.i());
        if (t == long.class || t == Long.class) return Long.valueOf(p.l());
        if (t == boolean.class || t == Boolean.class) return Boolean.valueOf(p.b());
        if (t == char.class || t == Character.class) return Character.valueOf(p.c());
        if (t == double.class || t == Double.class) return Double.valueOf(p.d());
        if (t == float.class || t == Float.class) return Float.valueOf((float) p.d());
        if (t == short.class || t == Short.class) return Short.valueOf((short) p.i());
        if (t == byte.class || t == Byte.class) return Byte.valueOf((byte) p.i());
        if (t == Number.class) return p.b() ? (Object) Integer.valueOf(p.i()) : (Object) Double.valueOf(p.d());
        if (t == StringBuffer.class) return new StringBuffer(p.s());
        if (t == StringBuilder.class) return new StringBuilder(p.s());
        if (t == java.io.Writer.class || t == java.io.StringWriter.class) return new java.io.StringWriter();
        if (t == java.util.Random.class) return new java.util.Random(p.l());
        if (t == java.util.Locale.class) return LOCALES[idx(p.i(), LOCALES.length)];
        if (t == java.util.TimeZone.class) return java.util.TimeZone.getTimeZone(TZS[idx(p.i(), TZS.length)]);
        if (t == java.util.Date.class) return new java.util.Date(p.l());
        if (t == java.util.Calendar.class || t == java.util.GregorianCalendar.class) {
            java.util.Calendar cal = new java.util.GregorianCalendar(
                java.util.TimeZone.getTimeZone(TZS[idx(p.i(), TZS.length)]), java.util.Locale.US);
            cal.setTimeInMillis(p.l());
            return cal;
        }
        if (t == Class.class || t == java.lang.reflect.Type.class) return CLASSES[idx(p.i(), CLASSES.length)];
        if (t.isEnum()) {
            Object[] v = t.getEnumConstants();
            return v == null || v.length == 0 ? null : v[idx(p.i(), v.length)];
        }
        if (t == java.util.List.class || t == java.util.Collection.class || t == Iterable.class
            || t == java.util.ArrayList.class)
            return new java.util.ArrayList<Object>(java.util.Arrays.asList((Object[]) split(p.s())));
        if (t == java.util.Set.class || t == java.util.SortedSet.class || t == java.util.TreeSet.class)
            return new java.util.TreeSet<Object>(java.util.Arrays.asList((Object[]) split(p.s())));
        if (t == java.util.Iterator.class)
            return java.util.Arrays.asList((Object[]) split(p.s())).iterator();
        if (t == java.util.Map.class || t == java.util.HashMap.class) {
            java.util.Map<Object, Object> m = new java.util.HashMap<Object, Object>();
            if (p.b()) {
                String[] parts = split(p.s());
                for (int j = 0; j + 1 < parts.length; j += 2) m.put(parts[j], parts[j + 1]);
            }
            return m;
        }
        if (t.isArray()) return arrayFrom(t.getComponentType(), p.s(), p);
        return null;   // unsupported type: null
    }

    static Object arrayFrom(Class<?> ct, String s, Pool p) {
        if (ct == char.class) return s.toCharArray();
        if (ct == String.class || ct == CharSequence.class || ct == Object.class) {
            String[] parts = split(s);
            Object arr = java.lang.reflect.Array.newInstance(ct, parts.length);
            for (int j = 0; j < parts.length; j++) java.lang.reflect.Array.set(arr, j, parts[j]);
            return arr;
        }
        if (ct.isPrimitive()) {
            int len = Math.min(s.length(), 8);
            Object arr = java.lang.reflect.Array.newInstance(ct, len);
            for (int j = 0; j < len; j++) {
                int x = s.charAt(j) - '0';
                if (ct == int.class) java.lang.reflect.Array.setInt(arr, j, x);
                else if (ct == long.class) java.lang.reflect.Array.setLong(arr, j, x);
                else if (ct == short.class) java.lang.reflect.Array.setShort(arr, j, (short) x);
                else if (ct == byte.class) java.lang.reflect.Array.setByte(arr, j, (byte) x);
                else if (ct == double.class) java.lang.reflect.Array.setDouble(arr, j, x / 2.0);
                else if (ct == float.class) java.lang.reflect.Array.setFloat(arr, j, x / 2.0f);
                else if (ct == boolean.class) java.lang.reflect.Array.setBoolean(arr, j, (x & 1) == 1);
            }
            return arr;
        }
        if (ct.isArray()) {                       // 2-D: rows separated by ';'
            String[] rows = s.split(";", -1);
            int len = Math.min(rows.length, 6);
            Object arr = java.lang.reflect.Array.newInstance(ct, len);
            for (int j = 0; j < len; j++) java.lang.reflect.Array.set(arr, j, arrayFrom(ct.getComponentType(), rows[j], p));
            return arr;
        }
        int len = idx(p.i(), 4);
        Object arr = java.lang.reflect.Array.newInstance(ct, len);
        for (int j = 0; j < len; j++) {
            Object v = arg(ct, p);
            if (v != null && !ct.isInstance(v)) v = null;
            java.lang.reflect.Array.set(arr, j, v);
        }
        return arr;
    }

    // ---------------------------------------------------------------- observation
    static String show(Object o) {
        if (o == null) return "null";
        String s;
        try {
            if (o.getClass().isArray()) s = java.util.Arrays.deepToString(new Object[]{o});
            else if (o instanceof Class) s = ((Class<?>) o).getName();
            else s = String.valueOf(o);
        } catch (Throwable t) {
            s = "!toString:" + t.getClass().getName();
        }
        if (s.matches("(?s).*@[0-9a-f]{4,8}.*")) s = "@?";          // identity hash: not stable
        if (s.length() > MAXS) s = s.substring(0, MAXS) + "...";
        return o.getClass().getName() + ":" + s;
    }

    static String mut(Object[] a) {
        StringBuilder b = new StringBuilder();
        for (int j = 0; j < a.length; j++) {
            Object o = a[j];
            if (o instanceof java.io.StringWriter || o instanceof StringBuffer || o instanceof StringBuilder
                || (o != null && o.getClass().isArray()))
                b.append(" |a").append(j).append('=').append(show(o));
        }
        return b.toString();
    }
}
