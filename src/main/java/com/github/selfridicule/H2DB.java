package com.github.selfridicule;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class H2DB {
    // 数据库连接URL，当前连接的是项目H2目录下的db数据库(h2数据存储有两种模式,一种是存储在硬盘上,一种是存储在内存中)
    //jdbc:h2:mem:数据库名称
    private static final String JDBC_URL = "jdbc:h2:file:" + new File(System.getProperty("basedir", System.getProperty("user.dir")), "H2/db").getAbsolutePath();
    // 连接数据库时使用的用户名
    private static final String USER = "root";
    // 连接数据库时使用的密码
    private static final String PASSWORD = "";
    // 连接H2数据库时使用的驱动类，org.h2.Driver这个类是由H2数据库自己提供的，在H2数据库的jar包中可以找到
    private static final String DRIVER_CLASS = "org.h2.Driver";
    // 全局数据库连接
    private static Connection conn;
    // 数据库操作接口
    private static Statement stmt;

    static {
        try {
            connection();
            statement();
//            createTable();
//            insertData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressFBWarnings("DMI_EMPTY_DB_PASSWORD")
    public static void connection() throws Exception {
        // 加载驱动
        Class.forName(DRIVER_CLASS);
        // 根据连接URL，用户名，密码，获取数据库连接
        conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    public static void statement() throws Exception {
        // 创建操作
        stmt = conn.createStatement();
    }

    public static void createTable() throws Exception {
        // 先删
        stmt.execute("DROP TABLE IF EXISTS link_to_be_process");
        stmt.execute("DROP TABLE IF EXISTS link_already_process");
        stmt.execute("DROP TABLE IF EXISTS news");
        // 再建
        stmt.execute("CREATE TABLE link_to_be_process(link VARCHAR(255))");
        stmt.execute("CREATE TABLE link_already_process(link VARCHAR(255))");
        stmt.execute("CREATE TABLE news(id int(11) PRIMARY KEY auto_increment,title text , content text , url VARCHAR(100) , create_time timestamp , update_time timestamp )");
    }

    // 插入数据
    public static void insertData() throws Exception {
        insertLinkToBeProcess("https://sina.cn/index/feed?from=touch&Ver=10");
    }

    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public static List<Map<String, String>> queryData(String tableName, List<String> columnList) throws Exception {
        StringBuffer columnValue = new StringBuffer("");
        for (String column : columnList) {
            columnValue.append(column + " ,");
        }
        if (columnValue.length() > 0) {
            columnValue = new StringBuffer(columnValue.substring(0, columnValue.length() - 1));
        }
        List<Map<String, String>> dataList = new ArrayList<>();
        try (PreparedStatement preparedStatement = conn.prepareStatement("SELECT " + columnValue.toString() + " FROM " + tableName)) {
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Map<String, String> map = new HashMap<>();
                for (String column : columnList) {
                    map.put(column, rs.getString(column));
                }
                dataList.add(map);
            }
        }
        return dataList;
    }

    public static String queryLinkToBeProcessFirst() {
        List<String> list = queryLinkToBeProcess();
        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public static List<String> queryLinkToBeProcess() {
        try {
            List<Map<String, String>> list = queryData("link_to_be_process", Arrays.asList("link"));
            return list.stream().map(map -> map.get("link")).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean insertLinkToBeProcess(String link) {
        try (PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO link_to_be_process (link) VALUES(?)")) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public static boolean deleteLinkToBeProcess(String link) {
        try (PreparedStatement preparedStatement = conn.prepareStatement("delete from link_to_be_process where link = ?")) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public static List<String> queryLinkAlreadyProcess() {
        try {
            List<Map<String, String>> list = queryData("link_already_process", Arrays.asList("link"));
            return list.stream().map(map -> map.get("link")).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean existLinkAlreadyProcess(String link) {
        try (PreparedStatement preparedStatement = conn.prepareStatement("select * from link_already_process where link = ?")) {
            preparedStatement.setString(1, link);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public static boolean insertLinkAlreadyProcess(String link) {
        try (PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO link_already_process (link) VALUES(?)")) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    // 释放资源和关闭连接
    public static void close() {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            List<String> list = H2DB.queryLinkToBeProcess();
            System.out.println(list);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("场面一度十分尴尬");
        } finally {
            try {
                H2DB.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("关都关不上了");
            }
        }
    }

}
