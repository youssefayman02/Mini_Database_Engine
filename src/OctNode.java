import java.io.Serializable;
import java.util.Vector;

public class OctNode implements Serializable{
    //min x
    private Object x1;
    //max x
    private Object x2;
    //min y
    private Object y1;
    //max y
    private Object y2;
    //min z
    private Object z1;
    //max z
    private Object z2;
    private Vector<OctPoint> storedData;
    private OctNode[] children = new OctNode[8];

    public OctNode (Object x1, Object x2, Object y1, Object y2, Object z1, Object z2)
    {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.z1 = z1;
        this.z2 = z2;

    }

    public Object getX1() {
        return x1;
    }

    public void setX1(Object x1) {
        this.x1 = x1;
    }

    public Object getX2() {
        return x2;
    }

    public void setX2(Object x2) {
        this.x2 = x2;
    }

    public Object getY1() {
        return y1;
    }

    public void setY1(Object y1) {
        this.y1 = y1;
    }

    public Object getY2() {
        return y2;
    }

    public void setY2(Object y2) {
        this.y2 = y2;
    }

    public Object getZ1() {
        return z1;
    }

    public void setZ1(Object z1) {
        this.z1 = z1;
    }

    public Object getZ2() {
        return z2;
    }

    public void setZ2(Object z2) {
        this.z2 = z2;
    }

    public Vector<OctPoint> getStoredData() {
        return storedData;
    }

    public void setStoredData(Vector<OctPoint> storedData) {
        this.storedData = storedData;
    }

    public OctNode[] getChildren() {
        return children;
    }

    public void setChildren(OctNode[] children) {
        this.children = children;
    }
}
