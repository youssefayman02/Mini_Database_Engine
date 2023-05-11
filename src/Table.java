import java.io.*;
import java.util.*;

public class Table implements Serializable {
    private String tableName;
    transient private Vector<Page> Pages;
    private Vector<Integer> pagesId;// keep track of pagesID in my table
    private int noPages;
    private int numOfIndices;

    public Table(String tableName, Vector<Page> pages, Vector<Integer> pagesId, int noPages, int numOfIndices) {
        this.tableName = tableName;
        this.Pages = pages;
        this.pagesId = pagesId;
        this.noPages = noPages;
        this.numOfIndices = numOfIndices;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Vector<Page> getPages() {
        return Pages;
    }

    public void setPages(Vector<Page> pages) {
        this.Pages = pages;
    }

    public Vector<Integer> getPagesId() {
        return this.pagesId;
    }

    public void setPagesId(Vector<Integer> pagesId) {
        this.pagesId = pagesId;
    }

    public int getNoPages() {
        return noPages;
    }

    public void setNoPages(int noPages) {
        this.noPages = noPages;
    }

    public int getNumOfIndices() {
        return numOfIndices;
    }

    public void setNumOfIndices(int numOfIndices) {
        this.numOfIndices = numOfIndices;
    }
}

