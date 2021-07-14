package com.github.selfridicule;

import java.util.List;

public interface SqlDao {

    String queryNextLinkThenDelete();

    String queryLinkToBeProcessFirst();

    List<String> queryLinkToBeProcess();

    boolean insertLinkToBeProcess(String link);

    boolean deleteLinkToBeProcess(String link);

    List<String> queryLinkAlreadyProcess();

    boolean existLinkAlreadyProcess(String link);

    boolean insertLinkAlreadyProcess(String link);

    boolean insertNews(String title, String content, String url);

    List<News> queryNews();
}
