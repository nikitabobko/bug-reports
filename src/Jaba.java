import kotlin.Unit;

public class Jaba {
    public static void main(String[] args) throws Exception {
        Unit call = MainKt.foo().call();
        System.out.println(call.getClass().getCanonicalName());
    }
}
