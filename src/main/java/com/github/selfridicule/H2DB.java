package com.github.selfridicule;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

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
    @SuppressFBWarnings("MS_PKGPROTECT")
    public static Connection conn;
    // 数据库操作接口
    @SuppressFBWarnings("MS_PKGPROTECT")
    public static Statement stmt;

    static {
        try {
            connection();
            statement();
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
            List<String> list = new H2SqlDao().queryLinkToBeProcess();
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
