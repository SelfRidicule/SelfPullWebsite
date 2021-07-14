package com.github.selfridicule;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticSearchMain {

    public static void main(String[] args) {
//        esAdd();
        search();
    }

    public static void esAdd() {
        SqlDao sqlDao = new MybatisSqlDao();
        List<News> list = sqlDao.queryNews();
        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            BulkRequest bulkRequest = new BulkRequest();
            for (int i = 0; i < list.size(); i++) {
                News news = list.get(i);
                Map<String, String> map = new HashMap<>();
                map.put("title", news.getTitle());
                map.put("content", news.getContent());
                map.put("url", news.getUrl());
                IndexRequest request = new IndexRequest("news");
                request.source(map, XContentType.JSON);
                bulkRequest.add(request);
                //
//                IndexResponse response = client.index(request, RequestOptions.DEFAULT);
//                System.out.println(response.status().getStatus());
            }
            BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            System.out.println(response.status().getStatus());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void search() {
        while (true) {
            System.out.println("Please input search keyword");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String keyword = null;
            try {
                keyword = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(keyword);
            try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
                SearchRequest request = new SearchRequest("news");
                request.source(new SearchSourceBuilder().query(new MultiMatchQueryBuilder(keyword, "title", "content")));
                SearchResponse result = client.search(request, RequestOptions.DEFAULT);
                result.getHits().forEach(hit -> System.out.println(hit.getSourceAsString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
