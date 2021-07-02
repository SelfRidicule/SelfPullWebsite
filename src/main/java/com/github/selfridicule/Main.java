package com.github.selfridicule;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;

public class Main {

    public static void main(String[] args) {

    }

    public static void accessNetwork() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://www.baidu.com");
        CloseableHttpResponse response = httpClient.execute(httpGet);
        try {
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            //is to string
            String content = IOUtils.toString(is, "UTF-8");
            System.out.println(content);
        } catch (Exception e) {
            response.close();
        }
    }

}
