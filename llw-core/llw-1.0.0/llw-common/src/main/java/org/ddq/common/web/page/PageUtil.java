package org.ddq.common.web.page;


public class PageUtil {
	/**降序*/
	public static final String ORDER_DESC="DESC";
	/**升序*/
	public static final String ORDER_ASC="ASC";
	/**
	 * Use the origin page to create a new page
	 * 
	 * @param page page
	 * @param totalRecords totalRecords
	 */
	public static void createPage(Page page, int totalRecords) {
        int totalPage = getTotalPage(page.getEveryPage(), totalRecords);
        int startPage = page.getCurrentPage();
		if (startPage == -1) {
        	startPage = totalPage;
        	page.setCurrentPage(totalPage);
        }
        page.setPage(startPage, totalPage, totalRecords);
	}

    /**
     * 
     * @param everyPage everyPage
     * @param totalRecords totalRecords
     * @return int
     */
	private static int getTotalPage(int everyPage, int totalRecords) {
		int totalPage = 0;

		if (totalRecords % everyPage == 0) {
            totalPage = totalRecords / everyPage;
        } else {
            totalPage = totalRecords / everyPage + 1;
        }
		return totalPage;
	}
}
