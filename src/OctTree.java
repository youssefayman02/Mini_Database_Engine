import java.io.*;
import java.util.*;

public class OctTree implements Serializable {
    private OctNode root;

    public OctTree(OctNode root) {
        this.root = root;
    }

    public void insert (Object o1,Object o2,Object o3,String reference,Object clusteringKey)
    {
        OctPoint point = new OctPoint(o1, o2, o3, clusteringKey, reference);
        root.insert(point, true);
    }

    public void printTree()
    {
        root.printTree();
    }

    public static void main(String[] args) {
        OctNode root = new OctNode(1,100,1.0,100.0,"aaaaa","zzzzz", true);
        OctTree tree = new OctTree(root);
        tree.insert(2,new Double(2.0),"bbbbd","reference",5);
        tree.insert(12,new Double(80.0),"ccccd","reference",5);
        tree.insert(30,new Double(23.0),"mzzzz","reference",5);
        tree.insert(40,new Double(75.5),"eeeed","reference",5);
        tree.insert(31,new Double(10.0),"bbbbs","reference",5);
        tree.insert(45,new Double(25.0),"bbscb","reference",5);
        tree.insert(55,new Double(56.0),"cccc","reference",5);
        tree.insert(31,new Double(63.0),"mzze","reference",5);
        tree.insert(92,new Double(82.5),"eeve","reference",5);
        tree.insert(10,new Double(70.0),"beeb","reference",5);
        tree.insert(85,new Double(60.0),"bedb","reference",5);
        tree.insert(51,new Double(95.5),"cccc","reference",5);
        tree.insert(23,new Double(23.0),"mzzzz","reference",5);
        tree.insert(42,new Double(16.5),"eeese","reference",5);
        tree.insert(30,new Double(12.0),"bbdbb","reference",5);
        tree.insert(28,new Double(34.0),"bbgbb","reference",5);
        tree.insert(55,new Double(44.0),"ccwcc","reference",5);
        tree.insert(63,new Double(98.0),"meziz","reference",5);
        tree.insert(67,new Double(93.5),"eyeee","reference",5);
        tree.insert(98,new Double(73.0),"seif","reference",5);

        tree.printTree();

    }
}
