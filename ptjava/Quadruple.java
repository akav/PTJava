package ptjava;

public class Quadruple<T0, T1, T2, T3> {
    public final T0 _0;
    public final T1 _1;
    public final T2 _2;
    public final T3 _3;

    public Quadruple(T0 _0, T1 _1, T2 _2, T3 _3) {
        this._0 = _0;
        this._1 = _1;
        this._2 = _2;
        this._3 = _3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Quadruple<?, ?, ?, ?> quadruple = (Quadruple<?, ?, ?, ?>) o;

        if (_0 != null ? !_0.equals(quadruple._0) : quadruple._0 != null) return false;
        if (_1 != null ? !_1.equals(quadruple._1) : quadruple._1 != null) return false;
        if (_2 != null ? !_2.equals(quadruple._2) : quadruple._2 != null) return false;
        return _3 != null ? _3.equals(quadruple._3) : quadruple._3 == null;
    }

    @Override
    public int hashCode() {
        int result = _0 != null ? _0.hashCode() : 0;
        result = 31 * result + (_1 != null ? _1.hashCode() : 0);
        result = 31 * result + (_2 != null ? _2.hashCode() : 0);
        result = 31 * result + (_3 != null ? _3.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "(" + _0 + ',' + _1 + ',' + _2 + ',' + _3 + ')';
    }
}