package com.zzr.business.controller;

import com.zzr.business.entity.ArticleEntity;
import com.zzr.business.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by zhaozhirong on 2019/10/15.
 */
@Controller
@RequestMapping(value = "/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @ResponseBody
    @RequestMapping(value = "getById", method = {RequestMethod.GET,RequestMethod.POST})
    public ArticleEntity getById(String id){
        return articleService.getById(id);
    }


    @ResponseBody
    @RequestMapping(value = "add", method = {RequestMethod.POST, RequestMethod.GET})
    public String add(ArticleEntity articleEntity){
        try {
            articleService.add(articleEntity);
            return "success";
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "fail";
    }


    @ResponseBody
    @RequestMapping(value = "deleteArticleById", method = {RequestMethod.POST, RequestMethod.GET})
    public String deleteArticleById(String id){
        try {
            articleService.deleteArticleById(id);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "fail";
    }

    @ResponseBody
    @RequestMapping(value = "deleteAllArticle", method = {RequestMethod.POST, RequestMethod.GET})
    public String deleteAllArticle(){
        try {
            articleService.deleteAllArticle();
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "fail";
    }

    @ResponseBody
    @RequestMapping(value = "updateArticleById", method = {RequestMethod.POST, RequestMethod.GET})
    public ArticleEntity updateArticleById(ArticleEntity articleEntity){
        try {
            return articleService.updateArticleById(articleEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @ResponseBody
    @RequestMapping(value = "findAll", method = {RequestMethod.POST, RequestMethod.GET})
    public List<ArticleEntity> findAll(){
        try {
            return articleService.findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
