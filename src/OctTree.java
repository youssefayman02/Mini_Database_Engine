import java.io.*;
import java.util.*;

public class OctTree implements Serializable {
    private OctNode root;

    public OctTree(OctNode root) {
        this.root = root;
    }

    public void insert(OctPoint Point)
    {

    }

    public void delete (OctPoint Point)
    {

    }

    public boolean search (OctPoint Point)
    {
        return false;
    }

    public Object getMedian (Object operand1, Object operand2)
    {
        if (operand1 instanceof java.lang.Integer)
        {
            return (((Integer) operand1) + ((Integer) operand2)) / 2;
        }
        else if (operand1 instanceof java.lang.Double) {
            return (((Integer) operand1) + ((Integer) operand2)) / 2;
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
