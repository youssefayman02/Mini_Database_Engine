import java.io.Serializable;

public class Null implements Serializable, Comparable {

    @Override
    public String toString() {
        return "Null";
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}

