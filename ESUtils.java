package com.hxgis.tianjing.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateResponse;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.*;
import org.elasticsearch.search.aggregations.bucket.terms.*;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ScrolledPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author xurui
 */
@Slf4j
public class ElasticService {
    private Client client;
    private final EsIndexService esIndexService;
    private ElasticsearchTemplate esTemplate;
    private final ObjectMapper objectMapper;
    private boolean useFastJson = true;
    private BulkProcessor bulkProcessor;
    private Object defaultIndexSettings;
    private boolean autoCreateIndexTemplate = true;
    private boolean checkIndexBeforeQuery = false;
    private boolean autoCreateIndex = true;
    private static final int size = 1000;
    private static final int time = 60000;
    private static final long MAX_OBSERVED_TIME = 253402271999000L;
    private final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_RANG_FIELD = "observedTime";


    public ElasticService(Client client, EsIndexService esIndexService, ElasticsearchTemplate esTemplate, ObjectMapper objectMapper) {
        this.client = client;
        this.esIndexService = esIndexService;
        this.esTemplate = esTemplate;
        this.objectMapper = objectMapper;
        Map<String, Object> settings = new HashMap();
        settings.put("index.number_of_shards", "8");
        this.defaultIndexSettings = settings;
        this.bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long l, BulkRequest bulkRequest) {
                ElasticService.log.info("准备:插入数据{}条", bulkRequest.numberOfActions());
            }

            @Override
            public void afterBulk(long l, BulkRequest bulkRequest, BulkResponse bulkResponse) {
                if (!bulkResponse.hasFailures()) {
                    ElasticService.log.info("成功:插入数据{}条", bulkRequest.numberOfActions());
                } else {
                    ElasticService.log.error("插入数据总条数：{}，插入过程存在错误：{}", bulkRequest.numberOfActions(), bulkResponse.buildFailureMessage());
                }

            }

            @Override
            public void afterBulk(long l, BulkRequest bulkRequest, Throwable throwable) {
                ElasticService.log.info("失败:插入数据{}条，throwable：{}", bulkRequest.numberOfActions(), throwable.getMessage());
            }
        }).setBulkActions(3000).setBulkSize(new ByteSizeValue(1L, ByteSizeUnit.GB))
                .setFlushInterval(TimeValue.timeValueSeconds(5L)).setConcurrentRequests(10)
                .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100L), 3)).build();
    }

    @PostConstruct
    public void init() {
        if (this.autoCreateIndexTemplate) {
            this.autoCreateIndexTemplate(this.client, "hxlc");
        }

    }

    public void setClient(Client client) {
        this.client = client;
        this.esTemplate = new ElasticsearchTemplate(client);
    }

    public Client getClient() {
        return this.client;
    }

    private void autoCreateIndexTemplate(final Client client, final String name) {
        GetIndexTemplatesRequest request = new GetIndexTemplatesRequest(name);
        client.admin().indices().getTemplates(request, new ActionListener<GetIndexTemplatesResponse>() {
            @Override
            public void onResponse(GetIndexTemplatesResponse getIndexTemplatesResponse) {
                List<IndexTemplateMetaData> indexTemplates = getIndexTemplatesResponse.getIndexTemplates();
                if (indexTemplates.size() == 0) {
                    ElasticService.log.info("未检测到[{}]索引模板，正在自动创建...", name);
                    PutIndexTemplateRequest templateRequest = new PutIndexTemplateRequest(name);
                    templateRequest.template("hxlc_*");
                    templateRequest.order(9999);
                    Map<String, Object> settings = new HashMap();
                    settings.put("index.number_of_shards", "8");
                    settings.put("index.mapping.total_fields.limit", 2000);
                    templateRequest.settings(settings);
                    Map<String, Object> mappings = new HashMap();
                    mappings.put("numeric_detection", false);
                    mappings.put("date_detection", false);
                    String dynamicTemplatesStr = "{\"dynamic_templates\": [\n          {\n            \"strings\": {\n              \"mapping\": {\n                \"type\": \"keyword\"\n              },\n              \"match_mapping_type\": \"string\"\n            }\n          }\n        ]}";
                    JSONObject dynamicTemplatesObj = JSONObject.parseObject(dynamicTemplatesStr);
                    mappings.put("dynamic_templates", dynamicTemplatesObj.get("dynamic_templates"));
                    templateRequest.mapping("_default_", mappings);

                    try {
                        PutIndexTemplateResponse putIndexTemplateResponse = client.admin().indices().putTemplate(templateRequest).get();
                        if (putIndexTemplateResponse.isAcknowledged()) {
                            ElasticService.log.info("索引模板[{}]自动创建成功", name);
                        } else {
                            ElasticService.log.error("索引模板[{}]自动创建失败: {}", name, putIndexTemplateResponse.toString());
                        }
                    } catch (ExecutionException | InterruptedException var9) {
                        throw new RuntimeException("索引模板创建过程出错", var9);
                    }
                }

            }

            @Override
            public void onFailure(Exception e) {
                throw new RuntimeException("索引模板创建过程出错", e);
            }
        });
    }

    public void setUseFastJson(boolean useFastJson) {
        this.useFastJson = useFastJson;
    }

    public void setBulkProcessor(BulkProcessor bulkProcessor) {
        this.bulkProcessor = bulkProcessor;
    }

    public void setDefaultIndexSettings(Object defaultIndexSettings) {
        this.defaultIndexSettings = defaultIndexSettings;
    }

    public void setAutoCreateIndexTemplate(boolean autoCreateIndexTemplate) {
        this.autoCreateIndexTemplate = autoCreateIndexTemplate;
    }

    public void setAutoCreateIndex(boolean autoCreateIndex) {
        this.autoCreateIndex = autoCreateIndex;
    }

    public void insertToEs(List<? extends BaseEsDoc> sources) {
        this.insertToEs(null, sources, this.bulkProcessor);
    }

    public void insertToEs(List<? extends BaseEsDoc> sources, BulkProcessor bulkProcessor) {
        this.insertToEs(null, sources, bulkProcessor);
    }

    public void insertToEs(String fixedIndexName, List<? extends BaseEsDoc> sources, BulkProcessor bulkProcessor) {
        if (sources != null && sources.size() != 0) {
            if (bulkProcessor == null) {
                throw new IllegalArgumentException("BulkProcessor不能为null");
            } else {
                Class<? extends BaseEsDoc> clazz = sources.get(0).getClass();
                boolean indexFixed = fixedIndexName != null;
                if (!indexFixed) {
                    fixedIndexName = this.getLatestIndexName(clazz);
                }

                if (this.autoCreateIndex && !this.esIndexService.autoCreateIndex(clazz, this.defaultIndexSettings, fixedIndexName)) {
                    log.error("自动创建索引失败");
                    throw new RuntimeException("自动创建索引失败");
                } else {
                    String esType = this.getType(clazz);

                    try {
                        if (BaseEsTimeDoc.class.isAssignableFrom(clazz)) {
                            String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIME_FORMAT));
                            long now = System.currentTimeMillis();
                            Stream<BaseEsTimeDoc> baseEsTimeDocStream = sources.stream().map((source) -> {
                                BaseEsTimeDoc obj = (BaseEsTimeDoc) source;
                                if (obj.getObservedTime() == 0L) {
                                    obj.setObservedTime(now);
                                }

                                if (obj.getCreateTime() == null) {
                                    obj.setCreateTime(createTime);
                                }

                                return obj;
                            }).filter((source) -> {
                                if (source.getObservedTime() > 253402271999000L) {
                                    try {
                                        log.warn("数据观测时间非法，拒绝插入：{}", this.toJson(source));
                                    } catch (IOException var3) {
                                    }

                                    return false;
                                } else {
                                    return true;
                                }
                            });
                            if (indexFixed) {
                                this.insertToEsInner(fixedIndexName, esType, baseEsTimeDocStream.collect(Collectors.toList()), bulkProcessor);
                            } else {
                                Map<String, List<BaseEsTimeDoc>> indexGroupedSources = baseEsTimeDocStream
                                        .collect(Collectors.groupingBy((obj) -> IndexUtils.getIndexNameByTime(clazz, obj.getObservedTime())));
                                Iterator var12;
                                if (this.autoCreateIndex) {
                                    var12 = indexGroupedSources.keySet().iterator();

                                    while (var12.hasNext()) {
                                        String indexName = (String) var12.next();
                                        if (!this.esIndexService.autoCreateIndex(clazz, this.defaultIndexSettings, indexName)) {
                                            log.error("自动创建索引失败");
                                            throw new RuntimeException("自动创建索引失败");
                                        }
                                    }
                                }

                                var12 = indexGroupedSources.entrySet().iterator();

                                while (var12.hasNext()) {
                                    Map.Entry<String, List<BaseEsTimeDoc>> entry = (Map.Entry) var12.next();
                                    String indexName = entry.getKey();
                                    List<BaseEsTimeDoc> groupedSources = entry.getValue();
                                    this.insertToEsInner(indexName, esType, groupedSources, bulkProcessor);
                                }
                            }
                        } else {
                            this.insertToEsInner(fixedIndexName, esType, sources, bulkProcessor);
                        }

                    } catch (IOException var16) {
                        throw new RuntimeException("序列化json出错");
                    }
                }
            }
        } else {
            log.info("数据源为空，跳过！");
        }
    }

    private void insertToEsInner(String index, String type, List<? extends BaseEsDoc> sources, BulkProcessor bulkProcessor) throws IOException {
        log.info("准备插入数据, index：{}，esType：{}，共：{}条数据", index, type, sources.size());
        Iterator<? extends BaseEsDoc> var5 = sources.iterator();

        while (true) {
            while (var5.hasNext()) {
                BaseEsDoc source = var5.next();
                String jsonSrc = this.toJson(source);
                String docId = source.getDocId();
                if (docId != null && docId.length() != 0) {
                    bulkProcessor.add(this.client.prepareIndex(index, type, docId).setSource(jsonSrc, XContentType.JSON).request());
                } else {
                    bulkProcessor.add(this.client.prepareIndex(index, type).setSource(jsonSrc, XContentType.JSON).request());
                }
            }

            return;
        }
    }

    private String toJson(Object obj) throws IOException {
        return this.useFastJson ? JSON.toJSONString(obj) : this.objectMapper.writeValueAsString(obj);
    }

    private <T> T toObject(String json, Class<T> clazz) throws IOException {
        return this.useFastJson ? JSON.parseObject(json, clazz) : this.objectMapper.readValue(json, clazz);
    }

    public void recoverInsert(String[] indexes, List<? extends BaseEsDoc> sources) {
        if (sources == null || sources.size() == 0) {
            log.info("数据源为空，跳过！");
        }

        this.deleteByType(indexes, sources.get(0).getClass());
        this.insertToEs(sources);
    }

    public long deleteByIndex(String indexName) {
        return this.deleteByQuery(new String[]{indexName}, (Class<?>) null, QueryBuilders.matchAllQuery());
    }

    public long deleteByType(String[] indexes, Class<? extends BaseEsDoc> clazz) {
        return this.deleteByQuery(indexes, clazz, QueryBuilders.matchAllQuery());
    }

    public String getLatestIndexName(Class<?> entityClass) {
        return IndexUtils.getLatestIndexName(entityClass);
    }

    public String getType(Class<?> entityClass) {
        return IndexUtils.getType(entityClass);
    }

    public String[] getTypes(Class<?>[] entityClasses) {
        return IndexUtils.getTypes(entityClasses);
    }

    public String getAlias(Class<?> clazz) {
        return IndexUtils.getAlias(clazz);
    }

    public <T extends BaseEsDoc> List<T> queryAllByType(String[] indexes, Class<T> typeClazz) {
        return this.queryAllByQuery(indexes, this.getType(typeClazz), QueryBuilders.matchAllQuery(), typeClazz);
    }

    public <T extends BaseEsDoc> List<T> queryAllByQuery(String[] indexes, Class<T> typeClazz, QueryBuilder queryBuilder, SearchQuerySetting searchQuerySetting) {
        return this.queryAllByQuery(indexes, this.getType(typeClazz), queryBuilder, searchQuerySetting, typeClazz);
    }

    public <T extends BaseEsDoc> List<T> queryAllByQuery(String[] indexes, Class<T> typeClazz, QueryBuilder queryBuilder) {
        return this.queryAllByQuery(indexes, typeClazz, queryBuilder, null);
    }

    public <T extends BaseEsDoc> List<T> queryAllByQuery(String[] indexes, String type, QueryBuilder queryBuilder, Class<T> clazz) {
        return this.queryAllByQuery(indexes, type, queryBuilder, null, clazz);
    }

    /**
     * 返回JSONObject的es查询方式
     * @param indexes
     * @param type
     * @param queryBuilder
     * @return
     */
    public List<JSONObject> queryAllByQuery(String[] indexes, String type, QueryBuilder queryBuilder) {
        List<String> queryIndexes = new ArrayList();

        for (String index : indexes) {
            if (this.esTemplate.indexExists(index)) {
                queryIndexes.add(index);
            } else {
                log.warn("索引[{}]不存在，已忽略", index);
            }
        }

        if (queryIndexes.size() == 0) {
            log.warn("指定索引均[{}]不存在", StringUtils.join(indexes, ","));
            return new ArrayList();
        } else {
            NativeSearchQueryBuilder nativeSearchQueryBuilder = (new NativeSearchQueryBuilder()).withQuery(queryBuilder)
                    .withIndices(queryIndexes.toArray(new String[0])).withTypes(type);

            NativeSearchQuery searchQuery = nativeSearchQueryBuilder.withPageable(PageRequest.of(0, 10000)).build();
            ArrayList<JSONObject> list = new ArrayList();

            try {
                String scrollId;
                for(Page scrollPage = this.esTemplate.startScroll(60000L, searchQuery, JSONObject.class); scrollPage.hasContent(); scrollPage = this.esTemplate.continueScroll(scrollId, 60000L, JSONObject.class)) {
                    list.addAll(scrollPage.getContent());
                    scrollId = ((ScrolledPage)scrollPage).getScrollId();
                }
                return list;
            } catch (IndexNotFoundException var12) {
                log.warn("索引或别名[{}]不存在", StringUtils.join(indexes, ","));
                return new ArrayList();
            }
        }
    }


    public <T extends BaseEsDoc> List<T> queryAllByQuery(String[] indexes, String type, QueryBuilder queryBuilder, SearchQuerySetting searchQuerySetting, Class<T> clazz) {
        int scrollTime;
        if (this.checkIndexBeforeQuery) {
            List<String> queryIndexes = new ArrayList();
            String[] var7 = indexes;
            scrollTime = indexes.length;

            for (int var9 = 0; var9 < scrollTime; ++var9) {
                String index = var7[var9];
                if (this.esTemplate.indexExists(index)) {
                    queryIndexes.add(index);
                } else {
                    log.warn("索引[{}]不存在，已忽略", index);
                }
            }

            if (queryIndexes.size() == 0) {
                log.warn("指定索引均[{}]不存在", StringUtils.join(indexes, ","));
                return new ArrayList();
            }

            indexes = queryIndexes.toArray(new String[0]);
        }

        NativeSearchQueryBuilder nativeSearchQueryBuilder = (new NativeSearchQueryBuilder()).withQuery(queryBuilder)
                .withIndices(indexes).withTypes(type);
        int scrollSize = 10000;
        scrollTime = 60000;
        if (searchQuerySetting != null) {
            searchQuerySetting.setting(nativeSearchQueryBuilder);
            scrollSize = searchQuerySetting.getScrollSize();
            scrollTime = searchQuerySetting.getScrollTime();
        }

        NativeSearchQuery searchQuery = nativeSearchQueryBuilder.withPageable(PageRequest.of(0, scrollSize)).build();
        ArrayList list = new ArrayList();

        try {
            Page<T> scrollPage = this.esTemplate.startScroll(scrollTime, searchQuery, clazz);

            String scrollId;
            for (scrollId = null; scrollPage.hasContent(); scrollPage = this.esTemplate.continueScroll(scrollId, scrollTime, clazz)) {
                list.addAll(scrollPage.getContent());
                scrollId = ((ScrolledPage) scrollPage).getScrollId();
            }

            if (scrollId != null) {
                this.esTemplate.clearScroll(scrollId);
            }

            return list;
        } catch (IndexNotFoundException var13) {
            log.warn("索引或别名[{}]不存在", StringUtils.join(indexes, ","));
            return new ArrayList();
        }
    }

    public <T extends BaseEsDoc> Page<T> queryPageByQuery(String[] index, Class<T> typeClazz, QueryBuilder queryBuilder, SearchQuerySetting searchQuerySetting, Pageable pageable) {
        return this.queryPageByQuery(index, this.getType(typeClazz), queryBuilder, searchQuerySetting, typeClazz, pageable);
    }

    public <T extends BaseEsDoc> Page<T> queryPageByQuery(String[] index, Class<T> typeClazz, QueryBuilder queryBuilder, Pageable pageable) {
        return this.queryPageByQuery(index, typeClazz, queryBuilder, null, pageable);
    }

    public <T extends BaseEsDoc> Page<T> queryPageByQuery(String index, String type, QueryBuilder queryBuilder, Class<T> clazz, Pageable pageable) {
        return this.queryPageByQuery(new String[]{index}, type, queryBuilder, null, clazz, pageable);
    }

    public <T extends BaseEsDoc> Page<T> queryPageByQuery(String[] index, String type, QueryBuilder queryBuilder, SearchQuerySetting searchQuerySetting, Class<T> clazz, Pageable pageable) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = (new NativeSearchQueryBuilder()).withQuery(queryBuilder).withIndices(index).withTypes(type);
        if (searchQuerySetting != null) {
            searchQuerySetting.setting(nativeSearchQueryBuilder);
        }

        NativeSearchQuery searchQuery = nativeSearchQueryBuilder.withPageable(pageable).build();

        try {
            return this.esTemplate.queryForPage(searchQuery, clazz);
        } catch (IndexNotFoundException var10) {
            log.warn("索引或别名[{}]不存在", Arrays.toString(index));
            return Page.empty(pageable);
        }
    }

    public <T extends BaseEsTimeDoc> List<T> queryAllTimeDocByRange(Long startTime, Long endTime, Class<T> clazz, QueryBuilder... queryBuilders) {
        return this.queryAllSortedTimeDocByRange(startTime, endTime, clazz, null, queryBuilders);
    }

    public <T extends BaseEsTimeDoc> List<T> queryAllSortedTimeDocByRange(Long startTime, Long endTime, Class<T> clazz, SortOrder sortOrder, QueryBuilder... queryBuilders) {
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(DEFAULT_RANG_FIELD).gte(startTime).lte(endTime);
        QueryBuilder dstQueryBuilder;
        if (queryBuilders == null) {
            dstQueryBuilder = rangeQuery;
        } else {
            BoolQueryBuilder tmpQb = QueryBuilders.boolQuery().must(rangeQuery);
            QueryBuilder[] var9 = queryBuilders;
            int var10 = queryBuilders.length;

            for (int var11 = 0; var11 < var10; ++var11) {
                QueryBuilder queryBuilder = var9[var11];
                tmpQb.must(queryBuilder);
            }

            dstQueryBuilder = tmpQb;
        }

        return sortOrder != null ? this.queryAllByQuery(IndexUtils.getIndexNamesByTimeRange(clazz,
                startTime, endTime), clazz, dstQueryBuilder,
                (searchQueryBuilder) -> searchQueryBuilder.withSort(SortBuilders.fieldSort(DEFAULT_RANG_FIELD).order(sortOrder)))
                : this.queryAllByQuery(IndexUtils.getIndexNamesByTimeRange(clazz, startTime, endTime), clazz, dstQueryBuilder);
    }

    public <T extends BaseEsDoc> List<T> queryAllByTypeAndField(String[] indexes, Class<T> typeClazz, String fieldName, Object fieldValue) {
        return this.queryAllByTypeAndField(indexes, this.getType(typeClazz), fieldName, fieldValue, typeClazz);
    }

    public <T extends BaseEsDoc> List<T> queryAllByTypeAndField(String[] indexes, String type, String fieldName, Object fieldValue, Class<T> clazz) {
        return this.queryAllByQuery(indexes, type, QueryBuilders.termQuery(fieldName, fieldValue), clazz);
    }

    public <T extends BaseEsDoc> List<T> processSearchResponse(SearchResponse searchResponse, Class<T> clazz) throws IOException {
        List<T> list = new ArrayList();
        SearchHit[] var4 = searchResponse.getHits().getHits();
        int var5 = var4.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            SearchHit hit = var4[var6];
            list.add(this.toObject(hit.getSourceAsString(), clazz));
        }

        return list;
    }

    /**
     * 根据orderField排序获取最新数据
     * @param indexes
     * @param type
     * @param queryBuilder
     * @param orderField
     * @param clazz
     * @param <T>
     * @return
     */
    public <T extends BaseEsDoc> T queryLatest(String[] indexes, String type, QueryBuilder queryBuilder, String orderField, Class<T> clazz) {
        List<T> list;
        try {
            SearchRequestBuilder searchRequestBuilder = this.client.prepareSearch(indexes).setTypes(new String[]{type}).setSize(1)
                    .addSort(SortBuilders.fieldSort(orderField).order(SortOrder.DESC).unmappedType("long")).setQuery(queryBuilder);
            SearchResponse searchResponse = searchRequestBuilder.get();

            try {
                list = this.processSearchResponse(searchResponse, clazz);
            } catch (IOException var10) {
                log.error("解析ES查询结果出错", var10);
                return null;
            }
        } catch (IndexNotFoundException var11) {
            log.error("索引[{}]不存在", indexes, var11);
            return null;
        }

        return list.size() == 0 ? null : list.get(0);
    }

    public <T extends BaseEsTimeDoc> T queryTimeDocLatest(String[] indexes, Class<T> typeClass) {
        return this.queryLatest(indexes, this.getType(typeClass), QueryBuilders.matchAllQuery(), DEFAULT_RANG_FIELD, typeClass);
    }

    public <T extends BaseEsTimeDoc> T queryTimeDocLatest(String[] indexes, Class<T> typeClass, QueryBuilder queryBuilder) {
        return this.queryLatest(indexes, this.getType(typeClass), queryBuilder, DEFAULT_RANG_FIELD, typeClass);
    }

    public <T extends BaseEsDoc> long countByType(String[] indexes, Class<T> typeClazz) {
        return this.countByQuery(indexes, this.getType(typeClazz), QueryBuilders.matchAllQuery(), typeClazz);
    }

    public <T extends BaseEsDoc> long countByQuery(String[] indexes, Class<T> typeClazz, QueryBuilder queryBuilder, SearchQuerySetting searchQuerySetting) {
        return this.countByQuery(indexes, this.getType(typeClazz), queryBuilder, searchQuerySetting, typeClazz);
    }

    public <T extends BaseEsDoc> long countByQuery(String[] indexes, Class<T> typeClazz, QueryBuilder queryBuilder) {
        return this.countByQuery(indexes, typeClazz, queryBuilder, null);
    }

    public <T extends BaseEsDoc> long countByQuery(String[] indexes, String type, QueryBuilder queryBuilder, Class<T> clazz) {
        return this.countByQuery(indexes, type, queryBuilder, null, clazz);
    }

    public <T extends BaseEsDoc> long countByQuery(String index, String type, QueryBuilder queryBuilder, SearchQuerySetting searchQuerySetting, Class<T> clazz) {
        return this.countByQuery(new String[]{index}, type, queryBuilder, searchQuerySetting, clazz);
    }

    public <T extends BaseEsDoc> long countByQuery(String[] indexes, String type, QueryBuilder queryBuilder, SearchQuerySetting searchQuerySetting) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = (new NativeSearchQueryBuilder()).withQuery(queryBuilder).withIndices(indexes).withTypes(type);
        if (searchQuerySetting != null) {
            searchQuerySetting.setting(nativeSearchQueryBuilder);
        }

        NativeSearchQuery searchQuery = nativeSearchQueryBuilder.build();

        try {
            return this.esTemplate.count(searchQuery);
        } catch (IndexNotFoundException var9) {
            log.warn("索引或别名[{}]不存在", indexes);
            return 0L;
        }
    }

    public <T extends BaseEsDoc> long countByQuery(String[] indexes, String type, QueryBuilder queryBuilder, SearchQuerySetting searchQuerySetting, Class<T> clazz) {
        return this.countByQuery(indexes, type, queryBuilder, searchQuerySetting);
    }

    public <T extends BaseEsDoc> T queryLatest(String[] indexes, Class<T> typeClass, QueryBuilder queryBuilder, String orderField) {
        return this.queryLatest(indexes, this.getType(typeClass), queryBuilder, orderField, typeClass);
    }

    public <T extends BaseEsDoc> T queryLatest(String[] indexes, Class<T> typeClass, String orderField) {
        return this.queryLatest(indexes, typeClass, QueryBuilders.matchAllQuery(), orderField);
    }

    public <T extends BaseEsTimeDoc> T queryLatest(String[] indexes, Class<T> typeClass) {
        return this.queryLatest(indexes, typeClass, DEFAULT_RANG_FIELD);
    }

    public <T extends BaseEsTimeDoc> List<T> queryLatestDocs(String[] indexes, Class<T> typeClass) {
        T latest = this.queryLatest(indexes, typeClass, DEFAULT_RANG_FIELD);
        return latest == null ? new ArrayList() : this.queryAllByTypeAndField(indexes, typeClass, DEFAULT_RANG_FIELD, latest.getObservedTime());
    }

    public <T extends BaseEsTimeDoc> List<T> queryLatestDocs(String[] indexes, Class<T> typeClass, QueryBuilder queryBuilder) {
        T latest = this.queryLatest(indexes, typeClass, queryBuilder, DEFAULT_RANG_FIELD);
        if (latest == null) {
            return new ArrayList();
        } else {
            BoolQueryBuilder dstQb;
            if (queryBuilder instanceof BoolQueryBuilder) {
                dstQb = (BoolQueryBuilder) queryBuilder;
            } else {
                dstQb = QueryBuilders.boolQuery();
                dstQb.must(queryBuilder);
            }

            dstQb.must(QueryBuilders.termQuery(DEFAULT_RANG_FIELD, latest.getObservedTime()));
            return this.queryAllByQuery(indexes, typeClass, dstQb);
        }
    }

    public Long queryLatestTime(String[] indexes, Class<? extends BaseEsTimeDoc> typeClass) {
        BaseEsTimeDoc baseEsTimeDoc = this.queryLatest(indexes, typeClass);
        return baseEsTimeDoc != null ? baseEsTimeDoc.getObservedTime() : 0L;
    }

    public Long queryLatestTime(String index, Class<?> typeClass, QueryBuilder queryBuilder){
        return queryLatestTime(index, typeClass, queryBuilder, DEFAULT_RANG_FIELD);
    }

    public Long queryLatestTime(String index, Class<?> typeClass, QueryBuilder queryBuilder, String fieldName) {
        String queryIndex = null;
        if (this.esTemplate.indexExists(index)) {
            queryIndex = index;
        } else {
            log.warn("索引[{}]不存在，已忽略", index);
        }

        if (queryIndex == null) {
            log.warn("指定索引{}不存在", index);
            return null;
        } else {
            FieldSortBuilder sortBuilder = new FieldSortBuilder(fieldName);
            //倒序
            sortBuilder.order(SortOrder.DESC);
            NativeSearchQueryBuilder nativeSearchQueryBuilder = (new NativeSearchQueryBuilder()).withQuery(queryBuilder)
                    .withIndices(queryIndex).withTypes(getType(typeClass)).withSort(sortBuilder);

            NativeSearchQuery searchQuery = nativeSearchQueryBuilder.withPageable(PageRequest.of(0, 100)).build();

            //获取所有数据
            List<JSONObject> queryForList = esTemplate.queryForList(searchQuery, JSONObject.class);
            if (queryForList != null && queryForList.size() > 0) {
                JSONObject object = queryForList.get(0);
                return object.getLong(fieldName);
            } else {
                return null;
            }
        }
    }

    public SearchResponse getQueryResponse(String[] indexes, Class<?> typeClass, QueryBuilder queryBuilder, AbstractAggregationBuilder... aggBuilders) {
        return this.getQueryResponse(indexes, new Class[]{typeClass}, queryBuilder, aggBuilders);
    }

    public SearchResponse getQueryResponse(String[] indexes, Class<?>[] typeClasses, QueryBuilder queryBuilder, AbstractAggregationBuilder... aggBuilders) {
        SearchRequestBuilder searchRequestBuilder = this.client.prepareSearch(indexes)
                .setTypes(this.getTypes(typeClasses)).setQuery(queryBuilder).setSize(0);
        if (aggBuilders != null) {
            AbstractAggregationBuilder[] var6 = aggBuilders;
            int var7 = aggBuilders.length;

            for (int var8 = 0; var8 < var7; ++var8) {
                AbstractAggregationBuilder aggBuilder = var6[var8];
                searchRequestBuilder.addAggregation(aggBuilder);
            }
        }

        return searchRequestBuilder.get();
    }

    /**
     * 根据groupField字段桶排序
     * @param indexes 多个索引
     * @param typeClass type
     * @param queryBuilder 查询语句
     * @param groupField 分组字段
     * @param sortBuilder 排序语句
     * @param <T> 排序后的实体类list
     * @return
     */
    public <T> List<T> queryGroupTopHits(String[] indexes, Class<T> typeClass, QueryBuilder queryBuilder, String groupField, SortBuilder sortBuilder) {
        //广度优先遍历
        TermsAggregationBuilder aggBuilder = AggregationBuilders.terms("table").field(groupField)
                .collectMode(Aggregator.SubAggCollectionMode.BREADTH_FIRST).size(500);
        aggBuilder.subAggregation(AggregationBuilders.topHits("first").size(1).sort(sortBuilder));
        SearchResponse searchResponse = this.client.prepareSearch(indexes).setTypes(this.getType(typeClass))
                .setQuery(queryBuilder).setSize(0).addAggregation(aggBuilder).get();
        List<T> list = new ArrayList();
        Aggregations aggregations = searchResponse.getAggregations();
        Terms terms = aggregations.get("table");
//        List<InternalTerms.Bucket> buckets = ((InternalMappedTerms) aggregations.get("table")).getBuckets();

        for (Terms.Bucket bucket : terms.getBuckets()) {
            SearchHit[] firsts = ((TopHits) bucket.getAggregations().get("first")).getHits().getHits();
            try {
                list.add(this.toObject(firsts[0].getSourceAsString(), typeClass));
            } catch (IOException var15) {
                log.error("ES查询结果反序列化出错", var15);
            }
        }

        return list;
    }

    /**
     * 根据两个字段进行桶排序分组
     * @param indexes 多个索引
     * @param typeClass type
     * @param queryBuilder 查询语句
     * @param groupField1 分组字段1
     * @param groupField2 分组字段2
     * @param sortBuilder 排序语句
     * @param <T> 返回实体类list
     * @return
     */
    public <T> List<T> queryGroupsTopHits(String[] indexes, Class<T> typeClass, QueryBuilder queryBuilder, String groupField1, String groupField2, SortBuilder sortBuilder) {
        List<T> list = new ArrayList();
        TermsAggregationBuilder aggBuilder = AggregationBuilders.terms("table1").field(groupField1).size(500);
        aggBuilder.subAggregation(AggregationBuilders.terms("table2").field(groupField2).size(500).subAggregation(AggregationBuilders.topHits("first").size(1).sort(sortBuilder)));
        SearchResponse searchResponse = this.client.prepareSearch(indexes).setTypes(this.getType(typeClass)).setQuery(queryBuilder).setSize(0).addAggregation(aggBuilder).get();
        Aggregations aggregations = searchResponse.getAggregations();
        Terms tableTemp1 = aggregations.get("table1");
//        List<InternalTerms.Bucket> buckets = ((InternalMappedTerms) aggregations.get("table1")).getBuckets();

        for (Terms.Bucket bucket : tableTemp1.getBuckets()) {
            Terms tableTemp2 = bucket.getAggregations().get("table2");
            for (Terms.Bucket table2 : tableTemp2.getBuckets()) {
                SearchHit[] firsts = ((TopHits) table2.getAggregations().get("first")).getHits().getHits();
                try {
                    list.add(this.toObject(firsts[0].getSourceAsString(), typeClass));
                } catch (IOException var19) {
                    log.error("ES查询结果反序列化出错", var19);
                }
            }
        }
        return list;
    }

    /**
     * 根据多个字段分组桶排序
     * @param indexes 多个索引
     * @param typeClass type
     * @param queryBuilder 查询语句
     * @param groupFields 分组字段list
     * @param sortBuilder 排序语句
     * @param <T> 实体类list
     * @return
     */
    public <T> List<T> queryGroupsTopHits(String[] indexes, Class<T> typeClass, QueryBuilder queryBuilder, List<String> groupFields, SortBuilder sortBuilder) {
        TermsAggregationBuilder aggBuilder = AggregationBuilders.terms("table" + (groupFields.size() - 1))
                .field(groupFields.get(groupFields.size() - 1)).size(500)
                .subAggregation(AggregationBuilders.topHits("first").size(1).sort(sortBuilder));

        for (int i = groupFields.size() - 2; i >= 0; --i) {
            aggBuilder = AggregationBuilders.terms("table" + i).field(groupFields.get(i)).size(500).subAggregation(aggBuilder);
        }

        SearchResponse searchResponse = this.client.prepareSearch(indexes).setTypes(this.getType(typeClass)).setQuery(queryBuilder).setSize(0).addAggregation(aggBuilder).get();
        Aggregations aggregations = searchResponse.getAggregations();
        return this.doFor(aggregations, groupFields.size(), 0, typeClass);
    }

    /**
     * 递归多个字段排序获取
     * @param aggregations
     * @param size
     * @param index
     * @param typeClass
     * @param <T>
     * @return
     */
    private <T> List<T> doFor(Aggregations aggregations, int size, int index, Class<T> typeClass) {
        List<T> list = new ArrayList();
        List<InternalTerms.Bucket> buckets = ((InternalMappedTerms) aggregations.get("table" + index)).getBuckets();

        for (InternalTerms.Bucket table : buckets) {
            if (index < size - 1) {
                list.addAll(this.doFor(table.getAggregations(), size, index + 1, typeClass));
            } else {
                SearchHit[] firsts = ((TopHits) table.getAggregations().get("first")).getHits().getHits();

                try {
                    list.add(this.toObject(firsts[0].getSourceAsString(), typeClass));
                } catch (IOException var11) {
                    log.error("ES查询结果反序列化出错", var11);
                }
            }
        }

        return list;
    }

    public List<GroupSum> queryGroupSumTopN(String[] indexes, Class<?> typeClass, QueryBuilder queryBuilder, String groupField, String sumField, int topNum) {
        TermsAggregationBuilder aggBuilder = AggregationBuilders.terms("group").field(groupField).size(topNum)
                .order(Terms.Order.aggregation("count", false));
        aggBuilder.subAggregation(AggregationBuilders.sum("count").field(sumField));
        SearchResponse searchResponse = this.client.prepareSearch(indexes).setTypes(this.getType(typeClass))
                .setQuery(queryBuilder).setSize(0).addAggregation(aggBuilder).get();
        List<GroupSum> list = new ArrayList();
        Aggregations aggregations = searchResponse.getAggregations();
        List<StringTerms.Bucket> buckets = ((StringTerms) aggregations.get("group")).getBuckets();

        for (StringTerms.Bucket bucket : buckets) {
            GroupSum groupSum = new GroupSum();
            double value = ((Sum) bucket.getAggregations().get("count")).getValue();
            groupSum.setKey(bucket.getKeyAsString());
            groupSum.setSum(value);
            list.add(groupSum);
        }

        return list;
    }

    public long deleteByField(String[] indexes, Class<?> typeClass, String fieldName, Object fieldValue) {
        String type = this.getType(typeClass);
        return this.deleteByField(indexes, type, fieldName, fieldValue);
    }

    public long deleteByField(String[] indexes, String type, String fieldName, Object fieldValue) {
        return this.deleteByQuery(indexes, type, QueryBuilders.termQuery(fieldName, fieldValue));
    }

    public long deleteByQuery(String[] indexes, Class<?> typeClass, QueryBuilder queryBuilder) {
        return this.deleteByQuery(indexes, this.getType(typeClass), queryBuilder);
    }

    public long deleteByQuery(String[] indexes, String type, QueryBuilder queryBuilder) {
        DeleteByQueryRequestBuilder deleteByQueryRequestBuilder = new DeleteByQueryRequestBuilder(this.client, DeleteByQueryAction.INSTANCE);
        DeleteByQueryRequest deleteByQueryRequest = deleteByQueryRequestBuilder.request();
        deleteByQueryRequest.types(type);
        deleteByQueryRequest.indices(indexes);
        deleteByQueryRequestBuilder.filter(queryBuilder);
        BulkByScrollResponse bulkByScrollResponse = deleteByQueryRequestBuilder.get();
        long deleted = bulkByScrollResponse.getDeleted();
        log.info("移除index为：{}, type为{}下{}条旧数据", indexes, type, deleted);
        return deleted;
    }

    public boolean delete(String index, String type, String id) {
        return this.client.prepareDelete(index, type, id).get().status() == RestStatus.ACCEPTED;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public <T> List<T> search(EsParameter param, Class<T> clazz) {
        param.setType(this.getType(clazz));
        JSONArray jsonArray = this.search(param);
        return JSONArray.parseArray(jsonArray.toJSONString(), clazz);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public JSONArray search(EsParameter param) {
        long t1 = System.currentTimeMillis();
        String index = param.getIndex();
        if (index == null) {
            index = IndexUtils.getDefaultAlias();
        }

        String type = param.getType();
        SearchRequestBuilder searchRequestBuilder = this.client.prepareSearch(index);
        if (StringUtils.isNotBlank(type)) {
            searchRequestBuilder.setTypes(type.split(","));
        }

        List<String> mustFields = param.getMustField();
        List<String> mustInFields = param.getMustInField();
        if (mustFields != null || mustInFields != null) {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            List<Object> mustFieldValue = param.getMustFieldValue();
            if (mustFields != null) {
                for (int i = 0; i < mustFields.size(); ++i) {
                    queryBuilder.filter(QueryBuilders.matchQuery(mustFields.get(i), mustFieldValue.get(i)));
                }
            }

            List<Object[]> mustInFieldValue = param.getMustInFieldValue();
            if (mustInFieldValue != null) {
                for (int i = 0; i < mustInFields.size(); ++i) {
                    queryBuilder.filter(QueryBuilders.termsQuery(mustInFields.get(i), mustInFieldValue.get(i)));
                }
            }

            searchRequestBuilder.setQuery(queryBuilder);
        }

        String rangField = param.getRangeField();
        if (rangField != null) {
            Object rangeFrom = param.getRangeFrom();
            Object rangeTo = param.getRangeTo();
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(rangField);
            if (rangeFrom != null) {
                rangeQueryBuilder.from(rangeFrom, param.isIncludeLower());
            }

            if (rangeTo != null) {
                rangeQueryBuilder.to(rangeTo, param.isIncludeUpper());
            }

            searchRequestBuilder.setPostFilter(rangeQueryBuilder);
        }

        String orderField = param.getOrderField();
        if (orderField != null) {
            if (param.isAsc()) {
                searchRequestBuilder.addSort(orderField, SortOrder.ASC);
            } else {
                searchRequestBuilder.addSort(orderField, SortOrder.DESC);
            }
        }

        if (param.getFetchFields() != null && param.getFetchFields().length > 0) {
            searchRequestBuilder.setFetchSource(param.getFetchFields(), null);
        }

        SearchResponse searchResponse = searchRequestBuilder.setScroll(new TimeValue(60000L))
                .setSearchType(SearchType.DEFAULT).setSize(1000).get();
        JSONArray jsonArray = this.getJSONArray(searchResponse);
        long t2 = System.currentTimeMillis();
        log.info("es查询type为{}的{}条数据，耗时{}", type, jsonArray.size(), t2 - t1);
        return jsonArray;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public JSONArray getJSONArray(SearchResponse scrollResp) {
        String scrollId = scrollResp.getScrollId();

        JSONArray jsonArray;
        for (jsonArray = new JSONArray(); scrollResp.getHits().getHits().length != 0; scrollId = scrollResp.getScrollId()) {
            SearchHit[] var4 = scrollResp.getHits().getHits();

            for (SearchHit hit : var4) {
                String sourceAsString = hit.getSourceAsString();
                JSONObject jsonObject = JSONObject.parseObject(sourceAsString);
                jsonArray.add(jsonObject);
            }

            scrollResp = this.client.prepareSearchScroll(scrollId).setScroll(new TimeValue(60000L)).execute().actionGet();
        }

        this.clearScroll(scrollId);
        return jsonArray;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public Long queryLatestTimestamp(Class<?> clazz) {
        return this.queryLatestTimestamp(IndexUtils.getDefaultAlias(), clazz);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public Long queryLatestTimestamp(String index, Class<?> clazz) {
        return this.queryLatestTimestamp(new String[]{index}, clazz);
    }

    public Long queryLatestTimestamp(String[] indexes, Class<?> clazz) {
        String type = this.getType(clazz);
        long t1 = System.currentTimeMillis();
        SearchResponse scrollResp = this.client.prepareSearch(indexes).setTypes(type)
                .addSort(DEFAULT_RANG_FIELD, SortOrder.DESC).setSize(1).get();
        JSONObject jsonObject = null;
        SearchHit[] var8 = scrollResp.getHits().getHits();

        for (SearchHit hit : var8) {
            String sourceAsString = hit.getSourceAsString();
            jsonObject = JSONObject.parseObject(sourceAsString);
        }

        if (jsonObject == null) {
            log.warn("当前索引[{}]，没有[{}]类型的数据", StringUtils.join(indexes, ","), type);
            return System.currentTimeMillis();
        } else {
            Long aLong = jsonObject.getLong("observedTime");
            long t2 = System.currentTimeMillis();
            log.info("查询type为{}最新时间耗时{},最新时间戳为{}", type, t2 - t1, aLong);
            return aLong;
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public <T> List<T> rangeQueryByObservedTime(Class<T> clazz, long start, long end) {
        String[] indexes = IndexUtils.getIndexNamesByTimeRange(clazz, start, end);
        String type = this.getType(clazz);

        try {
            SearchResponse scrollResp = this.client.prepareSearch(indexes).setTypes(type)
                    .addSort(DEFAULT_RANG_FIELD, SortOrder.ASC).setScroll(new TimeValue(60000L))
                    .setQuery(QueryBuilders.rangeQuery("observedTime").gte(start).lte(end)).setSize(1000).get();
            JSONArray jsonArray = this.getJSONArray(scrollResp);
            return JSONArray.parseArray(jsonArray.toJSONString(), clazz);
        } catch (IndexNotFoundException var10) {
            log.warn("索引或别名[{}]不存在", StringUtils.join(indexes, ","));
            return new ArrayList();
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    private boolean clearScroll(String scrollId) {
        ClearScrollRequestBuilder clearScrollRequestBuilder = this.client.prepareClearScroll();
        clearScrollRequestBuilder.addScrollId(scrollId);
        ClearScrollResponse response = clearScrollRequestBuilder.get();
        return response.isSucceeded();
    }

    public <T extends BaseEsDoc> List<T> distinctDoc(List<T> sources) {
        Iterator<? extends BaseEsDoc> iterator = sources.iterator();
        HashSet set = new HashSet();

        while (iterator.hasNext()) {
            BaseEsDoc doc = iterator.next();
            String id = doc.getDocId();
            if (id != null && !set.add(id)) {
                iterator.remove();
            }
        }

        return sources;
    }
}
