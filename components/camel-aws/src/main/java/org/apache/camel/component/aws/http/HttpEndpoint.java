/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.aws.s3;

import java.io.IOException;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.Request;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.ScheduledPollEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;
import org.apache.camel.support.SynchronizationAdapter;
import org.apache.camel.util.IOHelper;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  * The aws-s3 component is used for storing and retrieving objecct from Amazon
 *   * S3 Storage Service.
 *    */
@UriEndpoint(firstVersion = "2.8.0", scheme = "aws-s3", title = "AWS S3 Storage Service", syntax = "aws-s3:bucketNameOrArn", consumerClass = S3Consumer.class, label = "cloud,file")
public class HttpEndpoint extends ScheduledPollEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(HttpEndpoint.class);

    private AmazonHttpClient httpClient;

    @UriParam
    private HttpConfiguration configuration;
    @UriParam(label = "consumer", defaultValue = "10")
    private int maxMessagesPerPoll = 10;
    @UriParam(label = "consumer", defaultValue = "60")
    private int maxConnections = 50 + maxMessagesPerPoll;

    @Deprecated
    public HttpEndpoint(String uri, CamelContext context, HttpConfiguration configuration) {
        super(uri, context);
        this.configuration = configuration;
    }

    public HttpEndpoint(String uri, Component comp, HttpConfiguration configuration) {
        super(uri, comp);
        this.configuration = configuration;
    }

    /*public Consumer createConsumer(Processor processor) throws Exception {
        S3Consumer s3Consumer = new S3Consumer(this, processor);
        configureConsumer(s3Consumer);
        s3Consumer.setMaxMessagesPerPoll(maxMessagesPerPoll);
        return s3Consumer;
    }*/

    public Producer createProducer() throws Exception {
        return new HttpProducer(this);
    }

    public boolean isSingleton() {
        return true;
    }

    @Override
    public void doStart() throws Exception {
        super.doStart();

        httpClient = new AmazonHttpClient(configuration);
    }

    public Exchange createExchange() {
        return createExchange(getExchangePattern());
    }

    public Exchange createExchange(ExchangePattern pattern) {

        Exchange exchange = super.createExchange(pattern);
        Message message = exchange.getIn();



        /**
 *          * If includeBody != true, it is safe to close the object here. If
 *                   * includeBody == true, the caller is responsible for closing the stream
 *                            * and object once the body has been fully consumed. As of 2.17, the
 *                                     * consumer does not close the stream or object on commit.
 *                                              */
        if (!configuration.isIncludeBody()) {
            IOHelper.close(s3Object);
        } else {
            if (configuration.isAutocloseBody()) {
                exchange.addOnCompletion(new SynchronizationAdapter() {
                    @Override
                    public void onDone(Exchange exchange) {
                        IOHelper.close(s3Object);
                    }
                });
            }
        }

        return exchange;
    }

    public HttpConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(HttpConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setHttpClient(AmazonHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public AmazonHttpClient getHttpClient() {
        return httpClient;
    }

    /**
 *      * Provide the possibility to override this method for an mock
 *           * implementation
 *                */
    AmazonS3 createS3Client() {

        AmazonS3 client = null;
        AmazonS3ClientBuilder clientBuilder = null;
        AmazonS3EncryptionClientBuilder encClientBuilder = null;
        ClientConfiguration clientConfiguration = null;
        boolean isClientConfigFound = false;
        if (configuration.hasProxyConfiguration()) {
            clientConfiguration = new ClientConfiguration();
            clientConfiguration.setProxyHost(configuration.getProxyHost());
            clientConfiguration.setProxyPort(configuration.getProxyPort());
            clientConfiguration.setMaxConnections(getMaxConnections());
            isClientConfigFound = true;
        } else {
            clientConfiguration = new ClientConfiguration();
            clientConfiguration.setMaxConnections(getMaxConnections());
            isClientConfigFound = true;
        }
        if (configuration.getAccessKey() != null && configuration.getSecretKey() != null) {
            AWSCredentials credentials = new BasicAWSCredentials(configuration.getAccessKey(), configuration.getSecretKey());
            AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
            if (isClientConfigFound && !configuration.isUseEncryption()) {
                clientBuilder = AmazonS3ClientBuilder.standard().withClientConfiguration(clientConfiguration).withCredentials(credentialsProvider);
            } else if (isClientConfigFound && configuration.isUseEncryption()) {
                StaticEncryptionMaterialsProvider encryptionMaterialsProvider = new StaticEncryptionMaterialsProvider(configuration.getEncryptionMaterials());
                encClientBuilder = AmazonS3EncryptionClientBuilder.standard().withClientConfiguration(clientConfiguration).withCredentials(credentialsProvider)
                    .withEncryptionMaterials(encryptionMaterialsProvider);
            } else {
                clientBuilder = AmazonS3ClientBuilder.standard().withCredentials(credentialsProvider);
            }
            if (!configuration.isUseEncryption()) {
                if (ObjectHelper.isNotEmpty(configuration.getRegion())) {
                    clientBuilder = clientBuilder.withRegion(Regions.valueOf(configuration.getRegion()));
                }
                clientBuilder = clientBuilder.withPathStyleAccessEnabled(configuration.isPathStyleAccess());
                client = clientBuilder.build();
            } else {
                if (ObjectHelper.isNotEmpty(configuration.getRegion())) {
                    encClientBuilder = encClientBuilder.withRegion(Regions.valueOf(configuration.getRegion()));
                }
                encClientBuilder = encClientBuilder.withPathStyleAccessEnabled(configuration.isPathStyleAccess());
                client = encClientBuilder.build();
            }
        } else {
            if (isClientConfigFound && !configuration.isUseEncryption()) {
                clientBuilder = AmazonS3ClientBuilder.standard();
            } else if (isClientConfigFound && configuration.isUseEncryption()) {
                StaticEncryptionMaterialsProvider encryptionMaterialsProvider = new StaticEncryptionMaterialsProvider(configuration.getEncryptionMaterials());
                encClientBuilder = AmazonS3EncryptionClientBuilder.standard().withClientConfiguration(clientConfiguration).withEncryptionMaterials(encryptionMaterialsProvider);
            } else {
                clientBuilder = AmazonS3ClientBuilder.standard().withClientConfiguration(clientConfiguration);
            }
            if (!configuration.isUseEncryption()) {
                if (ObjectHelper.isNotEmpty(configuration.getRegion())) {
                    clientBuilder = clientBuilder.withRegion(Regions.valueOf(configuration.getRegion()));
                }
                clientBuilder = clientBuilder.withPathStyleAccessEnabled(configuration.isPathStyleAccess());
                client = clientBuilder.build();
            } else {
                if (ObjectHelper.isNotEmpty(configuration.getRegion())) {
                    encClientBuilder = encClientBuilder.withRegion(Regions.valueOf(configuration.getRegion()));
                }
                encClientBuilder = encClientBuilder.withPathStyleAccessEnabled(configuration.isPathStyleAccess());
                client = encClientBuilder.build();
            }
        }

        return client;
    }

    public int getMaxMessagesPerPoll() {
        return maxMessagesPerPoll;
    }

    /**
 *      * Gets the maximum number of messages as a limit to poll at each polling.
 *           * <p/>
 *                * Is default unlimited, but use 0 or negative number to disable it as
 *                     * unlimited.
 *                          */
    public void setMaxMessagesPerPoll(int maxMessagesPerPoll) {
        this.maxMessagesPerPoll = maxMessagesPerPoll;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    /**
 *      * Set the maxConnections parameter in the S3 client configuration
 *           */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
}
