package arekkuusu.enderskills.api.util;

import java.util.Objects;

public class Pair<L, R> {
    public final L l;
    public final R r;

    public Pair(L l, R r) {
        this.l = l;
        this.r = r;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return l.equals(pair.l) && r.equals(pair.r);
    }

    @Override
    public int hashCode() {
        return Objects.hash(l, r);
    }
}
