import java.io.Serializable;

public class OctPoint implements Serializable {
    private Object x;
    private Object y;
    private Object z;
    private Object clusteringKey;
    private String reference;
    public OctPoint(Object x, Object y, Object z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Object getX() {
        return x;
    }

    public Object getY() {
        return y;
    }

    public Object getZ() {
        return z;
    }

}
