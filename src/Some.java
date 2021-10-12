import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;

import java.util.Collections;
import java.util.List;

public class Some {
    private static final List ALL;
    public static final Some.Companion Companion = new Some.Companion((DefaultConstructorMarker)null);

    static {
        ALL = CollectionsKt.listOf(Some.A.INSTANCE);
    }

    public static final class A extends Some {
        public static final Some.A INSTANCE;

        private A() {
        }

        static {
            Some.A var0 = new Some.A();
            INSTANCE = var0;
        }
    }

    public static final class Companion {
        public final List getALL() {
            return Some.ALL;
        }

        private Companion() {
        }

        // $FF: synthetic method
        public Companion(DefaultConstructorMarker $constructor_marker) {
            this();
        }
    }
}

