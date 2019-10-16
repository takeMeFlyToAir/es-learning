// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.zzr.core.client;

import com.zzr.es6.mapping.IElasticSearchMapping;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 *
 * @param <TEntity>
 */
public interface IElasticSearchClient<TEntity> extends AutoCloseable {

    void index(String indexName, String id,TEntity entity);

    void index(String indexName, IElasticSearchMapping mapping, TEntity entity) throws InterruptedException;

    void index(String indexName, IElasticSearchMapping mapping,List<TEntity> entities) throws InterruptedException;

    void index(String indexName, IElasticSearchMapping mapping,Stream<TEntity> entities) throws InterruptedException;

    Boolean indexExist(String indexName);

    void deleteIndex(String indexName);

    void deleteDocument(String indexName, String id);

    void updateDocumentById(String indexName,String id,TEntity entity);

    void flush();

    boolean awaitClose(long timeout, TimeUnit unit) throws InterruptedException;

    TEntity getDocumentById(String indexName,String id,Class clazz);


    List<TEntity> queryAll(String indexName,Class clazz);

}
