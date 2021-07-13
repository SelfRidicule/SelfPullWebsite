package com.github.selfridicule;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Main extends Thread {

    SqlDao dao;

    public Main(SqlDao dao) {
        this.dao = dao;
    }

    public static void main(String[] args) {
        SqlDao sqlDao = new MybatisSqlDao();
        for (int i = 0; i < 4; i++) {
            new Main(sqlDao).start();
        }
    }

    @Override
    public void run() {
        accessNetwork();
    }

    public void accessNetwork() {
        try {
            //连接
            String link;
            //连接池有连接
            while ((link = dao.queryNextLinkThenDelete()) != null) {
                //处理过相同的连接
                if (dao.existLinkAlreadyProcess(link)) {
                    continue;
                }
                //从数据库添加已处理的连接
                dao.insertLinkAlreadyProcess(link);
                //连接是否要处理
                if (isLinkNotHandle(link)) {
                    continue;
                } else { //处理
                    //发送HttpGet请求，解析内容返回html对象
                    Document document = httpGetAndParseHtml(link);
                    //连接池添加a标签连接
                    parseDocLink(document);
                    //如果是新闻详情页面，就存入数据库，否则就什么都不做
                    storeIntoDatabaseIsPage(document, link);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            H2DB.close();
        }
    }

    private void parseDocLink(Document document) {
        //连接池添加a标签连接
        for (Element aTag : document.select("a")) {
            String href = aTag.attr("href");
            if (href == null || "".equals(href.trim()) || href.toLowerCase().contains("javascript")) {
                continue;
            }
            dao.insertLinkToBeProcess(href);
        }
    }

    private void storeIntoDatabaseIsPage(Document document, String url) {
        Elements articleTagList = document.select("article");
        if (articleTagList != null && articleTagList.size() > 0) {
            Element articleTag = articleTagList.get(0);
            String title = articleTag.child(0).text();
            ArrayList<Element> pList = articleTag.select("p");
            String content = pList.stream().map(Element::text).collect(Collectors.joining("\n"));
            //数据库添加新闻
            dao.insertNews(title, content, url);
        }
    }

    private Document httpGetAndParseHtml(String link) {
        // create http
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36");
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            System.out.println(link);
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            //is to string
            String content = IOUtils.toString(is, "UTF-8");
            //parse html
            return Jsoup.parse(content);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("html解析失败");
        }
    }

    private static boolean isLinkNotHandle(String link) {
        if (!isYesPage(link) || isLoginPage(link)) { //不处理
            return true;
        } else {
            return false;
        }
    }

    private static boolean isYesPage(String link) {
        return link.contains("sina.cn");
    }

    private static boolean isLoginPage(String link) {
        return link.contains("passport.sina.cn")
                || link.contains("games.sina.cn")
                || link.contains("game.proc.sina.cn")
                || link.contains("my.sina.cn")
                || link.contains("share.sina.cn/callback");
    }

}
