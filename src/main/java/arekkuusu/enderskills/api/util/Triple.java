package arekkuusu.enderskills.api.util;

import java.util.Objects;

public class Triple<L, C, R> {
    final L l;
    final C c;
    final R r;

    public Triple(L l, C c, R r) {
        this.l = l;
        this.c = c;
        this.r = r;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Triple)) return false;
        Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
        return l.equals(triple.l) && c.equals(triple.c) && r.equals(triple.r);
    }

    @Override
    public int hashCode() {
        return Objects.hash(l, c, r);
    }
}
