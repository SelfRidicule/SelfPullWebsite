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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws IOException {
        accessNetwork();
    }

    public static void accessNetwork() {
        //待处理的连接池
        List<String> linkPool = new ArrayList<>();
        //已处理的连接池
        Set<String> processedLink = new HashSet<>();
        //添加开始连接
        linkPool.add("https://sina.cn/index/feed?from=touch&Ver=10");
        //连接池有连接
        while (!linkPool.isEmpty()) {
            //获取连接
            String link = linkPool.remove(linkPool.size() - 1);
            //处理过相同的连接
            if (processedLink.contains(link)) {
                continue;
            }
            //添加处理过的连接
            processedLink.add(link);
            //连接是否要处理
            if (isLinkNotHandle(link)) {
                continue;
            } else { //处理
                //发送HttpGet请求，解析内容返回html对象
                Document document = httpGetAndParseHtml(link);
                //连接池添加a标签连接
                document.select("a").stream().map(aTag-> aTag.attr("href")).forEach(linkPool::add);
                //如果是新闻详情页面，就存入数据库，否则就什么都不做
                storeIntoDatabaseIsPage(document);
            }
        }
    }

    private static void storeIntoDatabaseIsPage(Document document) {
        Elements articleTagList = document.select("article");
        if (articleTagList != null && articleTagList.size() > 0) {
            Element articleTag = articleTagList.get(0);
            String title = articleTag.child(0).text();
            System.out.println(title);
        }
    }

    private static Document httpGetAndParseHtml(String link) {
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
