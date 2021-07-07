package com.github.selfridicule;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MybatisSqlDao implements SqlDao {

    private SqlSessionFactory sqlSessionFactory;

    public MybatisSqlDao() {
        String resource = "db/mybatis/config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    @Override
    public String queryNextLinkThenDelete() {
        String link = queryLinkToBeProcessFirst();
        if (link != null) {
            deleteLinkToBeProcess(link);
        }
        return link;
    }

    @Override
    public String queryLinkToBeProcessFirst() {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            return session.selectOne("com.github.selfridicule.MyMapper.queryLinkToBeProcessFirst");
        }
    }

    @Override
    public List<String> queryLinkToBeProcess() {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            return session.selectList("com.github.selfridicule.MyMapper.queryLinkToBeProcess");
        }
    }

    @Override
    public boolean insertLinkToBeProcess(String link) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.selfridicule.MyMapper.insertLinkToBeProcess", link);
            return true;
        }
    }

    @Override
    public boolean deleteLinkToBeProcess(String link) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.delete("com.github.selfridicule.MyMapper.deleteLinkToBeProcess", link);
            return true;
        }
    }

    @Override
    public List<String> queryLinkAlreadyProcess() {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            return session.selectList("com.github.selfridicule.MyMapper.queryLinkAlreadyProcess");
        }
    }

    @Override
    public boolean existLinkAlreadyProcess(String link) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            Integer count = session.selectOne("com.github.selfridicule.MyMapper.existLinkAlreadyProcess");
            if (count > 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean insertLinkAlreadyProcess(String link) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.selfridicule.MyMapper.insertLinkAlreadyProcess", link);
            return true;
        }
    }

    @Override
    public boolean insertNews(String title, String content, String url) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            News news = new News();
            news.setTitle(title);
            news.setContent(content);
            news.setUrl(url);
            session.insert("com.github.selfridicule.MyMapper.insertNews", news);
            return true;
        }
    }
}
