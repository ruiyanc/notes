package com.hxlc.api.uilts;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Httpclient请求工具类
 *
 * @author hellofly
 * @date 2019/4/9
 */
@Slf4j
public class HttpClientUtil {
    /**
     * 连接主机超时（30s）
     */
    public static final int HTTP_CONNECT_TIMEOUT_30S = 30 * 1000;
    /**
     * 从主机读取数据超时（3min）
     */
    public static final int HTTP_READ_TIMEOUT_3MIN = 180 * 1000;
    /**
     * HTTP成功状态码（200）
     */
    public static final int HTTP_SUCCESS_STATUS_CODE = 200;

    private static final PoolingHttpClientConnectionManager cm;

    static {
        cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        cm.setDefaultMaxPerRoute(30);
        cm.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setCharset(StandardCharsets.UTF_8).build());
        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(30000)
                .setSoReuseAddress(true).build();
        cm.setDefaultSocketConfig(socketConfig);
    }

//    public static void init() {
//        cm = new PoolingHttpClientConnectionManager();
//        cm.setMaxTotal(100);
//        cm.setDefaultMaxPerRoute(30);
//        cm.setDefaultConnectionConfig(ConnectionConfig.custom()
//                .setCharset(StandardCharsets.UTF_8).build());
//        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(30000)
//                .setSoReuseAddress(true).build();
//        cm.setDefaultSocketConfig(socketConfig);
//    }

    public static CloseableHttpClient getHttpClient() {
        int timeout = 2;
        RequestConfig config = RequestConfig.custom()
                //设置连接超时时间，单位毫秒
                .setConnectTimeout(timeout * 1000)
                //设置从connect Manager获取Connection 超时时间，单位毫秒
                .setConnectionRequestTimeout(timeout * 1000)
                //请求获取数据的超时时间，单位毫秒
                .setSocketTimeout(timeout * 1000).build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm).setDefaultRequestConfig(config)
                .build();
        //打印连接池的状态
        if (cm != null && cm.getTotalStats() != null) {
            log.info("now client pool {}", cm.getTotalStats().toString());
        }
        return httpClient;
    }

    /**
     * get请求
     *
     * @param url
     * @param formDataParam
     * @return
     */
    public static String getMap(String url, Map<String, Object> formDataParam) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String result = "";
        // 超时时间设置
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(HTTP_READ_TIMEOUT_3MIN)
                .setConnectTimeout(HTTP_CONNECT_TIMEOUT_30S).build();
        try {
            URIBuilder builder = new URIBuilder(url);
            if (null != formDataParam && formDataParam.size() > 0) {
                // 创建参数队列
                List<NameValuePair> formParams = new ArrayList<>();
                for (Entry<String, Object> entry : formDataParam.entrySet()) {
                    formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
                }
                builder.setParameters(formParams);
            }
            // 设置参数
            HttpGet httpGet = new HttpGet(builder.build());
            httpGet.setConfig(requestConfig);
            // 发送请求
            response = httpclient.execute(httpGet);
            result = EntityUtils.toString(response.getEntity(), Charsets.UTF_8);
            if (response.getStatusLine().getStatusCode() != HTTP_SUCCESS_STATUS_CODE) {
                log.info("Request URL is [{}], params [{}]. Result:[{}]", url, formDataParam, result);
            }
        } catch (Exception e) {
            log.error("Error in getMap", e);
        } finally {
            HttpClientUtils.closeQuietly(httpclient);
            HttpClientUtils.closeQuietly(response);
        }
        return result;
    }

    /**
     * 连接池优化的get请求
     *
     * @param url
     * @return
     */
    public static String getMap(String url) {
//        init();
        CloseableHttpClient httpClient = getHttpClient();
        CloseableHttpResponse response;
        String result = "";
        // 超时时间设置
        RequestConfig requestConfig = RequestConfig.custom()
                //数据传输过程中数据包之间间隔的最大时间
                .setSocketTimeout(HTTP_READ_TIMEOUT_3MIN)
                //连接建立时间，三次握手完成时间
                .setConnectTimeout(HTTP_CONNECT_TIMEOUT_30S)
                .setExpectContinueEnabled(true)
                .setConnectionRequestTimeout(10000)
                .build();
        try {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(requestConfig);
            // 发送请求
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, Charsets.UTF_8);
            if (response.getStatusLine().getStatusCode() != HTTP_SUCCESS_STATUS_CODE) {
                log.info("Request URL is [{}], Result:[{}]", url, result);
            }
            EntityUtils.consume(entity);
        } catch (Exception e) {
            log.error("Error in getMap", e);
        }
        return result;
    }

    /**
     * postFormData
     *
     * @param url
     * @param formDataParam
     * @return
     */
    public static String postFormData(String url, Map<String, Object> formDataParam) {
//        init();
        CloseableHttpClient httpclient = getHttpClient();
        CloseableHttpResponse response;
        String result = "";
        // 超时时间设置
        RequestConfig requestConfig = RequestConfig.custom()
                //数据传输过程中数据包之间间隔的最大时间
                .setSocketTimeout(HTTP_READ_TIMEOUT_3MIN)
                //连接建立时间，三次握手完成时间
                .setConnectTimeout(HTTP_CONNECT_TIMEOUT_30S)
                .setExpectContinueEnabled(true)
                .setConnectionRequestTimeout(10000)
                .build();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        try {
            if (null != formDataParam && formDataParam.size() > 0) {
                // 创建参数队列
                List<NameValuePair> formParams = new ArrayList<>();
                for (Entry<String, Object> entry : formDataParam.entrySet()) {
                    formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
                }
                // 设置参数
                UrlEncodedFormEntity urlEntity = new UrlEncodedFormEntity(formParams, Charsets.UTF_8);
                httpPost.setEntity(urlEntity);
            }
            // 发送请求
            response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, Charsets.UTF_8);
            if (response.getStatusLine().getStatusCode() != HTTP_SUCCESS_STATUS_CODE) {
                log.info("Request URL is [{}], params [{}]. Result:[{}]", url, formDataParam, result);
            }
            EntityUtils.consume(entity);
        } catch (Exception e) {
            log.error("Error in postFormData", e);
        }
        return result;
    }

    /**
     * postJson
     *
     * @param url
     * @param jsonParam
     * @return
     */
    public static String postJson(String url, String jsonParam) {
//        init();
        CloseableHttpClient httpClient = getHttpClient();
        CloseableHttpResponse response;
        String result = "";
        // 超时时间设置
        RequestConfig requestConfig = RequestConfig.custom()
                //数据传输过程中数据包之间间隔的最大时间
                .setSocketTimeout(HTTP_READ_TIMEOUT_3MIN)
                //连接建立时间，三次握手完成时间
                .setConnectTimeout(HTTP_CONNECT_TIMEOUT_30S)
                .setExpectContinueEnabled(true)
                .setConnectionRequestTimeout(10000)
                .build();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        // 设置请求头和请求参数
        if (StringUtils.isNotEmpty(jsonParam)) {
            StringEntity entity = new StringEntity(jsonParam, "utf-8");
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
        }
        try {
            // 发送请求
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, Charsets.UTF_8);
            if (response.getStatusLine().getStatusCode() != HTTP_SUCCESS_STATUS_CODE) {
                log.info("Request URL is [{}], params [{}]. Result:[{}]", url, jsonParam, result);
            }
            EntityUtils.consume(entity);
        } catch (Exception e) {
            log.error("Error in postJson", e);
        }
        return result;
    }

    /**
     * deleteJson
     * @param url
     */
    public static void deleteJson(String url) {
//        init();
        CloseableHttpClient httpclient = getHttpClient();
        CloseableHttpResponse response;
        String result;
        // 超时时间设置
        RequestConfig requestConfig = RequestConfig.custom()
                //数据传输过程中数据包之间间隔的最大时间
                .setSocketTimeout(HTTP_READ_TIMEOUT_3MIN)
                //连接建立时间，三次握手完成时间
                .setConnectTimeout(HTTP_CONNECT_TIMEOUT_30S)
                .setExpectContinueEnabled(true)
                .setConnectionRequestTimeout(10000)
                .build();
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setConfig(requestConfig);
        try {
            // 发送请求
            response = httpclient.execute(httpDelete);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, Charsets.UTF_8);
            if (response.getStatusLine().getStatusCode() != HTTP_SUCCESS_STATUS_CODE) {
                log.info("Request URL is [{}], Result:[{}]", url, result);
            }
            EntityUtils.consume(entity);
        } catch (Exception e) {
            log.error("Error in postJson", e);
        }
    }

    /**
     * putJson
     *
     * @param url
     * @param jsonParam
     * @return
     */
    public static String putJson(String url, String jsonParam) {
//        init();
        CloseableHttpClient httpclient = getHttpClient();
        CloseableHttpResponse response;
        String result = "";
        // 超时时间设置
        RequestConfig requestConfig = RequestConfig.custom()
                //数据传输过程中数据包之间间隔的最大时间
                .setSocketTimeout(HTTP_READ_TIMEOUT_3MIN)
                //连接建立时间，三次握手完成时间
                .setConnectTimeout(HTTP_CONNECT_TIMEOUT_30S)
                .setExpectContinueEnabled(true)
                .setConnectionRequestTimeout(10000)
                .build();
        HttpPut httpPut = new HttpPut(url);
        httpPut.setConfig(requestConfig);
        // 设置请求头和请求参数
        if (StringUtils.isNotEmpty(jsonParam)) {
            StringEntity entity = new StringEntity(jsonParam, "utf-8");
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            httpPut.setEntity(entity);
        }
        try {
            // 发送请求
            response = httpclient.execute(httpPut);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, Charsets.UTF_8);
            if (response.getStatusLine().getStatusCode() != HTTP_SUCCESS_STATUS_CODE) {
                log.info("Request URL is [{}], params [{}]. Result:[{}]", url, jsonParam, result);
            }
            EntityUtils.consume(entity);
        } catch (Exception e) {
            log.error("Error in postJson", e);
        }
        return result;
    }

}