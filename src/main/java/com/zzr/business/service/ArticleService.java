package com.zzr.business.service;

import com.zzr.business.entity.ArticleEntity;

/**
 * Created by zhaozhirong on 2019/10/15.
 */

public interface ArticleService {

    String INDEX_NAME = "article";

    void add(ArticleEntity articleEntity) throws InterruptedException;

    void deleteAllArticle();

    void deleteArticleById(String id);

    ArticleEntity getById(String id);

    ArticleEntity updateArticleById(ArticleEntity articleEntity);
}
