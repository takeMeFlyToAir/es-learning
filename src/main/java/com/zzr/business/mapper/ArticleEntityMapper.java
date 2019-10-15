// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.zzr.business.mapper;

import com.zzr.es6.mapping.BaseElasticSearchMapping;
import org.elasticsearch.index.mapper.*;
import org.springframework.stereotype.Service;

@Service
public class ArticleEntityMapper extends BaseElasticSearchMapping {

    @Override
    protected void configureRootObjectBuilder(RootObjectMapper.Builder builder) {
        builder
                .add(new NumberFieldMapper.Builder("price", NumberFieldMapper.NumberType.FLOAT))
                .add(new TextFieldMapper.Builder("name"))
                .add(new TextFieldMapper.Builder("author"))
                .add(new TextFieldMapper.Builder("id"))
                .add(new TextFieldMapper.Builder("content"))
                        .nested(ObjectMapper.Nested.newNested(true, false));
    }
}