// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.zzr.es6.client.bulk.configuration;

import com.zzr.es6.client.bulk.listener.LoggingBulkProcessorListener;
import com.zzr.es6.client.bulk.options.BulkProcessingOptions;
import com.zzr.es6.client.bulk.options.BulkProcessingOptionsBuilder;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.client.Client;

public class BulkProcessorConfiguration {

    private BulkProcessingOptions options = new BulkProcessingOptionsBuilder().build();
    private BulkProcessor.Listener listener = new LoggingBulkProcessorListener();

    public BulkProcessorConfiguration(BulkProcessingOptions options)
    {
        this(options, new LoggingBulkProcessorListener());
    }

    public BulkProcessorConfiguration(BulkProcessingOptions options, BulkProcessor.Listener listener) {
        this.options = options;
        this.listener = listener;
    }

    public BulkProcessingOptions getBulkProcessingOptions() {
        return options;
    }

    public BulkProcessor.Listener getBulkProcessorListener() {
        return listener;
    }

    public BulkProcessor build(final Client client) {
        return BulkProcessor.builder(client, listener)
                .setConcurrentRequests(options.getConcurrentRequests())
                .setBulkActions(options.getBulkActions())
                .setBulkSize(options.getBulkSize())
                .setFlushInterval(options.getFlushInterval())
                .setBackoffPolicy(options.getBackoffPolicy())
                .build();
    }
}