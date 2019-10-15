package com.zzr.business.serviceImpl;

import com.zzr.business.entity.ArticleEntity;
import com.zzr.business.mapper.ArticleEntityMapper;
import com.zzr.business.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by zhaozhirong on 2019/10/15.
 */

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ElasticSearchClient<ArticleEntity> elasticSearchClient;

    @Autowired
    private ArticleEntityMapper articleEntityMapper;

    @Override
    public void add(ArticleEntity articleEntity) throws InterruptedException {
        //TODO 保存数据库
        //保存ES
        elasticSearchClient.index(INDEX_NAME,articleEntityMapper,articleEntity);
    }

    @Override
    public void deleteAllArticle() {
        elasticSearchClient.deleteIndex(INDEX_NAME);
    }

    @Override
    public void deleteArticleById(String id) {
        elasticSearchClient.deleteDocument(INDEX_NAME, id);
    }

    @Override
    public ArticleEntity getById(String id) {
        return elasticSearchClient.getDocumentById(INDEX_NAME,id,ArticleEntity.class);
    }

    @Override
    public ArticleEntity updateArticleById(ArticleEntity articleEntity) {
        elasticSearchClient.updateDocumentById(INDEX_NAME,articleEntity.getId(),articleEntity);
        return getById(articleEntity.getId());
    }
}
