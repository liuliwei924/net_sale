package org.llw.com.web.page;


@SuppressWarnings("serial")
public class Page implements java.io.Serializable{

    /** the number of every page */
    private int everyPage = 10;

    /** the total page number */
    private int totalPage;

    /** the number of current page */
    private int currentPage;

    /** the begin index of the records by the current query */
    private int beginIndex;
    
    /** the page list record count*/
    private int recordCount;

    /**totalRecords**/
    private long totalRecords;

	/** The default constructor */
    public Page() {
        setCurrentPage(1);
    }
    
    /** The default constructor */
    public Page(int everyPage) {
        setCurrentPage(1);
        this.everyPage = everyPage;
    }
    /**
     * 
     * @param pCurrentPage pCurrentPage
     * @param pTotalPage everyPage
     */
    public Page(int pCurrentPage, int everyPage) {
       setCurrentPage(pCurrentPage);
       this.everyPage = everyPage;
    }
    /**
     * 
     * @param pCurrentPage pCurrentPage
     * @param pTotalPage pTotalPage
     * @param pTotalRecords pTotalRecords
     */
    public void setPage(int pCurrentPage, int pTotalPage, int pTotalRecords) {
        if (pTotalPage == 0) {
            this.currentPage = 0;
        } else if (pCurrentPage <= 0) {
            this.currentPage = 1;
        } else if (pCurrentPage > pTotalPage && pTotalPage != 0) {
            this.currentPage = pTotalPage;
        } else {
            this.currentPage = pCurrentPage;
        }
        this.totalPage = pTotalPage;
        this.totalRecords = pTotalRecords;
        setCurrentPage(this.currentPage);
    }

    /**
     * @return Returns the beginIndex.
     */
    public int getBeginIndex() {
        return beginIndex;
    }

    /**
     * @param beginIndex The beginIndex to set.
     */
    public void setBeginIndex(int beginIndex) {
        this.beginIndex = beginIndex;
    }

    /**
     * @return Returns the currentPage.
     */
    public int getCurrentPage() {
		if (currentPage == 0) {
			return 1;
		}
        return currentPage;
    }

    /**
     * @param currentPage The currentPage to set.
     */
    public void setCurrentPage(int currentPage) {
        if (currentPage == 0) {
            this.beginIndex = 0;
        } else {
            this.beginIndex = (currentPage - 1) * everyPage;
        }
        this.currentPage = currentPage;
       
    }

    /**
     * @return Returns the everyPage.
     */
    public int getEveryPage() {
		return everyPage;
    }

    /**
     * @param everyPage The everyPage to set.
     */
    public void setEveryPage(int everyPage) {
        this.everyPage = everyPage;
    }
    /**
     * @return Returns the totalPage.
     * 
     */
    public int getTotalPage() {
    	if(totalPage<=0){
    		return 1;
    	}
        return totalPage;
    }

    /**
     * @param totalPage The totalPage to set.
     */
    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }
    
    /**
     * @return totalRecords
     */
    public long getTotalRecords() {
        return this.totalRecords;
    }
    
    /**
     * @param pTotalRecords pTotalRecords
     */
    public void setTotalRecords(long pTotalRecords) {
        this.totalRecords = pTotalRecords;
    }

	public int getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}
    
    
}
