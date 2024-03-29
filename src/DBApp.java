import javax.print.attribute.HashAttributeSet;
import java.io.*;
import java.text.*;
import java.util.*;

public class DBApp {
    static int MaximumRowsCountinTablePage;
    static int MaximumEntriesinOctreeNode;

    public void init()
    {
        //create resources directory
        File resourcesDirectory = new File("src/main/resources/data/Tables");
        if (!resourcesDirectory.exists()) resourcesDirectory.mkdirs();

        try {
            File metadata = new File("src/main/resources/metadata.csv");
            if (!metadata.exists()) metadata.createNewFile();

            FileWriter metadataWriter = new FileWriter("src/main/resources/metadata.csv", true);
            metadataWriter.append("Table Name,Column Name,Column Type,ClusteringKey,IndexName,IndexType,min,max\n");
            metadataWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void createTable(String strTableName, String strClusteringKeyColumn, Hashtable<String,String> htblColNameType, Hashtable<String,String> htblColNameMin, Hashtable<String,String> htblColNameMax ) throws DBAppException
    {
        //validate the given inputs
        for (String obj : htblColNameType.keySet())
            if (!htblColNameMin.containsKey(obj) || !htblColNameMax.containsKey(obj))
                throw new DBAppException("type");

        for (String obj : htblColNameMax.keySet())
            if (!htblColNameType.containsKey(obj) || !htblColNameMin.containsKey(obj))
                throw new DBAppException("max");

        for (String obj : htblColNameMin.keySet())
            if (!htblColNameType.containsKey(obj) || !htblColNameMax.containsKey(obj))
                throw new DBAppException("min");

        if (!htblColNameType.containsKey(strClusteringKeyColumn)) throw new DBAppException("Clustering key Column does not exist");

        File tableDirectory = new File("src/main/resources/data/Tables/"+strTableName);
        File indexDirectory = new File("src/main/resources/data/Tables/"+strTableName+"/Indices");

        if (!tableDirectory.exists()) tableDirectory.mkdir();
        else throw new DBAppException("Table already exists");

        indexDirectory.mkdirs();

        //Create table and initializing its attributes then serializing it
        Vector<Page> pages = new Vector<>();
        Vector<Integer> pagesID = new Vector<>();
        Table table = new Table(strTableName,pages,pagesID,new Vector<>(),0);
        String tablePath = "src/main/resources/data/Tables/"+strTableName+"/"+strTableName+".ser";
        Serialize(tablePath,table);

        //insert information about the table in the metadata file
        try {
            FileWriter fw = new FileWriter("src/main/resources/metadata.csv", true);
            for (String obj : htblColNameType.keySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append(strTableName + "," + obj + "," + htblColNameType.get(obj) + ",");
                if (obj.equals(strClusteringKeyColumn)) sb.append("true,");
                else sb.append("false,");
                sb.append("null,null," + htblColNameMin.get(obj) + "," + htblColNameMax.get(obj));
                fw.append(sb.toString() + "\n");
            }
            fw.close();
        } catch (IOException e) {
            throw new DBAppException();
        }
    }

    public void createIndex(String strTableName, String[] strarrColName) throws DBAppException
    {
        Object[] dataInfo = readFromCSV(strTableName);
        String primaryKey = (String) dataInfo[0];
        String clusteringType = (String) dataInfo[1];
        Hashtable<String, String> dataTypes = (Hashtable) dataInfo[2];
        Hashtable<String, Object> minValues = (Hashtable) dataInfo[3];
        Hashtable<String, Object> maxValues = (Hashtable) dataInfo[4];

        if (strarrColName.length != 3) throw new DBAppException("Can not build index on the given columns");

        validateColumnNames(strarrColName);

        for (String col : strarrColName)
        {
            if (!dataTypes.containsKey(col)) throw new DBAppException("Column does not exist");
        }

        Table table = (Table) Deserialize("src/main/resources/data/Tables/"+strTableName+"/"+strTableName+".ser");
        Vector<Integer> indicesId = table.getIndicesId();

        if (indexIsCreated(strTableName,strarrColName, table)) throw new DBAppException("Index Already Created on the given columns");

        String indexedCol1 = strarrColName[0], indexedCol2 = strarrColName[1], indexedCol3 = strarrColName[2];
        Object minX = minValues.get(indexedCol1), minY = minValues.get(indexedCol2), minZ = minValues.get(indexedCol3);
        Object maxX = maxValues.get(indexedCol1), maxY = maxValues.get(indexedCol2), maxZ = maxValues.get(indexedCol3);
        OctNode root = new OctNode(minX, maxX, minY, maxY, minZ, maxZ, true, null);
        OctTree tree = new OctTree(root, indexedCol1, indexedCol2, indexedCol3);
        int indexId = indicesId.size();

        String indexPath = "src/main/resources/data/Tables/"+strTableName+"/Indices/index"+indexId+".ser";
        indicesId.add(indexId);
        table.setIndicesId(indicesId);
        //to handle if index is created after inserting the records
        if (table.getPagesId().size() != 0)
        {
            populateRecordsInIndex(strTableName,table.getPagesId(),tree,primaryKey);
        }

        updateMetaData(strTableName,strarrColName, indexId);
        Serialize("src/main/resources/data/Tables/"+strTableName+"/"+strTableName+".ser", table);
        Serialize(indexPath, tree);
    }

    public void validateColumnNames (String[] strarrColName) throws DBAppException {

        if (strarrColName[0].equals(strarrColName[1]) || strarrColName[0].equals(strarrColName[2]) || strarrColName[1].equals(strarrColName[2]))
        {
            throw new DBAppException("Can not build index on two same columns");
        }

    }

    public void updateMetaData(String strTableName, String[] strarrColName, int indexId) throws DBAppException {
        try {
            FileReader oldMetaDataFile = new FileReader("src/main/resources/metadata.csv");
            BufferedReader br = new BufferedReader(oldMetaDataFile);
            List<String[]> rows = new ArrayList<>();
            String curLine = "";

            while ((curLine = br.readLine()) != null) {
                String[] values = curLine.split(",");
                rows.add(values);
            }

            for (String[] row : rows)
            {
                if (!row[0].equals(strTableName)) continue;

                for (String col : strarrColName)
                {
                    if (row[1].equals(col))
                    {
                        row[4] = "Index"+indexId;
                        row[5] = "Octree";
                    }
                }
            }
            FileWriter metaDataFile = new FileWriter("src/main/resources/metadata.csv");
            for (String[] row : rows)
            {
                metaDataFile.write(String.join(",", row) + "\n");
            }
            metaDataFile.close();
        }
        catch (Exception e)
        {
            throw new DBAppException("Exception thrown in updateMetaData");
        }
    }

    public void populateRecordsInIndex(String strTableName, Vector<Integer> pagesId, OctTree tree, String primaryKey) throws DBAppException {
        readConfig();
        for (Integer id : pagesId)
        {
            String path = "src/main/resources/data/Tables/"+strTableName+"/Page"+id+".ser";
            Page target = (Page) Deserialize(path);
            Vector<Hashtable<String, Object>> records = target.getRecords();
            for (Hashtable<String,Object> record : records)
            {
                String indexedCol1 = tree.getIndexedCol1(), indexedCol2 = tree.getIndexedCol2(), indexedCol3 = tree.getIndexedCol3();
                Object o1 = record.get(indexedCol1), o2 = record.get(indexedCol2), o3 = record.get(indexedCol3);
                Object clusteringKey = record.get(primaryKey);
                tree.insert(o1,o2,o3,clusteringKey,id);
            }
        }
    }

    public boolean indexIsCreated(String strTableName, String[] strarrColName, Table table) throws DBAppException {
        Vector<Integer> indicesId = table.getIndicesId();
        if (indicesId.size() == 0) return false;
        HashSet<String> hs = new HashSet<>();
        for (String s : strarrColName)
        {
            hs.add(s);
        }
        for (Integer id : indicesId)
        {
            String path = "src/main/resources/data/Tables/"+strTableName+"/Indices/index"+id+".ser";
            OctTree tree = (OctTree) Deserialize(path);
            if (hs.contains(tree.getIndexedCol1()) || hs.contains(tree.getIndexedCol2()) || hs.contains(tree.getIndexedCol3()))
            {
                return true;
            }
        }
        return false;
    }

    public void insertIntoTable(String strTableName, Hashtable<String,Object> htblColNameValue) throws DBAppException
    {
        if (htblColNameValue.isEmpty()) throw new DBAppException("Clustering key value cannot be null");

        Object[] tableInfo = readFromCSV(strTableName);

        String primaryKey = (String) tableInfo[0];

        if (!htblColNameValue.containsKey(primaryKey)) throw new DBAppException("Clustering key value cannot be null");

        Hashtable<String, String> dataTypes = (Hashtable) tableInfo[2];
        Hashtable<String, Object> minValues = (Hashtable) tableInfo[3];
        Hashtable<String, Object> maxValues = (Hashtable) tableInfo[4];

        checkColDataTypes(dataTypes, htblColNameValue);
        checkColCompatibility(dataTypes, htblColNameValue);
        checkRange(dataTypes, minValues, maxValues, htblColNameValue);
        // memic the null values
        for (String key : dataTypes.keySet()) {
            if (!htblColNameValue.containsKey(key)) {
                htblColNameValue.put(key, new DBAppNull());
            }
        }

        String tablePath = "src/main/resources/data/Tables/"+strTableName+"/"+strTableName+".ser";
        Table table = (Table) Deserialize(tablePath);
        Vector<Integer> pagesId = table.getPagesId();

        //to know whether the page I will insert record in it is full or not
        readConfig();
        Vector<String> indiciesPath = searchIfIndexExists(strTableName,table,htblColNameValue);

        //there is no pages yet in the table so we need to create our first page
        if (table.getNoPages() == 0)
        {
            String pagePath = "src/main/resources/data/Tables/"+strTableName+"/Page0.ser";
            Vector<Hashtable<String,Object>> records = new Vector<>();
            records.add(htblColNameValue);
            Page p = new Page(pagePath,0,htblColNameValue.get(primaryKey),htblColNameValue.get(primaryKey),records);
            table.setNoPages(1);
            pagesId.add(p.getPageId());
            table.setPagesId(pagesId);
            Serialize(pagePath, p);
            Serialize(tablePath,table);
            insertIntoIndex(indiciesPath, htblColNameValue, htblColNameValue.get(primaryKey), 0);
            return;
        }

        //search for the page I want to insert in
        int indexOfPage = searchForPageToInsert(pagesId,strTableName,htblColNameValue.get(primaryKey));
        String pagePath = "src/main/resources/data/Tables/"+strTableName+"/Page"+indexOfPage+".ser";
        Page p = (Page) Deserialize(pagePath);

        //case 1: There is an empty space for the record to be inserted
        if (!isFull(p.getRecords()))
        {
            //binary search to insert the record in the correct place
            Vector<Hashtable<String,Object>> records = p.getRecords();
            int insertIndexInPage = searchForRecord(records,htblColNameValue.get(primaryKey),primaryKey);
            records.add(insertIndexInPage,htblColNameValue);
            p.setMaxClusteringKey((compare(htblColNameValue.get(primaryKey),p.getMaxClusteringKey()) > 0 ? htblColNameValue.get(primaryKey) : p.getMaxClusteringKey()));
            p.setMinClusteringKey((compare(htblColNameValue.get(primaryKey),p.getMinClusteringKey()) <= 0 ? htblColNameValue.get(primaryKey) : p.getMinClusteringKey()));
            p.setRecords(records);
            Serialize(pagePath,p);
            Serialize(tablePath,table);
            insertIntoIndex(indiciesPath, htblColNameValue, htblColNameValue.get(primaryKey), indexOfPage);
            return;
        }

        //the page is full so we have to insert the record in the correct place and shift the last record
        Vector<Hashtable<String,Object>> records = p.getRecords();
        int indexToInsert = searchForRecord(records,htblColNameValue.get(primaryKey),primaryKey);
        records.add(indexToInsert,htblColNameValue);
        Hashtable<String,Object> shiftedRecord = records.lastElement();
        records.remove(records.size() - 1);
        //to handle the min and max clustering key of the page
        Hashtable<String,Object> firstRecord = records.firstElement();
        Hashtable<String,Object> lastRecord = records.lastElement();
        p.setMinClusteringKey(firstRecord.get(primaryKey));
        p.setMaxClusteringKey(lastRecord.get(primaryKey));
        p.setRecords(records);
        Serialize(pagePath,p);
        Serialize(tablePath,table);
        deleteFromIndex(indiciesPath, shiftedRecord, shiftedRecord.get(primaryKey), true);
        insertIntoIndex(indiciesPath, htblColNameValue, htblColNameValue.get(primaryKey), indexOfPage);

        //check if this page is the last page so I will have to create a new page to enter the shifted record
        if (pagesId.lastElement() == p.getPageId())
        {
            Serialize(pagePath,p);
            int newId = pagesId.lastElement() + 1;
            String newPath = "src/main/resources/data/Tables/"+strTableName+"/Page"+newId+".ser";
            Vector<Hashtable<String,Object>> newRecords = new Vector<>();
            newRecords.add(shiftedRecord);
            Object clusteringKey = shiftedRecord.get(primaryKey);
            Page newPage = new Page(newPath,newId,clusteringKey,clusteringKey,newRecords);
            Serialize(newPath, newPage);
            pagesId.add(newId);
            table.setNoPages(table.getNoPages() + 1);
            Serialize(tablePath,table);
            deleteFromIndex(indiciesPath, shiftedRecord, shiftedRecord.get(primaryKey), true);
            insertIntoIndex(indiciesPath, shiftedRecord, shiftedRecord.get(primaryKey), newId);
            return;
        }

        insertIntoTable(strTableName,shiftedRecord);

    }

    public  void deleteFromIndex (Vector<String> indicesPath, Hashtable<String,Object> htblColNameValue, Object clusteringKey, boolean deleteByClusteringKey) throws DBAppException {
        for (String path : indicesPath)
        {
            OctTree tree = (OctTree) Deserialize(path);
            Object o1 = htblColNameValue.get(tree.getIndexedCol1());
            Object o2 = htblColNameValue.get(tree.getIndexedCol2());
            Object o3 = htblColNameValue.get(tree.getIndexedCol3());
            tree.delete(o1, o2, o3, clusteringKey,deleteByClusteringKey);
            Serialize(path, tree);
        }
    }

    public void insertIntoIndex (Vector<String> indicesPath, Hashtable<String,Object> htblColNameValue, Object clusteringKey,int pageId) throws DBAppException {

        for (String path : indicesPath)
        {
            OctTree tree = (OctTree) Deserialize(path);
            Object o1 = htblColNameValue.get(tree.getIndexedCol1());
            Object o2 = htblColNameValue.get(tree.getIndexedCol2());
            Object o3 = htblColNameValue.get(tree.getIndexedCol3());
            tree.insert(o1, o2, o3, clusteringKey, pageId);
            Serialize(path, tree);
        }
    }

    public Vector<String> searchIfIndexExists(String strTableName,Table table, Hashtable<String,Object> htblColNameValue) throws DBAppException {

        Vector<String> res = new Vector<>();
        Vector<Integer> indicesId = table.getIndicesId();
        for (Integer id : indicesId)
        {
            String path = "src/main/resources/data/Tables/"+strTableName+"/Indices/index"+id+".ser";
            OctTree tree = (OctTree) Deserialize(path);
            String indexedCol1 = tree.getIndexedCol1();
            String indexedCol2 = tree.getIndexedCol2();
            String indexedCol3 = tree.getIndexedCol3();
            if (htblColNameValue.containsKey(indexedCol1) && htblColNameValue.containsKey(indexedCol2) && htblColNameValue.containsKey(indexedCol3))
            {
                res.add(path);
            }
        }

        return res;
    }

    public void updateTable(String strTableName, String strClusteringKeyValue, Hashtable<String,Object> htblColNameValue ) throws DBAppException {

        if (htblColNameValue.isEmpty()) return;

        Object[] tableInfo = readFromCSV(strTableName);

        String clusteringKey = (String) tableInfo[0];
        String clusteringType = (String) tableInfo[1];
        Hashtable<String,String> dataTypes = (Hashtable) tableInfo[2];
        Hashtable<String,Object> minValues= (Hashtable) tableInfo[3];
        Hashtable<String,Object> maxValues = (Hashtable) tableInfo[4];

        //make sure not to update the clustering key
        if (htblColNameValue.containsKey(clusteringKey))
        {
            throw new DBAppException("can not update clustering");
        }

        Object clusteringObject;
        switch (clusteringType)
        {
            case "java.lang.Integer":
                try {
                    clusteringObject = Integer.parseInt(strClusteringKeyValue);
                }catch (NumberFormatException e)
                {
                    throw new DBAppException();
                }
                break;
            case "java.lang.Double":
                try {
                    clusteringObject = Double.parseDouble(strClusteringKeyValue);
                }catch (NumberFormatException e)
                {
                    throw new DBAppException();
                }
                break;
            case "java.util.Date":
                try {
                    clusteringObject = new SimpleDateFormat("yyyy-MM-dd").parse(strClusteringKeyValue);
                } catch (ParseException e) {
                    throw new DBAppException("Parse exception in update table method ");
                }
                break;
            default:
                clusteringObject = (String)strClusteringKeyValue;
                break;
        }

        htblColNameValue.put(clusteringKey,clusteringObject);

        checkColDataTypes(dataTypes, htblColNameValue);
        checkColCompatibility(dataTypes, htblColNameValue);
        checkRange(dataTypes, minValues, maxValues, htblColNameValue);

        String tablePath = "src/main/resources/data/Tables/"+strTableName+"/"+strTableName+".ser";
        Table table = (Table) Deserialize(tablePath);
        Vector<Integer> pagesId = table.getPagesId();

        if (pagesId.size() > 0) {
            //search for the page to update in
            int indexPage = binarySearchOnPages(strTableName, pagesId, clusteringObject);

            if (indexPage == -1) throw new DBAppException("Clustering key value does not exits");

            String updatePath = "src/main/resources/data/Tables/" + strTableName + "/Page" + indexPage + ".ser";
            Page updatePage = (Page) Deserialize(updatePath);
            Vector<Hashtable<String, Object>> records = updatePage.getRecords();
            int recordIndex = searchForRecordToUpdate(records, clusteringKey, clusteringObject);

            if (recordIndex == -1) throw new DBAppException("Clustering key value does not exits");

            Hashtable<String, Object> oldRecord = records.get(recordIndex);
            Vector<String> indicesPath = searchIfIndexExists(strTableName, table, htblColNameValue);
            deleteFromIndex(indicesPath, oldRecord,oldRecord.get(clusteringKey), true);

            for (String key : htblColNameValue.keySet()) {
                records.get(recordIndex).replace(key, htblColNameValue.get(key));
            }

            Hashtable<String,Object> updatedRecord = records.get(recordIndex);
            updatePage.setRecords(records);
            Serialize(updatePath, updatePage);

            // delete old record from indices and update the new one
            insertIntoIndex(indicesPath, updatedRecord, updatedRecord.get(clusteringKey), indexPage);

        }
        Serialize(tablePath,table);
    }

    public void deleteFromTable(String strTableName, Hashtable<String,Object> htblColNameValue) throws DBAppException {

        Object[] tableInfo = readFromCSV(strTableName);

        String clusteringKey = (String) tableInfo[0];
        Hashtable<String,String> dataTypes = (Hashtable) tableInfo[2];
        Hashtable<String,Object> minValues= (Hashtable) tableInfo[3];
        Hashtable<String,Object> maxValues = (Hashtable) tableInfo[4];

        checkColDataTypes(dataTypes, htblColNameValue);
        checkColCompatibility(dataTypes, htblColNameValue);
        checkRange(dataTypes, minValues, maxValues, htblColNameValue);

        String tablePath = "src/main/resources/data/Tables/"+strTableName+"/"+strTableName+".ser";
        Table table = (Table) Deserialize(tablePath);
        Vector<Integer> pagesId = table.getPagesId();
        Vector<String> availableIndices = getAvailableIndices(strTableName, table);
        Vector<String> indicesPath = searchIfIndexExists(strTableName, table, htblColNameValue);
        boolean useIndex = indicesPath.size() != 0;
        boolean clusteringKeyExists = htblColNameValue.containsKey(clusteringKey);

        if (table.getNoPages() > 0){

            if (useIndex)
            {
                TreeSet<Integer> targetId = new TreeSet<>();
                for (String path : indicesPath)
                {
                    OctTree tree = (OctTree) Deserialize(path);
                    String indexedCol1 = tree.getIndexedCol1(), indexedCol2 = tree.getIndexedCol2(), indexedCol3 = tree.getIndexedCol3();
                    Object o1 = htblColNameValue.get(indexedCol1), o2 = htblColNameValue.get(indexedCol2), o3 = htblColNameValue.get(indexedCol3);
                    targetId.addAll(tree.search(o1, o2,o3));
                    Serialize(path, tree);
                    tree = null;
                }

                for (Integer id : targetId)
                {
                    if (!pagesId.contains(id)) continue;

                    String pagePath = "src/main/resources/data/Tables/" + strTableName + "/Page" + id + ".ser";
                    Page targetPage = (Page) Deserialize(pagePath);
                    Vector<Hashtable<String, Object>> updatedRecords = deleteRecordsFromPage(targetPage.getRecords(), htblColNameValue, availableIndices);

                    if (updatedRecords.size() == 0) {

                        File f = new File(pagePath);
                        f.delete();
                        table.setNoPages(table.getNoPages() - 1);
                        Vector<Integer> pagesID = table.getPagesId();
                        pagesID.remove(new Integer(targetPage.getPageId()));
                        table.setPagesId(pagesID);

                    } else {
                        int size = updatedRecords.size();
                        targetPage.setRecords(updatedRecords);
                        targetPage.setMinClusteringKey(updatedRecords.get(0).get(clusteringKey));
                        targetPage.setMaxClusteringKey(updatedRecords.get(size - 1).get(clusteringKey));
                        Serialize(pagePath, targetPage);
                    }
                }

            }

            else {
                //linear search on the pages of the table
                for (int i = 0; i < pagesId.size(); i++) {

                    int id = table.getPagesId().get(i);
                    String path = "src/main/resources/data/Tables/" + strTableName + "/Page" + id + ".ser";
                    Page targetPage = (Page) Deserialize(path);
                    Vector<Hashtable<String, Object>> currRecords = targetPage.getRecords();

                    Vector<Hashtable<String, Object>> updatedRecords = new Vector<>();
                    if (clusteringKeyExists)
                    {
                        System.out.println("entered");
                        updatedRecords = deleteRecordsByBinarySearch(currRecords, htblColNameValue, clusteringKey,htblColNameValue.get(clusteringKey), availableIndices);
                    }
                    else
                    {
                        updatedRecords =  deleteRecordsFromPage(targetPage.getRecords(), htblColNameValue, availableIndices);
                    }

                    //check if no records exist in the page after deletion
                    if (updatedRecords.size() == 0) {

                        File f = new File(path);
                        f.delete();
                        table.setNoPages(table.getNoPages() - 1);
                        Vector<Integer> pagesID = table.getPagesId();
                        pagesID.remove(new Integer(targetPage.getPageId()));
                        table.setPagesId(pagesID);
                        i--;

                    } else {

                        int size = updatedRecords.size();
                        targetPage.setRecords(updatedRecords);
                        targetPage.setMinClusteringKey(updatedRecords.get(0).get(clusteringKey));
                        targetPage.setMaxClusteringKey(updatedRecords.get(size - 1).get(clusteringKey));
                        Serialize(path, targetPage);

                    }
                }
            }
        }

        Serialize(tablePath,table);
    }

    public Iterator selectFromTable(SQLTerm[] arrSQLTerms, String[] strarrOperators) throws DBAppException
    {
        if (arrSQLTerms.length - strarrOperators.length != 1) throw new DBAppException("Invalid arrSQLTerms and strarrOperators");

        //to check on the given operators AND,OR,XOR
        validateStrarrOperators(strarrOperators);
        validateArrSQLTerms(arrSQLTerms);

        //insert logic of select
        return null;
    }

    public void validateArrSQLTerms(SQLTerm[] arrSQLTerms) throws DBAppException {
        String tableName = arrSQLTerms[0]._strTableName;

        for (int i = 1; i < arrSQLTerms.length; i++)
        {
            if (!(arrSQLTerms[1]._strTableName).equals(tableName))
            {
                throw new DBAppException("Different table names are passed ");
            }
        }

        Hashtable<String, Object> record = new Hashtable<>();
        for (int i = 0; i < arrSQLTerms.length; i++)
        {
            record.put(arrSQLTerms[i]._strColumnName, arrSQLTerms[i]._objValue);
        }

        Object[] tableInfo = readFromCSV(tableName);

        Hashtable<String,String> dataTypes = (Hashtable) tableInfo[2];
        Hashtable<String,Object> minValues= (Hashtable) tableInfo[3];
        Hashtable<String,Object> maxValues = (Hashtable) tableInfo[4];

        checkColDataTypes(dataTypes, record);
        checkColCompatibility(dataTypes, record);
        checkRange(dataTypes, minValues, maxValues, record);

    }

    public void validateStrarrOperators(String[] strarrOperators) throws DBAppException {
        for (String str : strarrOperators)
        {
            if (!str.equals("OR") && !str.equals("AND") && !str.equals("XOR"))
            {
                throw new DBAppException("strarrOperators contains invalid operators");
            }
        }
    }

    public Vector<Hashtable<String, Object>> deleteRecordsFromPage (Vector<Hashtable<String,Object>> records, Hashtable<String,Object> htblColNameValue, Vector<String> availableIndices) throws DBAppException {

        for (int i = 0; i < records.size(); i++)
        {
            Hashtable<String,Object> targetRecord = records.get(i);
            boolean delete = true;
            for (String key : htblColNameValue.keySet())
            {
                if ( ((htblColNameValue.get(key) instanceof DBAppNull) && !(targetRecord.get(key) instanceof DBAppNull))
                        || (!(htblColNameValue.get(key) instanceof DBAppNull) && (targetRecord.get(key) instanceof DBAppNull))
                        || compare(htblColNameValue.get(key),targetRecord.get(key)) != 0)
                {
                    delete = false;
                    break;
                }

            }
            if (delete){

                records.remove(i);
                i--;
                //to delete record from the existing indices
                deleteFromIndex(availableIndices, targetRecord, null, false);
            }

        }

        return records;
    }

    public Vector<Hashtable<String, Object>> deleteRecordsByBinarySearch(Vector<Hashtable<String,Object>> records, Hashtable<String, Object> htblColNameValue, String clusteringkey, Object clusteringObject, Vector<String> availableIndices) throws DBAppException {

        int lo = 0;
        int hi = records.size() - 1;

        while (lo <= hi) {

            int mid = (lo + hi) / 2;
            Hashtable<String, Object> targetRecord = records.get(mid);

            if (compare(clusteringObject,targetRecord.get(clusteringkey)) == 0)
            {
                boolean delete = true;
                for (String key : htblColNameValue.keySet())
                {
                    if ( ((htblColNameValue.get(key) instanceof DBAppNull) && !(targetRecord.get(key) instanceof DBAppNull))
                            || (!(htblColNameValue.get(key) instanceof DBAppNull) && (targetRecord.get(key) instanceof DBAppNull))
                            || compare(htblColNameValue.get(key),targetRecord.get(key)) != 0)
                    {
                        delete = false;
                        break;
                    }

                }

                if (delete){

                    records.remove(mid);
                    //to delete record from the existing indices
                    deleteFromIndex(availableIndices, targetRecord, clusteringObject, true);
                }

                return records;
            }
            else if (compare(clusteringObject,records.get(mid).get(clusteringkey)) < 0)
            {
                hi = mid - 1;
            }
            else
            {
                lo = mid + 1;
            }
        }

        return records;
    }

    public Vector<String> getAvailableIndices (String strTableName,Table table)
    {
        Vector<String> res = new Vector<>();
        Vector<Integer> indicesId = table.getIndicesId();

        for (Integer id : indicesId)
        {
            String path = "src/main/resources/data/Tables/"+strTableName+"/Indices/index"+id+".ser";
            res.add(path);
        }

        return res;

    }

    public int searchForPageToInsert (Vector<Integer> pagesID,String strTableName,Object clusteringKey) throws DBAppException {

        for (int i = 0; i < pagesID.size(); i++)
        {
            int index = pagesID.get(i);
            String path = "src/main/resources/data/Tables/"+strTableName+"/Page"+index+".ser";
            Page p = (Page) Deserialize(path);
            Serialize(path,p);

            if (compare(clusteringKey,p.getMinClusteringKey()) >= 0 && compare(p.getMaxClusteringKey(),clusteringKey) >= 0) return index;
            else if (compare(p.getMinClusteringKey(),clusteringKey) >= 0)
            {
                if (i == 0) return index;
                else
                {
                    String targetPath = "src/main/resources/data/Tables/"+strTableName+"/Page"+pagesID.get(i - 1)+".ser";
                    Page targetPage = (Page) Deserialize(targetPath);
                    Serialize(targetPath, targetPage);
                    if (isFull(targetPage.getRecords())) return index;
                    else return pagesID.get(i-1);
                }
            }
        }

        return pagesID.lastElement();
    }

    public int searchForRecord(Vector<Hashtable<String, Object>> records,Object clusteringKey,String primaryKey) throws DBAppException {

        int lo = 0;
        int hi = records.size() - 1;

        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            if (compare(clusteringKey,records.get(mid).get(primaryKey)) == 0) {
                throw new DBAppException("Primary key already exits");
            } else if (compare(clusteringKey,records.get(mid).get(primaryKey)) < 0) {
                hi = mid - 1;
            } else {
                lo = mid + 1;
            }
        }
        return hi + 1 ;
    }

    public int searchForRecordToUpdate(Vector<Hashtable<String, Object>> records, String clusteringKey,Object clusteringObject) {

        int lo = 0;
        int hi = records.size() - 1;

        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            if (compare(clusteringObject, records.get(mid).get(clusteringKey)) == 0) {
                return mid;
            } else if (compare(clusteringObject, records.get(mid).get(clusteringKey)) < 0) {
                hi = mid - 1;
            } else {
                lo = mid + 1;
            }
        }
        return -1;
    }

    public int binarySearchOnPages (String strTableName,Vector<Integer> pagesId, Object clusteringObject) throws DBAppException {

        int lo = 0;
        int hi = pagesId.size() - 1;
        while (lo <= hi)
        {
            int mid = (lo + hi) / 2;
            int pageId = pagesId.get(mid);
            String path = "src/main/resources/data/Tables/"+strTableName+"/Page"+pageId+".ser";
            Page p = (Page) Deserialize(path);
            Serialize(path, p);

            if (compare(clusteringObject, p.getMinClusteringKey()) >= 0 && compare(clusteringObject, p.getMaxClusteringKey()) <= 0)
            {
                return mid;
            }
            else if (compare(clusteringObject, p.getMinClusteringKey()) <= 0)
            {
                hi = mid - 1;
            }
            else
            {
                lo = mid + 1;
            }
        }
        return -1;
    }

    public boolean isFull(Vector<Hashtable<String, Object>> records)
    {
        return records.size() >= MaximumRowsCountinTablePage;
    }

    public int compare (Object o1,Object o2)
    {
        if (o1 instanceof java.lang.Integer && o2 instanceof java.lang.Integer) return ((Integer)o1).compareTo((Integer)o2);
        else if (o1 instanceof java.lang.Double && o2 instanceof java.lang.Double) return ((Double)o1).compareTo((Double)o2);
        else if (o1 instanceof java.lang.String && o2 instanceof java.lang.String) return ((String)o1).compareTo((String)o2);
        else if (o1 instanceof DBAppNull && o2 instanceof DBAppNull) return ((DBAppNull)o1).compareTo((DBAppNull)o2);
        else return ((Date)o1).compareTo((Date)o2);

    }

    //to know maximum no of records per page
    public void readConfig()
    {
        Properties prop = new Properties();
        String filename = "src/main/resources/DBApp.config";
        try (FileInputStream fis = new FileInputStream(filename))
        {
            prop.load(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MaximumRowsCountinTablePage = Integer.parseInt(prop.getProperty("MaximumRowsCountinTablePage"));
        MaximumEntriesinOctreeNode = Integer.parseInt(prop.getProperty("MaximumEntriesinOctreeNode"));
    }

    public Object[] readFromCSV (String strTableName) throws DBAppException {
        String clusteringKey = "", clusteringType = "";
        Hashtable<String,String> dataTypes = new Hashtable<>();
        Hashtable<String,Object> minValues = new Hashtable<>();
        Hashtable<String,Object> maxValues = new Hashtable<>();
        boolean flag = false;
        try {
            BufferedReader br = new BufferedReader(new FileReader("src/main/resources/metadata.csv"));
            String line = "";
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                if (data[0].equals(strTableName)) {

                    dataTypes.put(data[1], data[2]);
                    flag = true;

                    if (data[3].equals("true")) {
                        clusteringKey = data[1];
                        clusteringType = data[2];
                    }

                    switch (data[2]) {
                        case "java.lang.Integer":
                            minValues.put(data[1], Integer.parseInt(data[6]));
                            maxValues.put(data[1], Integer.parseInt(data[7]));
                            break;
                        case "java.lang.Double":
                            minValues.put(data[1], Double.parseDouble(data[6]));
                            maxValues.put(data[1], Double.parseDouble(data[7]));
                            break;
                        case "java.util.Date":
                            try {
                                minValues.put(data[1], new SimpleDateFormat("yyyy-MM-dd").parse(data[6]));
                                maxValues.put(data[1], new SimpleDateFormat("yyyy-MM-dd").parse(data[7]));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            minValues.put(data[1], data[6]);
                            maxValues.put(data[1], data[7]);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!flag) throw new DBAppException("Table does not exist");

        return new Object[]{clusteringKey,clusteringType,dataTypes,minValues,maxValues};
    }

    public void checkColDataTypes (Hashtable<String,String> dataTypes,Hashtable<String,Object> htblColNameValue) throws DBAppException {

        for (String key : htblColNameValue.keySet())
        {
            if (htblColNameValue.get(key) instanceof DBAppNull) continue;

            if (!dataTypes.containsKey(key)) throw new DBAppException("Column does not exist");
        }
    }

    public void checkColCompatibility(Hashtable<String,String> dataTypes,Hashtable<String,Object> htblColNameValue) throws DBAppException {

        for (String key : htblColNameValue.keySet())
        {
            if (htblColNameValue.get(key) instanceof DBAppNull) continue;
            Class col;
            try {
                col = Class.forName(dataTypes.get(key));
            } catch (ClassNotFoundException e) {
                throw new DBAppException("checkColCompatibility method");
            }

            if (!col.isInstance(htblColNameValue.get(key))) throw new DBAppException("Column data type is incompatible");
        }
    }

    public void checkRange(Hashtable<String,String> dataTypes,Hashtable<String,Object> minValues, Hashtable<String,Object> maxValues, Hashtable<String,Object> htblColNameValue) throws DBAppException {

        for (String key : htblColNameValue.keySet())
        {
            if (htblColNameValue.get(key) instanceof DBAppNull) continue;

            switch (dataTypes.get(key))
            {
                case "java.lang.Integer":
                    if (((Integer) htblColNameValue.get(key)).compareTo((Integer) minValues.get(key)) < 0)
                    {
                        throw new DBAppException("The value "+ key + " is below the minimum "+ minValues.get(key));
                    }
                    if (((Integer) htblColNameValue.get(key)).compareTo((Integer) maxValues.get(key)) > 0)
                    {
                        throw new DBAppException("The value "+ key + " is above the maximum "+ maxValues.get(key));
                    }
                    break;
                case "java.lang.Double":
                    if (((Double) htblColNameValue.get(key)).compareTo((Double) minValues.get(key)) < 0)
                    {
                        throw new DBAppException("The value "+ key + " is below the minimum "+ minValues.get(key));
                    }
                    if (((Double) htblColNameValue.get(key)).compareTo((Double) maxValues.get(key)) > 0)
                    {
                        throw new DBAppException("The value "+ key + " is above the maximum "+ maxValues.get(key));
                    }
                    break;
                case "java.util.Date":
                    if (((Date) htblColNameValue.get(key)).compareTo((Date) minValues.get(key)) < 0)
                    {
                        throw new DBAppException("The value "+ key + " is below the minimum "+ minValues.get(key));
                    }
                    if (((Date) htblColNameValue.get(key)).compareTo((Date) maxValues.get(key)) > 0)
                    {
                        throw new DBAppException("The value "+ key + " is above the maximum "+ maxValues.get(key));
                    }
                    break;
                default:
                    if (((String) htblColNameValue.get(key)).compareTo((String) minValues.get(key)) < 0)
                    {
                        throw new DBAppException("The value "+ key + " is below the minimum "+ minValues.get(key));
                    }
                    if (((String) htblColNameValue.get(key)).compareTo((String) maxValues.get(key)) > 0)
                    {
                        throw new DBAppException("The value "+ key + " is above the maximum "+ maxValues.get(key));
                    }
                    break;
            }

        }
    }

    public boolean containsNull (Hashtable<String,Object> htblColNameValue)
    {
        for (Object obj : htblColNameValue.values())
        {
            if (obj instanceof DBAppNull) return true;
        }

        return false;
    }

    public void Serialize(String path , Object obj) throws DBAppException {
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(obj);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            throw new DBAppException("Serialize method exception");
        }
    }

    public Object Deserialize(String path) throws DBAppException {
        Object o;
        try {
            FileInputStream fileIn = new FileInputStream(path);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            o = objectIn.readObject();
            objectIn.close();
            fileIn.close();
        } catch (Exception e) {
            throw new DBAppException("Deserialize method exception");
        }
        return o;
    }

    public void printPages(String tableName) throws DBAppException {
        String path = "src/main/resources/data/Tables/"+tableName+"/"+tableName+".ser";
        Table t = (Table) Deserialize(path);
        Serialize(path,t);
        System.out.println("Page Content ");
        for (Integer index : t.getPagesId())
        {
            String pagePath = "src/main/resources/data/Tables/"+tableName+"/Page"+index+".ser";
            Page p = (Page) Deserialize(pagePath);
            Serialize(pagePath,p);
            System.out.println(p.getRecords().toString());
            System.out.println(p.getMaxClusteringKey());
            System.out.println(p.getMinClusteringKey());
        }
        System.out.println("Table contents");
        System.out.println(t.getNoPages()+" "+t.getPagesId().toString() + " Indices: " + t.getIndicesId().toString());
        System.out.println("Index Content");
        for (Integer index : t.getIndicesId())
        {
            System.out.println("------------------------------------Index"+index+"------------------------------------");
            String indexPath = "src/main/resources/data/Tables/"+tableName+"/Indices/index"+index+".ser";
            OctTree tree = (OctTree) Deserialize(indexPath);
            Serialize(indexPath,tree);
            tree.printTree();
        }
    }

}
