import java.io.Serializable;

public class OctPoint implements Serializable {
    private Object x;
    private Object y;
    private Object z;
    private Object clusteringKey;
    private String reference;
    public OctPoint(Object x, Object y, Object z,Object clusteringKey, String reference)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.clusteringKey = clusteringKey;
        this.reference = reference;
    }

    public Object getX() {
        return x;
    }

    public void setX(Object x) {
        this.x = x;
    }

    public Object getY() {
        return y;
    }

    public void setY(Object y) {
        this.y = y;
    }

    public Object getZ() {
        return z;
    }

    public void setZ(Object z) {
        this.z = z;
    }

    public Object getClusteringKey() {
        return clusteringKey;
    }

    public void setClusteringKey(Object clusteringKey) {
        this.clusteringKey = clusteringKey;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public String toString() {
        return "X:" + this.x + " Y:" + this.y + " Z:" + this.z +" clusteringKey:" + this.clusteringKey + " Reference:" + this.reference;
    }
}
