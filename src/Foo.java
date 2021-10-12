public class Foo {
    public static void main(String[] args) {
        new Sub().test(1);
    }

    static class Super<A, B> {
        void test(B b) {}
    }
    static class Sub<A> extends Super<A, String> {
        @Override
        void test(String s) {
            super.test(s);
        }
    }
}
