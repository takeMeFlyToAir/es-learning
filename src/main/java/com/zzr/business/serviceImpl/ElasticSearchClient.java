// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.zzr.business.serviceImpl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.zzr.business.config.ESConstant;
import com.zzr.business.config.ESTransportClientDecorator;
import com.zzr.core.client.IElasticSearchClient;
import com.zzr.core.exceptions.CreateIndexFailedException;
import com.zzr.core.exceptions.IndicesExistsFailedException;
import com.zzr.core.exceptions.PutMappingFailedException;
import com.zzr.core.utils.JsonUtilities;
import com.zzr.es6.client.bulk.configuration.BulkProcessorConfiguration;
import com.zzr.es6.client.bulk.options.BulkProcessingOptions;
import com.zzr.es6.mapping.IElasticSearchMapping;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
public class ElasticSearchClient<TEntity> implements IElasticSearchClient<TEntity>,InitializingBean, DisposableBean {

    private static final Logger log = Loggers.getLogger(IElasticSearchClient.class, IElasticSearchClient.class.getName());

    private String host = "140.143.238.46";

    private Integer port = 9300;

    private TransportClient transportClient;

    private BulkProcessor bulkProcessor;

    public ElasticSearchClient() {
        init();
    }

    private void init(){
        try {
            transportClient = new PreBuiltTransportClient(Settings.EMPTY);
            transportClient.addTransportAddress(new TransportAddress(InetAddress.getByName(host), port));
            BulkProcessorConfiguration bulkProcessorConfiguration = new BulkProcessorConfiguration(BulkProcessingOptions.builder()
                    .setBulkActions(100)
                    .build());
            this.bulkProcessor = bulkProcessorConfiguration.build(transportClient);
        }catch (Exception e){

        }
    }

    @Override
    public void index(String indexName, String id,TEntity entity){
         transportClient.prepareIndex(indexName, ESConstant.INDEX_TYPE, id).setSource(JSONObject.toJSONString(entity), XContentType.JSON).get();
    }

    @Override
    public void index(String indexName, IElasticSearchMapping mapping,TEntity entity) throws InterruptedException {
        index(indexName,mapping,Arrays.asList(entity));
    }

    @Override
    public void index(String indexName, IElasticSearchMapping mapping,List<TEntity> entities) throws InterruptedException {
        if(!indexExist(indexName)){
            createIndex(indexName);
            putMapping(indexName,mapping);
        }
        index(indexName,mapping,entities.stream());
    }

    @Override
    public void index(String indexName, IElasticSearchMapping mapping,Stream<TEntity> entities) throws InterruptedException {
        entities
                .map(x -> JsonUtilities.convertJsonToBytes(x))
                .filter(x -> x.isPresent())
                .map(x -> createIndexRequest(indexName, mapping,x.get()))
                .forEach(bulkProcessor::add);
        flush();
    }

    private IndexRequest createIndexRequest(String indexName, IElasticSearchMapping mapping,byte[] messageBytes) {
        return transportClient.prepareIndex()
                .setIndex(indexName)
                .setType(mapping.getIndexType())
                .setSource(messageBytes, XContentType.JSON)
                .request();
    }

    @Override
    public void deleteIndex(String indexName) {
        transportClient.admin().indices().prepareDelete(indexName).get();
    }

    @Override
    public void deleteDocument(String indexName, String id) {
        transportClient.prepareDelete(indexName, ESConstant.INDEX_TYPE,id).get();
    }

    @Override
    public void updateDocumentById(String indexName,String id, TEntity tEntity) {
        transportClient.prepareUpdate(indexName,ESConstant.INDEX_TYPE,id)
                .setDoc(JSONObject.toJSONString(tEntity), XContentType.JSON)
                .get();
    }


    @Override
    public void flush() {
        bulkProcessor.flush();
    }

    @Override
    public synchronized boolean awaitClose(long timeout, TimeUnit unit) throws InterruptedException {
        return bulkProcessor.awaitClose(timeout, unit);
    }

    @Override
    public void close() throws Exception {
        bulkProcessor.close();
    }



    @Override
    public TEntity getDocumentById(String indexName,String id,Class clazz) {
        GetResponse response  = transportClient.prepareGet(indexName, ESConstant.INDEX_TYPE, id).get();
        if(!response.isExists()){
            return null;
        }
        String sourceAsString = response.getSourceAsString();
        TEntity entity = JSONObject.parseObject(sourceAsString, (Class<TEntity>) clazz);
        return entity;
    }


    @Override
    public List<TEntity> queryAll(String indexName,Class clazz) {
        List<TEntity> tEntityList = new ArrayList<TEntity>();
        SearchRequestBuilder searchRequestBuilder = transportClient.prepareSearch(indexName).setTypes(ESConstant.INDEX_TYPE);
        SearchResponse searchResponse = searchRequestBuilder.setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
        SearchHits hits = searchResponse.getHits();
        hits.forEach(hit -> {
            tEntityList.add(JSONObject.parseObject(hit.getSourceAsString(),(Class<TEntity>) clazz));
        });
        return tEntityList;
    }

    @Override
    public  Boolean indexExist(String indexName) {
        try {
            return transportClient.admin().indices()
                    .prepareExists(indexName)
                    .execute().actionGet().isExists();
        } catch(Exception e) {
            if(log.isErrorEnabled()) {
                log.error("Error Checking Index Exist", e);
            }
            throw new IndicesExistsFailedException(indexName, e);
        }
    }

    private   CreateIndexResponse createIndex(String indexName) {
        try {
            return internalCreateIndex(indexName);
        } catch(Exception e) {
            if(log.isErrorEnabled()) {
                log.error("Error Creating Index", e);
            }
            throw new CreateIndexFailedException(indexName, e);
        }
    }

    private  AcknowledgedResponse putMapping( String indexName, IElasticSearchMapping mapping) {
        try {
            return internalPutMapping(indexName, mapping);
        } catch(Exception e) {
            if(log.isErrorEnabled()) {
                log.error("Error Creating Index", e);
            }
            throw new PutMappingFailedException(indexName, e);
        }
    }

    private  CreateIndexResponse internalCreateIndex(String indexName) throws IOException {
        final CreateIndexRequestBuilder createIndexRequestBuilder = transportClient
                .admin() // Get the Admin interface...
                .indices() // Get the Indices interface...
                .prepareCreate(indexName); // We want to create a new index ....

        final CreateIndexResponse indexResponse = createIndexRequestBuilder.execute().actionGet();

        if(log.isDebugEnabled()) {
            log.debug("CreatedIndexResponse: isAcknowledged {}", indexResponse.isAcknowledged());
        }

        return indexResponse;
    }

    private  AcknowledgedResponse internalPutMapping(String indexName, IElasticSearchMapping mapping) throws IOException {

        String json = Strings.toString(mapping.getMapping());

        final PutMappingRequest putMappingRequest = new PutMappingRequest(indexName)
                .type(mapping.getIndexType())
                .source(json, XContentType.JSON);

        final AcknowledgedResponse putMappingResponse = transportClient
                .admin()
                .indices()
                .putMapping(putMappingRequest)
                .actionGet();

        if(log.isDebugEnabled()) {
            log.debug("PutMappingResponse: isAcknowledged {}", putMappingResponse.isAcknowledged());
        }

        return putMappingResponse;
    }

    @Override
    public void destroy() throws Exception {
        transportClient.close();
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
