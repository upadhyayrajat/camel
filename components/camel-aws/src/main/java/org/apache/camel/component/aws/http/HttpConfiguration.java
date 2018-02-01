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
package org.apache.camel.component.aws.http;

import com.amazonaws.http.AmazonHttpClient;

import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriParams;
import org.apache.camel.util.ObjectHelper;

@UriParams
public class HttpConfiguration implements Cloneable {

    @UriParam
    private AmazonHttpClient amazonHttpClient;
    @UriParam
    private String accessKey;
    @UriParam
    private String secretKey;
    @UriParam(label = "producer")
    private String region;
    @UriParam
    private String amazonHttpEndpoint;
    @UriParam
    private String proxyHost;
    @UriParam
    private Integer proxyPort;
    @UriParam(label = "consumer", defaultValue = "true")
    private boolean includeBody = true;
    @UriParam
    private boolean pathStyleAccess;
    @UriParam(label = "consumer,advanced", defaultValue = "true")
    private boolean autocloseBody = true;
    @UriParam(label = "common")
    private EncryptionMaterials encryptionMaterials;
    @UriParam(label = "common", defaultValue = "false")
    private boolean useEncryption;


    /**
 *      * The region with which the AWS-HTTP client wants to work with.
 *           */
    public void setAmazonHttpEndpoint(String amazonHttpEndpoint) {
        this.amazonHttpEndpoint = amazonHttpEndpoint;
    }

    public String getAmazonHttpEndpoint() {
        return amazonHttpEndpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    /**
 *      * Amazon AWS Access Key
 *           */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    /**
 *      * Amazon AWS Secret Key
 *           */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public AmazonHttpClient getAmazonHttpClient() {
        return amazonHttpClient;
    }

    /**
 *      * Reference to a `com.amazonaws.services.sqs.AmazonS3` in the
 *           * link:registry.html[Registry].
 *                */
    public void setAmazonHttpClient(AmazonHttpClient amazonHttpClient) {
        this.amazonHttpClient = amazonHttpClient;
    }

    public String getPrefix() {
        return prefix;
    }


    public String getRegion() {
        return region;
    }

    /**
 *      * The region where the bucket is located. This option is used in the
 *           * `com.amazonaws.services.s3.model.CreateBucketRequest`.
 *                */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
 *      * If it is true, the exchange body will be set to a stream to the contents
 *           * of the file. If false, the headers will be set with the S3 object
 *                * metadata, but the body will be null. This option is strongly related to
 *                     * autocloseBody option. In case of setting includeBody to true and
 *                          * autocloseBody to false, it will be up to the caller to close the S3Object
 *                               * stream. Setting autocloseBody to true, will close the S3Object stream
 *                                    * automatically.
 *                                         */
    public void setIncludeBody(boolean includeBody) {
        this.includeBody = includeBody;
    }

    public boolean isIncludeBody() {
        return includeBody;
    }

    public boolean isDeleteAfterRead() {
        return deleteAfterRead;
    }

    /**
 *      * Delete objects from S3 after they have been retrieved. The delete is only
 *           * performed if the Exchange is committed. If a rollback occurs, the object
 *                * is not deleted.
 *                     * <p/>
 *                          * If this option is false, then the same objects will be retrieve over and
 *                               * over again on the polls. Therefore you need to use the Idempotent
 *                                    * Consumer EIP in the route to filter out duplicates. You can filter using
 *                                         * the {@link S3Constants#BUCKET_NAME} and {@link S3Constants#KEY} headers,
 *                                              * or only the {@link S3Constants#KEY} header.
 *                                                   */
    public void setDeleteAfterRead(boolean deleteAfterRead) {
        this.deleteAfterRead = deleteAfterRead;
    }

    public boolean isDeleteAfterWrite() {
        return deleteAfterWrite;
    }

    /**
 *      * Delete file object after the S3 file has been uploaded
 *           */
    public void setDeleteAfterWrite(boolean deleteAfterWrite) {
        this.deleteAfterWrite = deleteAfterWrite;
    }

    public String getPolicy() {
        return policy;
    }

    /**
 *      * The policy for this queue to set in the
 *           * `com.amazonaws.services.s3.AmazonS3#setBucketPolicy()` method.
 *                */
    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getStorageClass() {
        return storageClass;
    }

    /**
 *      * The storage class to set in the
 *           * `com.amazonaws.services.s3.model.PutObjectRequest` request.
 *                */
    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    public String getServerSideEncryption() {
        return serverSideEncryption;
    }

    /**
 *      * Sets the server-side encryption algorithm when encrypting
 *           * the object using AWS-managed keys. For example use <tt>AES256</tt>.
 *                */
    public void setServerSideEncryption(String serverSideEncryption) {
        this.serverSideEncryption = serverSideEncryption;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    /**
 *      * To define a proxy host when instantiating the SQS client
 *           */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    /**
 *      * Specify a proxy port to be used inside the client definition.
 *           */
    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }


    public boolean isAutocloseBody() {
        return autocloseBody;
    }

    /**
 *      * If this option is true and includeBody is true, then the S3Object.close()
 *           * method will be called on exchange completion. This option is strongly
 *                * related to includeBody option. In case of setting includeBody to true and
 *                     * autocloseBody to false, it will be up to the caller to close the S3Object
 *                          * stream. Setting autocloseBody to true, will close the S3Object stream
 *                               * automatically.
 *                                    */
    public void setAutocloseBody(boolean autocloseBody) {
        this.autocloseBody = autocloseBody;
    }

    public EncryptionMaterials getEncryptionMaterials() {
        return encryptionMaterials;
    }

    /**
 *      * The encryption materials to use in case of Symmetric/Asymmetric client
 *           * usage
 *                */
    public void setEncryptionMaterials(EncryptionMaterials encryptionMaterials) {
        this.encryptionMaterials = encryptionMaterials;
    }

    public boolean isUseEncryption() {
        return useEncryption;
    }

    /**
 *      * Define if encryption must be used or not
 *           */
    public void setUseEncryption(boolean useEncryption) {
        this.useEncryption = useEncryption;
    }

    boolean hasProxyConfiguration() {
        return ObjectHelper.isNotEmpty(getProxyHost()) && ObjectHelper.isNotEmpty(getProxyPort());
    }
}

