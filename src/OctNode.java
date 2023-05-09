import java.io.Serializable;
import java.util.Date;
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
    private Vector<OctPoint> storedData = new Vector<>();
    private OctNode[] children = new OctNode[8];
    private boolean isLeaf;

    public OctNode (Object x1, Object x2, Object y1, Object y2, Object z1, Object z2, boolean isLeaf)
    {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.z1 = z1;
        this.z2 = z2;
        this.isLeaf = isLeaf;
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

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    public void insert(OctPoint Point)
    {

        if (this.isLeaf())
        {
            if (this.getStoredData().size() < DBApp.MaximumEntriesinOctreeNode)
            {
                Vector<OctPoint> updatedData = this.getStoredData();
                updatedData.add(Point);
                return;
            }
            // create the children, distribute the data, set the root to non leaf
            else
            {
                this.setLeaf(false);
                Object xMid = getMedian(this.getX1(), this.getX2());
                Object yMid = getMedian(this.getY1(), this.getY2());
                Object zMid = getMedian(this.getZ1(), this.getZ2());
                // create the children of the node
                OctNode[] children = this.getChildren();
                children[0] = new OctNode(this.getX1(), xMid, this.getY1(), yMid, this.getZ1(), zMid,true);
                children[1] = new OctNode(this.getX1(), xMid, this.getY1(), yMid, zMid, this.getZ2(), true);
                children[2] = new OctNode(this.getX1(), xMid, yMid, this.getY2(), this.getZ1(), zMid, true);
                children[3] = new OctNode(this.getX1(), xMid, yMid, this.getY2(), zMid, this.getZ2(), true);
                children[4] = new OctNode(xMid, this.getX2(), this.getY1(), yMid, this.getZ1(), zMid, true);
                children[5] = new OctNode(xMid, this.getX2(), this.getY1(), yMid, zMid, this.getZ2(), true);
                children[6] = new OctNode(xMid, this.getX2(), yMid, this.getY2(), this.getZ1(), zMid, true);
                children[7] = new OctNode(xMid, this.getX2(), yMid, this.getY2(), zMid, this.getZ2(), true);
                //distribute the octPoints in that node
                this.setChildren(children);
                Vector<OctPoint> distributedData = this.getStoredData();
                distributedData.add(Point);
                for (OctPoint p : distributedData)
                {
                    int childIndex = childIndex(p, children);
                    this.children[childIndex].insert(p);
                }
                this.setStoredData(new Vector<>());
            }
        }
        else
        {
            OctNode[] children = this.getChildren();
            for (int i = 0; i < children.length; i++)
            {
                Object minX = children[i].getX1(), minY = children[i].getY1(), minZ = children[i].getZ1();
                Object maxX = children[i].getX2(), maxY = children[i].getY2(), maxZ = children[i].getZ2();

                if (withinTheRange(Point,minX,maxX,minY,maxY,minZ,maxZ))
                {
                    children[i].insert(Point);
                    return;
                }

            }

        }
    }

    public void printTree()
    {
        if (this.isLeaf)
        {
            System.out.println("Leaf/ Coordinates: minX: "+ this.getX1()+" maxX: "+this.getX2()+" minY: "+this.getY1()+" maxY: "+this.getY2()+" minZ: "+this.getZ1()+" maxZ: "+ this.getZ2());
            System.out.println("Stored Data:" + this.storedData.toString());
            return;
        }
        else
        {
            for (int i = 0; i < this.children.length; i++)
            {
                children[i].printTree();
            }
        }
    }
    public int childIndex (OctPoint Point, OctNode[] children)
    {
        for (int i = 0; i < children.length; i++)
        {
            Object minX = children[i].getX1(), minY = children[i].getY1(), minZ = children[i].getZ1();
            Object maxX = children[i].getX2(), maxY = children[i].getY2(), maxZ = children[i].getZ2();
            if (withinTheRange(Point,minX,maxX,minY,maxY,minZ,maxZ))
            {
                return i;
            }
        }
        return -1;
    }
    public Object getMedian (Object operand1, Object operand2)
    {
        if (operand1 instanceof java.lang.Integer)
        {
            return (((Integer) operand1) + ((Integer) operand2)) / 2;
        }
        else if (operand1 instanceof java.lang.Double) {
            return (((Double) operand1) + ((Double) operand2)) / 2.0;
        }
        else if (operand1 instanceof java.util.Date)
        {
            return (((Date) operand1).getTime() + ((Date) operand2).getTime()) / 2;
        }

        String str1 = (String) operand1;
        String str2 = (String) operand2;
        str1 = str1.toLowerCase();
        str2 = str2.toLowerCase();
        if (str1.length() > str2.length())
        {
            str2 += str1.substring(str2.length());
        }
        else
        {
            str1 += str2.substring(str1.length());
        }

        int[] arr = new int[str1.length()+1];
        for(int i = 0; i < str1.length(); i++)
        {
            arr[i+1] = (int)str1.charAt(i) - 97 + (int)str2.charAt(i) - 97;
        }

        for (int i = str1.length(); i >= 1; i--)
        {
            arr[i-1] += (int)arr[i] / 26;
            arr[i] %= 26;
        }

        for (int i = 0; i <= str1.length(); i++)
        {
            if ((arr[i] & 1) != 0)
            {
                if (i + 1 <= str1.length())
                {
                    arr[i+1] += 26;
                }
            }
            arr[i] = (int)arr[i] / 2;
        }
        String res = "";
        for (int i = 1; i <= str1.length(); i++)
        {
            res += (char)(arr[i] + 97);
        }
        return res;
    }

    public boolean withinTheRange (OctPoint Point,Object X1, Object X2, Object Y1, Object Y2, Object Z1, Object Z2)
    {
        return (compare(Point.getX(), X1) >= 0 && compare(Point.getX(),X2) < 0)
                && (compare(Point.getY(), Y1) >= 0 && compare(Point.getY(),Y2) < 0)
                && (compare(Point.getZ(), Z1) >= 0 && compare(Point.getZ(),Z2) < 0);
    }

    public int compare (Object o1,Object o2)
    {
        if (o1 instanceof java.lang.Integer) return ((Integer)o1).compareTo((Integer)o2);
        else if (o1 instanceof java.lang.Double) return ((Double)o1).compareTo((Double)o2);
        else if (o1 instanceof java.lang.String) return ((String)o1).compareTo((String)o2);
        else if (o1 instanceof Null) return ((Null)o1).compareTo((Null)o2);
        else return ((Date)o1).compareTo((Date)o2);

    }

    public static void main(String[] args) {

    }
}
