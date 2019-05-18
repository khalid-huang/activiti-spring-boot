package org.self.activitidemo.constant;

import java.util.List;

/**
 * 分页类
 */
public class Pagination<T> {

    public static int DEFAULT_PAGENUMBER = 1;
    public static int DEFAULT_PAGESIZE = 10;

    private List<T> rows = null; //当前返回的记录列表
    private int rowTotal = 0; //总记录数
    private int pageTotal = 0; //总页数


    protected int pageNumber = DEFAULT_PAGENUMBER;  //页码
    protected int pageSize = DEFAULT_PAGESIZE; //每页记录数

    public Pagination() {
        this(DEFAULT_PAGENUMBER, DEFAULT_PAGESIZE);
    }

    public Pagination(int pageNumber, int pageSize) {
        setPageNumber(pageNumber);//pageNumber从1开始不是从0开始
        setPageSize(pageSize);
    }

    public static Pagination getInstance(int pageNumber, int pageSize) {
        return new Pagination(pageNumber, pageSize);
    }

    public static Pagination getInstance2Top(int top) {
        return new Pagination(1, top);
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public int getRowTotal() {
        return rowTotal;
    }

    public void setRowTotal(int rowTotal) {
        this.rowTotal = rowTotal;
    }

    public int getPageTotal() {
        return pageTotal;
    }

    public void setPageTotal(int pageTotal) {
        this.pageTotal = pageTotal;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getStart(){
        return (getPageNumber()-1)*getPageSize();
    }
    public int getEnd(){
        return getStart() + getPageSize();
    }
}
