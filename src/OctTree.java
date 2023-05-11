import java.io.*;
import java.util.*;

public class OctTree implements Serializable {
    private OctNode root;
    private String indexedCol1;
    private String indexedCol2;
    private String indexedCol3;

    public OctTree(OctNode root, String indexedCol1, String indexedCol2, String indexedCol3)
    {
        this.root = root;
        this.indexedCol1 = indexedCol1;
        this.indexedCol2 = indexedCol2;
        this.indexedCol3 = indexedCol3;
    }

    public void insert (Object o1,Object o2,Object o3,Object clusteringKey, String reference)
    {
        OctPoint point = new OctPoint(o1, o2, o3, clusteringKey, reference);
        root.insert(point);
    }

    public void delete (Object o1,Object o2, Object o3, Object clusteringKey, String reference)
    {
        OctPoint point = new OctPoint(o1,o2,o3, clusteringKey, reference);
        root.delete(point);
    }


    public boolean find (Object o1, Object o2, Object o3, String reference, Object clusteringKey)
    {
        OctPoint point = new OctPoint(o1, o2, o3, clusteringKey, reference);
        return root.find(point);
    }

    public void printTree()
    {
        root.printTree();
    }

    public String getIndexedCol1() {
        return indexedCol1;
    }

    public void setIndexedCol1(String indexedCol1) {
        this.indexedCol1 = indexedCol1;
    }

    public String getIndexedCol2() {
        return indexedCol2;
    }

    public void setIndexedCol2(String indexedCol2) {
        this.indexedCol2 = indexedCol2;
    }

    public String getIndexedCol3() {
        return indexedCol3;
    }

    public void setIndexedCol3(String indexedCol3) {
        this.indexedCol3 = indexedCol3;
    }

    public OctNode getRoot() {
        return root;
    }

    public void setRoot(OctNode root) {
        this.root = root;
    }

    public static void main(String[] args) {
        OctNode root = new OctNode(1,100,1.0,100.0,"aaaaa","zzzzz", true, null);
        OctTree tree = new OctTree(root,"","","");
        tree.insert(2,new Double(2.0),"bbbbd",5,"reference");
        tree.insert(12,new Double(80.0),"ccccc",5,"reference");
        tree.insert(49,new Double(40.0),"ccccd",5,"reference");
        tree.insert(23,new DBAppNull(),"mzzzz",5,"reference");
        tree.insert(42,new Double(16.5),new DBAppNull(),5,"reference");
        tree.insert(30,new Double(23.0),"mzzzz",5,"reference");
        tree.insert(30,new Double(23.0),"mzzzz",5,"reference1");
        tree.insert(30,new Double(23.0),"mzzzz",5,"reference2");
        tree.insert(30,new Double(23.0),"mzzzz",5,"reference3");
        tree.insert(40,new Double(75.5),"eeeed",5,"reference");
        tree.insert(31,new Double(10.0),"bbbbs",5,"reference");
        tree.insert(45,new Double(25.0),"bbscb",5,"reference");
        tree.insert(55,new Double(56.0),"cccc",5,"reference");
//        tree.insert(23,new DBAppNull(),"mzzzz",5,"reference");
//        tree.insert(42,new Double(16.5),new DBAppNull(),5,"reference");
//        tree.insert(1,new Double(5.0),"aaaaa",5,"reference");
//        tree.insert(28,new DBAppNull(),"bbgbb",5,"reference");
//        tree.insert(55,new Double(44.0),new DBAppNull(),5,"reference");
//        tree.insert(63,new DBAppNull(),"meziz",5,"reference");
//        tree.insert(new DBAppNull(),new Double(93.5),"eyeee",5,"reference");
//        tree.insert(new DBAppNull(),new Double(73.0),"seiff",5,"reference");
//        tree.insert(63,new DBAppNull(),"meziz",5,"reference1");
//        tree.insert(new DBAppNull(),new Double(93.5),"eyeee",5,"reference2");
//        tree.insert(new DBAppNull(),new Double(73.0),"seiff",5,"reference3");
        System.out.println("Before---------------------------------------");
        tree.printTree();
        tree.delete(30,new Double(23.0),"mzzzz",5,"reference");
        tree.delete(40,new Double(75.5),"eeeed",5,"reference");
        tree.delete(31,new Double(10.0),"bbbbs",5,"reference");
        System.out.println("After-----------------------------------------");
        tree.printTree();
//         tree.insert(55,new Double(56.0),"cccc",5,"reference");
//        tree.insert(31,new Double(63.0),"mzze",5,"reference");
//        tree.insert(92,new Double(82.5),"eeve",5,"reference");
//        tree.insert(10,new Double(70.0),"beeb",5,"reference");
//        tree.insert(85,new Double(60.0),"bedb",5,"reference");
//        tree.insert(51,new Double(95.5),"cccc",5,"reference");
//        tree.insert(23,new Double(23.0),"mzzzz",5,"reference");
//        tree.insert(42,new Double(16.5),"eeese",5,"reference");
//        tree.insert(30,new Double(12.0),"bbdbb",5,"reference");
//        tree.insert(28,new Double(34.0),"bbgbb",5,"reference");
//        tree.insert(30,new Double(12.0),"bbdbb",5,"reference1");
//        tree.insert(28,new Double(34.0),"bbgbb",5,"reference1");
//        tree.insert(30,new Double(12.0),"bbdbb",5,"reference2");
//        tree.insert(28,new Double(34.0),"bbgbb",5,"reference2");
//        tree.insert(55,new Double(44.0),"ccwcc",5,"reference");
//        tree.insert(63,new Double(98.0),"meziz",5,"reference");
//        tree.insert(67,new Double(93.5),"eyeee",5,"reference");
//        tree.insert(98,new Double(73.0),"seiff",5,"reference");

    }
}
