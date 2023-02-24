package com.nowcoder.community.entity;

/**
 * @Projectname: community
 * @Filename: Page
 * @Author: yunqi
 * @Date: 2023/2/23 18:09
 * @Description: 封装分页相关信息
 */
public class Page {

    //浏览器向服务器传入的数据
    private int current = 1;  // 当前页码
    private int limit = 10;   // 显示帖子数量上限

    //服务器查询后，向浏览器返回的数据
    private int rows;         // 数据总数（用于计算总页数）
    private String path;      // 查询路径（用于复用分页链接）

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1) {
            this.current = current;
        } else if (current > getTotal()) {
            current = 1;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页的起始行
     *
     * @return
     */
    public int getOffset() {
        // current * limit -limit;
        return (current - 1) * limit;
    }

    /**
     * 获取总页数
     *
     * @return
     */
    public int getTotal() {
        // rows/limit
        if (rows % limit == 0) {
            return rows / limit;
        }
        return rows / limit + 1;
    }

    /**
     * 获取起始页码
     *
     * @return
     */
    public int getFrom() {
        int from = current - 2;
        return Math.max(from, 1);
    }

    /**
     * 获取结束页码
     *
     * @return
     */

    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        return Math.min(to, total);
    }


}
