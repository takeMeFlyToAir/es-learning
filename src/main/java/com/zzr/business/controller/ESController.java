package com.zzr.business.controller;

import com.zzr.business.entity.ArticleEntity;
import com.zzr.business.service.ArticleService;
import com.zzr.business.serviceImpl.ElasticSearchClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by zhaozhirong on 2019/10/15.
 */
@Controller
@RequestMapping(value = "/es")
public class ESController {

    @Autowired
    private ElasticSearchClient elasticSearchClient;


    @ResponseBody
    @RequestMapping(value = "add", method = {RequestMethod.POST, RequestMethod.GET})
    public String add(String indexName, String id, String name,String author){
        try {
            ESEntity esEntity = new ESEntity();
            esEntity.setName(name);
            esEntity.setAuthor(author);
            elasticSearchClient.index(indexName, id, esEntity);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "fail";
    }


    @Data
    class ESEntity{
        private String name;

        private String author;

    }
}
