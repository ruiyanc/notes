package com.hxlc.api.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
//import com.hxlc.api.dao.NifiInfoJpa;
import com.hxlc.api.entity.DataInfo;
//import com.hxlc.api.entity.NifiInfo;
import com.hxlc.api.service.DataService;
import com.hxlc.api.uilts.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("template")
public class TemplateController {

    public static final String api = "http://10.225.1.24:7777/nifi-api";
//    public static final String api28 = "http://10.225.1.28:8084//nifi-api";
    public static final String dmzApi = "http://10.225.1.1:8989/nifi-api";

    public static final String nifiFlowId = "0ad787a0-017b-1000-ab3b-ed0649336a42";
//    public static final String nifiFlowId28 = "f2e99fba-017b-1000-4f64-ea920c295aa9";
    public static double originX = 0;
    public static double originY = 0;
    public static final String RUNNING = "RUNNING";
    public static final String STOPPED = "STOPPED";

    @Autowired
    private DataService dataService;
//    @Autowired
//    private NifiInfoJpa nifiInfoJpa;

    public static Map<String, String> dmzApiMap = new HashMap<>();
    public static Map<String, String> nifiMap = new HashMap<>();
    public static Map<String, String> sqlMap = new HashMap<>();

    static {
        dmzApiMap.put("10.224.153.16", "http://10.225.1.1:8989/nifi-api");
        dmzApiMap.put("10.224.114.122", "http://10.225.1.1:8999/nifi-api");
        //dmz地址对应nifi模板rootId
        nifiMap.put("10.224.153.16", "29ab1c0e-017b-1000-d3d0-c3e6001a9bd1");
        nifiMap.put("10.224.114.122", "475cb317-017d-1000-ae14-d83c164b5b14");

        sqlMap.put(api, "198ee55f-017b-1000-0000-00004a18254f");
        //sqlMap.put(api1, "98805404-3b73-325d-020f-5a23b5d1f9f9");
    }


    /**
     * 集群服务器上添加nifi模板
     *
     * @param map1
     * @return
     */
    @PostMapping("addProcessorGroup")
    public String addTemplate(@RequestBody Map<String, Object> map1) {
        try {
            String encode = map1.get("encode").toString();
            String name = map1.get("name") == null ? "" : map1.get("name").toString();
            //之前是是由后端配置nifiFlowId，现在使用前端传过来的nifiFlowId
            String nifiFlowId = map1.get("nifiFlowId")== null ? "" :map1.get("nifiFlowId").toString();
            String type = map1.get("type").toString();
            String dataType = null;
            if (map1.get("dataType") != null) {
                dataType = map1.get("dataType").toString();
            }
            Object o = map1.get("paramMap");
            Map<String, String> paramMap = (Map<String, String>) o;
            //如果name重复则不创建
            if(nifiFlowId==null || "".equals(nifiFlowId)){
                nifiFlowId="0ad787a0-017b-1000-ab3b-ed0649336a42";
            }
            Map<String, String> groupMap = queryAllProcessorGroupMapByGroupId(api, nifiFlowId);
            //encode -> 模板名, name -> 模板描述
            List<String> list;
            if (encode.contains("_")) {
                String s = encode.substring(encode.lastIndexOf("_") + 1);
                list = queryAllEncodeList(api, s);
                if (!list.contains(s)) {
                    return "collection does not exist!";
                }
            } else {
                list = queryAllEncodeList(api, encode);
            }
            //encode -> 模板名, name -> 模板描述
            if (list.contains(encode)) {
                return "encode already exists!";
            }
            //查询出所有模板的name和id键值对
            Map<String, String> templates = queryMapTemplates(api);
            JSONObject template;
            //dataType=null为分发，不为null
            if (dataType != null) {
                if (groupMap.containsKey(dataType)) {
                    String dataTypeGroupId = groupMap.get(dataType);
                    template = createTemplateInstance(api, dataTypeGroupId, templates.get(type));
                    //修改采集处理器的坐标
                    String groupId = getGroupIdByTemplate(template);
                    JSONObject maxXY = getMaxXY(api, dataTypeGroupId);
                    BigDecimal x = (BigDecimal) maxXY.get("x");
                    BigDecimal y = (BigDecimal) maxXY.get("y");
                    Random random = new Random();
                    double v1 = random.nextDouble() * 1000;
                    double v2 = random.nextDouble() * 1000;
                    x = x.add(BigDecimal.valueOf(v1));
                    y = y.add(BigDecimal.valueOf(v2));
                    updateProcessorGroupXY(api, groupId, x, y);

                    //采集encode入库
                    if (!encode.contains("ORDER")) {
                        DataInfo dataInfo = new DataInfo();
                        dataInfo.setEncode(encode);
                        dataInfo.setDataName(name);
                        dataInfo.setDataType(dataType);
                        dataInfo.setDataState("启动");
                        dataInfo.setReplacementCycle(paramMap.get("num")==null?"10":paramMap.get("num"));
                        dataService.insertDataInfo(dataInfo);
                    }
                } else {
                    template = createTemplateInstance(api, nifiFlowId, templates.get(type));
                }
            } else {
                if (encode.contains("_")) {
                    template = createDist(map1, templates);
                } else {
                    return "encode do not '_', distribute error！";
                }
            }
            String groupId = getGroupIdByTemplate(template);
            //修改组名
            updateProcessorGroup(api, groupId, encode, name);
            //新增组的全局参数
            if (paramMap != null) {
                createVariable(api, groupId, paramMap);
                //修改采集频次
                String frequency = String.valueOf(paramMap.get("frequency"));
                if (frequency!=null && !"".equals(frequency)) {
                    updateProcessorFre(api,groupId,frequency);
                }
                //file Filter单独加参数
                Map<String, String> processorMap = findProcessorByGroupId(api, groupId);
                Map<String, String> variable = queryMapVariableByGroupId(api, groupId);
                processorMap.forEach((key, value) -> {
                    if (key.contains("FTP") || key.contains("File") || key.contains("AMQP") || key.contains("PutSQL")) {
                        if (key.contains("AMQP") || key.contains("FTP")) {
                            String password = variable.get("password");
                            paramMap.put("password", password);
                            if ("ConsumeAMQP".equals(key)) {
                                String queue = variable.get("queue");
                                paramMap.put("queue", queue);
                            }
                        }
                        if (key.contains("PutSQL")) {
                            paramMap.put("DBCPConnectionPool", api);
                        }
                        updateProcessor(api, value, paramMap, key);
                    }
                });
            }
            return groupId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建模板时获取groupId
     * @param template
     * @return
     */
    public String getGroupIdByTemplate(JSONObject template) {
        JSONObject flow = (JSONObject) template.get("flow");
        JSONArray processGroups = (JSONArray) flow.get("processGroups");
        JSONObject jsonObject = (JSONObject) processGroups.get(0);
        return jsonObject.get("id").toString();
    }

    /**
     * 从map中查询想要的map项，根据key
     */
    public static Map<String, String> parseMapForFilter(Map<String, String> map, String filters) {
        if (map == null) {
            return null;
        } else {
            map = map.entrySet().stream()
                    .filter((e) -> e.getKey().contains(filters))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, Map.Entry::getValue
                    ));
        }
        return map;
    }

    /**
     * 在1.1nifi服务器上新增模板，dmz新增等
     *
     * @param map1
     * @return
     */
    @PostMapping("addProcessorGroupNew")
    public String addTemplateNew(@RequestBody Map map1) {
        try {
            String encode = map1.get("encode").toString();
            String name = map1.get("name") == null ? "" : map1.get("name").toString();
            String type = map1.get("type").toString();
            String dataType = null;
            if (map1.get("dataType") != null) {
                dataType = map1.get("dataType").toString();
            }
            Object o = map1.get("paramMap");
            Map<String, String> paramMap = (Map<String, String>) o;
            //如果name重复则不创建
            Map<String, String> groupMap = queryAllProcessorGroupMapByGroupId(api, nifiFlowId);
            //encode -> 模板名, name -> 模板描述
            List<String> list;
            if (encode.contains("_")) {
                String s = encode.substring(encode.lastIndexOf("_") + 1);
                list = queryAllEncodeList(api, s);
                if (!list.contains(s)) {
                    return "collection does not exist!";
                }
            } else {
                list = queryAllEncodeList(api, encode);
            }
            //encode -> 模板名, name -> 模板描述
            if (list.contains(encode)) {
                return "encode already exists!";
            }
            //查询出所有模板的name和id键值对
            Map<String, String> templates = queryMapTemplates(api);
            JSONObject template;
            if (dataType != null) {
                if (groupMap.containsKey(dataType)) {
                    String dataTypeGroupId = groupMap.get(dataType);
                    template = createTemplateInstance(api, dataTypeGroupId, templates.get(type));
                    //修改采集处理器的坐标
                    String groupId = getGroupIdByTemplate(template);
                    JSONObject maxXY = getMaxXY(api, dataTypeGroupId);
                    BigDecimal x = (BigDecimal) maxXY.get("x");
                    BigDecimal y = (BigDecimal) maxXY.get("y");
                    y = y.add(BigDecimal.valueOf(250));
                    updateProcessorGroupXY(api, groupId, x, y);
                } else {
                    template = createTemplateInstance(api, nifiFlowId, templates.get(type));
                }
                String groupId = getGroupIdByTemplate(template);
                //修改组名
                updateProcessorGroup(api, groupId, encode, name);
                createVariable(api, groupId, paramMap);
                return groupId;
            } else {
                //新增分发会和对应的监控建立连接
                if (encode.contains("_")) {
                    template = createDist(map1, templates);
                    String distGroupId = getGroupIdByTemplate(template);
                    if (encode.contains("FTP")) {
                        //新增1.1服务器组的全局参数
                        Map<String, String> paramMap1 = new HashMap<>();
                        paramMap1.put("ftpPath", encode);
                        paramMap1.put("host", paramMap.get("dmzHost"));
                        paramMap1.put("dmzUsername", "hxlc");
                        paramMap1.put("dmzPassword", "hxlc");
                        paramMap1.putAll(paramMap);
                        updateProcessorGroup(api, distGroupId, encode, name);
                        createVariable(api, distGroupId, paramMap1);
                        //file Filter单独加参数
                        if (paramMap1.get("dmzPassword") != null) {
                            Map<String, String> map = findProcessorByGroupId(api, distGroupId);
                            Map<String, String> ftpMap = parseMapForFilter(map, "FTP");
                            ftpMap.forEach((key, value) -> updateProcessor(api, value, paramMap1, key));
                        }
                        //1.1内网新增分发并在dmz上新增dmz转发
                        String dmzHost = paramMap.get("dmzHost");
                        String dmzNifiFlowId = nifiMap.get(dmzHost);
                        //dmzNifiId入库
//                        NifiInfo nifiInfo = nifiInfoJpa.findById(dmzHost).orElse(null);
//                        String dmzApi = nifiInfo != null ? nifiInfo.getNifiAddress() : null;
//                        String dmzNifiFlowId = nifiInfo != null ? nifiInfo.getRootGroupId() : null;
                        Map<String, String> dmzTemplates = queryMapTemplates(dmzApi);
                        String dmz = dmzTemplates.get("DMZ转发");
                        //如果name重复则不创建
//                        Map<String, String> dmzGroupMap = queryAllProcessorGroupMapByGroupId(dmzApi, dmzNifiFlowId);
                        List<String> dmzList = queryAllEncodeList(dmzApi, encode);
                        if (dmzList.contains(encode)) {
                            return "encode already exists!";
                        }
                        JSONObject dmzTemplate = createTemplateInstance(dmzApi, dmzNifiFlowId, dmz);
                        String dmzGroupId = getGroupIdByTemplate(dmzTemplate);
                        updateProcessorGroup(dmzApi, dmzGroupId, encode, name);
                        createVariable(dmzApi, dmzGroupId, paramMap);
                        //file Filter单独加参数
                        if (paramMap.get("password") != null) {
                            Map<String, String> mapzz = findProcessorByGroupId(dmzApi, dmzGroupId);
                            Map<String, String> ftpMap = parseMapForFilter(mapzz, "FTP");
                            ftpMap.forEach((key, value) -> updateProcessor(dmzApi, value, paramMap, key));
                        }
                        updateProcessorGroupStatus(dmzApi, dmzGroupId, RUNNING);
                        return distGroupId;
                    }
                    if (encode.contains("KAFKA")) {
                        //修改组名
                        updateProcessorGroup(api, distGroupId, encode, name);
                        createVariable(api, distGroupId, paramMap);
                        paramMap.put("DBCPConnectionPool", api);
                        Map<String, String> mapzz = findProcessorByGroupId(api, distGroupId);
                        Map<String, String> kafkaMap = parseMapForFilter(mapzz, "PutSQL");
                        kafkaMap.forEach((key, value) -> updateProcessor(api, value, paramMap, key));
                    }
                    return distGroupId;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    @PostMapping("addHttpGroup")
//    public String addHttpGroup(@RequestBody Map map1) {
//        try {
//            String encode = map1.get("encode").toString();
//            String name = map1.get("name") == null ? "" : map1.get("name").toString();
//            String type = map1.get("type").toString();
//            String dataType = null;
//            if (map1.get("dataType") != null) {
//                dataType = map1.get("dataType").toString();
//            }
//            Object o = map1.get("paramMap");
//            Map<String, String> paramMap = (Map<String, String>) o;
//            //如果name重复则不创建
//            Map<String, String> groupMap = queryAllProcessorGroupMapByGroupId(api28, nifiFlowId28);
//            List<String> list;
//            if (encode.contains("_")) {
//                String s = encode.substring(encode.lastIndexOf("_") + 1);
//                list = queryAllEncodeList(api28, s);
//                if (!list.contains(s)) {
//                    return "collection does not exist!";
//                }
//            } else {
//                list = queryAllEncodeList(api28, encode);
//            }
//            //encode -> 模板名, name -> 模板描述
//            if (list.contains(encode)) {
//                return "name already exists!";
//            }
//            //查询出所有模板的name和id键值对
//            Map<String, String> templates = queryMapTemplates(api28);
//            JSONObject template = null;
//            //dataType=null为分发，不为null
//            if (dataType != null) {
//                template = createTemplateInstance(api28, groupMap.getOrDefault(dataType, nifiFlowId28),
//                        templates.get(type));
//            } else {
//                if (encode.contains("_")) {
//                    String s = encode.substring(encode.lastIndexOf("_") + 1);
//                    for (String str : groupMap.keySet()) {
//                        String group = groupMap.get(str);
//                        Map<String, String> map = queryAllProcessorGroupMapByGroupId(api28, group);
//                        if (map.containsKey(s)) {
//                            template = createTemplateInstance(api28, group, templates.get(type));
//                            String groupId = getGroupIdByTemplate(template);
//                            String sourceId = map.get(s);
//                            JSONObject processGroup = queryProcessGroup(api28, sourceId);
//                            JSONObject position = (JSONObject) processGroup.get("position");
//                            BigDecimal x = (BigDecimal) position.get("x");
//                            x = x.add(BigDecimal.valueOf(400));
//                            BigDecimal y = (BigDecimal) position.get("y");
//                            //修改ftp组的xy坐标
//                            updateProcessorGroupXY(api28, groupId, x, y);
//                            createGroupConnections(api28, group, sourceId, groupId);
//                        }
//                    }
//                }
//            }
//            String groupId = getGroupIdByTemplate(template);
//            //修改组名
//            updateProcessorGroup(api28, groupId, encode, name);
//            createVariable(api28, groupId, paramMap);
////            updateProcessorGroupStatus(api28, groupId, "RUNNING");
//            return groupId;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    @GetMapping("getAllMonitor")
    public Map<String, JSONArray> getAllMonitor() {
        Map<String, JSONArray> resultMap = new HashMap<>();
        Map<String, String> groupMap = queryAllProcessorGroupMapByGroupId(api, nifiFlowId);
        groupMap.forEach((key, value) -> {
            JSONArray jsonArray = queryAllProcessorGroupByGroupId(api, value);
            JSONArray result = new JSONArray();
            for (Object o : jsonArray) {
                JSONObject object = (JSONObject) o;
                JSONObject component = (JSONObject) object.get("component");
                String name = component.get("name").toString();
                if (!name.contains("_")) {
                    String english = "[a-zA-Z]";
                    if (name.substring(0, 1).matches(english)) {
                        result.add(o);
                    }
                }
            }
            resultMap.put(key, result);
        });
        return resultMap;
    }

    @GetMapping("getFTPOrKafkaByEncode")
    public JSONArray getFTPOrKafkaByEncode(@RequestParam("encode") String encode) {
        //28服务器上
        JSONArray result = new JSONArray();
        Map<String, String> map = queryAllEncodeMapList(api, encode);
        if (map != null) {
            map.values().forEach(value -> {
                JSONObject jsonObject = queryProcessGroup(api, value);
                result.add(jsonObject);
            });
        }
        return result;
    }


    @PostMapping("delProcessorGroup")
    public String delProcessorGroup(@RequestBody Map map1) {
        String groupId = map1.get("groupId").toString();
        List<String> list = queryAllEncodeList(api, groupId);
        if (list != null && list.size() > 0) {
            String parentGroupId = queryParentProcessorGroupIdByGroupId(api, groupId);
            //在多个dmz上查询并删除
            JSONObject jsonObject = queryProcessGroup(api, groupId);
            JSONObject component = (JSONObject) jsonObject.get("component");
            String name = String.valueOf(component.get("name"));
            if(name==null ||"".equals(name)){
                return "encode error";
            }
//            List<NifiInfo> nifiInfoList = nifiInfoJpa.findAll();
            dmzApiMap.values().forEach(dmzApi -> {
//                String dmzApi = dmzApiMap.get(key);
                JSONArray jsonArray = queryAllEncodeObjectList(dmzApi, name);
                if (jsonArray != null && jsonArray.size() > 0) {
                    for (Object o : jsonArray) {
                        JSONObject object = (JSONObject) o;
                        String dmzId = object.get("id").toString();
                        updateProcessorGroupStatus(dmzApi, dmzId, STOPPED);
                        //终止所有处理器
                        Map<String, String> dmzMap = findProcessorByGroupId(dmzApi, dmzId);
                        dmzMap.values().forEach(
                                processId -> terminationProcessor(dmzApi, processId)
                        );
                        //清空dmz组的所有connection
                        List<String> connectionIdList = queryAllConnectionIdsByGroupId(dmzApi, dmzId);
                        connectionIdList.forEach(connection -> emptyQueue(dmzApi, connection));
                        deleteProcessorGroup(dmzApi, dmzId);
                    }
                }
            });
            //先暂停此组
            updateProcessorGroupStatus(api, groupId, STOPPED);
            Map<String, String> connectionMap = queryMapConnectionByGroupId(api, parentGroupId);
            String connectionId = connectionMap.get(groupId);
            if (connectionId != null) {
                String sourceGroupId = deleteConnectionById(api, connectionId);
                //如果采集存在其它分发则重启
                //清除缓存重新获取所有connection
                connectionMap = queryMapConnectionByGroupId(api, parentGroupId);
                String sourceConnectionId = connectionMap.get(sourceGroupId);
                if (sourceConnectionId != null) {
                    updateProcessorGroupStatus(api, sourceGroupId, RUNNING);
                }
            }
            //终止此组下面的所有处理器
            Map<String, String> processorMap = findProcessorByGroupId(api, groupId);
            for (String value : processorMap.values()) {
                terminationProcessor(api, value);
            }
            //清空当前组的所有connection
            List<String> connectionIdList = queryAllConnectionIdsByGroupId(api, groupId);
            for (String connection : connectionIdList) {
                emptyQueue(api, connection);
            }
            deleteProcessorGroup(api, groupId);
            if (map1.get("encode") != null && !"".equals(map1.get("encode"))) {
                String encode = map1.get("encode").toString();
                List<String> connectionList = queryConnectionListByGroupId(api, parentGroupId, groupId);
                for (String connection : connectionList) {
                    emptyQueue(api, connection);
                    deleteConnectionById(api, connection);
                }
                JSONArray jsonArray1 = queryAllEncodeObjectList(api, encode);
                if (jsonArray1 != null && jsonArray1.size() > 0) {
                    for (Object o : jsonArray1) {
                        JSONObject object = (JSONObject) o;
                        String id = object.get("id").toString();
                        updateProcessorGroupStatus(api, id, STOPPED);
                        //清空dmz组的所有connection
                        List<String> connectionIdList1 = queryAllConnectionIdsByGroupId(api, id);
                        for (String connection : connectionIdList1) {
                            emptyQueue(api, connection);
                        }
                        deleteProcessorGroup(api, id);
                    }
                }
                //删除data_info表中内容
                dataService.deleteByEncode(encode);
            }
            return "delete success!";
        } else {
            return "groupId error!";
        }
    }

    /**
     * 更新组的状态
     *
     * @param map1
     * @return
     */
    @PostMapping("updateProcessorGroupState")
    public String updateProcessorGroupState(@RequestBody Map map1) {
        String groupId = map1.get("groupId").toString();
        String state = map1.get("state").toString();
        List<String> list = queryAllEncodeList(api, groupId);
        String status = null;
        Integer invalidCount = 0;
        if (list != null && list.size() > 0) {
            status = updateProcessorGroupStatus(api, groupId, state);
            if (RUNNING.equalsIgnoreCase(state)) {
                JSONObject jsonObject = queryProcessGroup(api, groupId);
                invalidCount = (Integer) jsonObject.get("invalidCount");
            }
        }
        if (invalidCount > 0) {
            return "failed to start, invalid configuration exists!";
        } else {
            return status;
        }
    }

    /**
     * 更新频次及队列优先级
     *
     * @param map1
     * @return
     */
    @PostMapping("updateProcessorFrequency")
    public String updateProcessorFrequency(@RequestBody Map map1) {
        String groupId = map1.get("groupId").toString();
        String frequency = map1.get("frequency").toString();
        String prioritizers = map1.get("prioritizers").toString();
        Map<String, String> stringMap = (Map<String, String>) map1.get("map");
        return updateProcessorFre(api, groupId, prioritizers, frequency, stringMap);
    }

    @GetMapping("selectProcessorGroupByGroupId")
    public JSONObject selectProcessorGroupByGroupId(@RequestParam("groupId") String groupId) {
        return queryProcessGroup(api, groupId);
    }

    /**
     * 修改频次
     * @param api
     * @param groupId
     * @param frequency
     * @return
     */
    public String updateProcessorFre(String api, String groupId, String frequency) {
        Map<String, String> processorMap = findProcessorByGroupId(api, groupId);
        processorMap.forEach((key, value) -> {
            if (key.contains("FTP") || key.contains("File") || key.contains("AMQP")) {
                if (!"FetchFTP".equals(key) && !key.contains("FetchFile")) {
                    updateProcessorFrequency(api, value, frequency);
                }
            }
        });
        return "modification frequency succeeded!";
    }

    /**
     * 修改频次及队列优先级
     * @param api
     * @param groupId
     * @param prioritizers
     * @param frequency
     * @param stringMap
     * @return
     */
    public String updateProcessorFre(String api, String groupId, String prioritizers, String frequency, Map<String, String> stringMap) {
        Map<String, String> processorMap = findProcessorByGroupId(api, groupId);
        updateProcessorGroupStatus(api, groupId, STOPPED);
        String parentGroupId = queryParentProcessorGroupIdByGroupId(api, groupId);
        Map<String, String> connectionMap = queryMapConnectionByGroupId(api, parentGroupId);
        String connectionId = connectionMap.get(groupId);
        //修改connection的优先级
        if (connectionId != null) {
            updateConnectionByIdAndPrioritizer(api, connectionId, prioritizers);
        }
        //添加到全局变量中
        createVariable(api, groupId, stringMap);
        //此处内容为李犇新增-----------（修改组名和资料名称）-------
        String stringMapName = stringMap.get("name") == null ? "" : stringMap.get("name");
        String stringMapEncode = stringMap.get("encode") == null ? "" : stringMap.get("encode");
        if (!"".equals(stringMapName) && !"".equals(stringMapEncode)) {
            updateProcessorGroup(api, groupId, stringMapEncode, stringMapName);
        }
        //---------------------------------------------------
        processorMap.forEach((key, value) -> {
            if (key.contains("FTP") || key.contains("File") || key.contains("AMQP")) {
                if (!"FetchFTP".equals(key) && !key.contains("FetchFile")) {
                    updateProcessorFrequency(api, value, frequency);
                    Map<String, String> variable = queryMapVariableByGroupId(api, groupId);
                    String password = variable.get("password");
                    //密码被清空了需要重新设置
                    Map<String, String> paramMap = new HashMap<>();
                    paramMap.put("password", password);
                    if (key.contains("ConsumeAMQP")) {
                        String queue = variable.get("queue");
                        paramMap.put("queue", queue);
                    }
                    //paramMap.put("password", "${password}");
                    updateProcessor(api, value, paramMap, key);
                    updateProcessorGroupStatus(api, groupId, RUNNING);
                }
            }
        });
        return "modification frequency succeeded!";
    }

    /**
     * 模版实例化
     */
    public JSONObject createTemplateInstance(String api, String nifiFlowId, String templateId) {
//        originX += 100;
        originY += 100;
        String json = HttpClientUtil.postJson(api + "/process-groups/" + nifiFlowId + "/template-instance",
                "{\n" +
                        "    \"templateId\":\"" + templateId + "\",\n" +
                        "    \"originX\":" + originX + ",\n" +
                        "    \"originY\":" + originY + ",\n" +
                        "    \"disconnectedNodeAcknowledged\":false\n" +
                        "}");
        return JSONObject.parseObject(json);
    }

    /**
     * 根据encode和dataType查询出组id
     *
     * @param encode
     * @param dataType
     * @return
     */
    public String queryGroupIdByEncode(String encode, String dataType) {
        Map<String, String> groupMap = queryAllProcessorGroupMapByGroupId(api, nifiFlowId);
        String nifiId = groupMap.get(dataType);
        Map<String, String> map = queryAllProcessorGroupMapByGroupId(api, nifiId);
        return map.get(encode);
    }

    /**
     * 创建分发
     * @param map1
     * @param templates
     * @return
     */
    public JSONObject createDist(Map map1, Map<String, String> templates) {
        String encode = map1.get("encode").toString();
        String name = map1.get("name") == null ? "" : map1.get("name").toString();
        String type = map1.get("type").toString();
        String s = encode.substring(encode.lastIndexOf("_") + 1);
        Map<String, String> encodeMap = queryAllEncodeMapList(api, s);
        String sourceId = encodeMap.get(s);
        String parentGroupId = queryParentProcessorGroupIdByGroupId(api, sourceId);
        JSONObject template = createTemplateInstance(api, parentGroupId, templates.get(type));
        String groupId = getGroupIdByTemplate(template);
        updateProcessorGroup(api, groupId, encode, name);
        JSONObject processGroup = queryProcessGroup(api, sourceId);
        JSONObject position = (JSONObject) processGroup.get("position");
        BigDecimal x = (BigDecimal) position.get("x");
        BigDecimal xb = x.add(BigDecimal.valueOf(450));
        BigDecimal y = (BigDecimal) position.get("y");

        //如果源头采集已经有分发了，则新创建的分发再y+100，防止叠在一起
        Map<String, String> connectionMap = queryMapConnectionByGroupId(api, parentGroupId);
        if (connectionMap.get(sourceId) != null) {
            BigDecimal yb = y.add(BigDecimal.valueOf(100));
            //修改分发的xy坐标
            updateProcessorGroupXY(api, groupId, xb, yb);
            //采集永远和最新创建的分发在同一水平线上
            updateProcessorGroupXY(api, sourceId, x, yb);
        } else {
            //修改组的xy坐标
            updateProcessorGroupXY(api, groupId, xb, y);
        }
        createGroupConnections(api, parentGroupId, sourceId, groupId);
        return template;
    }

    /**
     * 查询所有模版
     *
     * @return
     */

    public JSONArray queryAllTemplates(String api) {
        String json = HttpClientUtil.getMap(api + "/flow/templates");
        JSONObject jsonObject = JSONObject.parseObject(json);
        return (JSONArray) jsonObject.get("templates");
    }

    /**
     * 查询所有模版返回name和id的map
     *
     * @return
     */
    public Map<String, String> queryMapTemplates(String api) {
        Map<String, String> map = new HashMap<>();
        JSONArray jsonArray = queryAllTemplates(api);
        for (Object o : jsonArray) {
            JSONObject object = (JSONObject) o;
            JSONObject template = (JSONObject) object.get("template");
            String name = template.get("name").toString();
            String id = template.get("id").toString();
            map.put(name, id);
        }
        return map;
    }


    /**
     * 改变组的状态
     *
     * @param groupId
     * @param state
     * @return
     */
    public String updateProcessorGroupStatus(String api, String groupId, String state) {
        String json = HttpClientUtil.putJson(api + "/flow/process-groups/" + groupId,
                "{\"id\":\"" + groupId + "\",\"state\":\"" + state + "\"," +
                        "\"disconnectedNodeAcknowledged\":false}");
        JSONObject jsonObject = JSONObject.parseObject(json);
        return jsonObject.get("state").toString();
    }

    /**
     * 组新增全局参数
     *
     * @param groupId
     * @param paramMap
     */
    public void createVariable(String api, String groupId, Map<String, String> paramMap) {
        JSONObject jsonObject = queryAllVariable(api, groupId);
        JSONObject processGroupRevision = (JSONObject) jsonObject.get("processGroupRevision");
        JSONObject object = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        paramMap.forEach((key, value) -> {
            JSONObject variable = new JSONObject();
            variable.put("name", key);
            variable.put("value", value);
            JSONObject object1 = new JSONObject();
            object1.put("variable", variable);
            jsonArray.add(object1);
        });
        object.put("processGroupId", groupId);
        object.put("variables", jsonArray);
        HttpClientUtil.postJson(api + "/process-groups/" + groupId + "/variable-registry/update-requests",
                "{\"processGroupRevision\":" + processGroupRevision.toJSONString() + "," +
                        "\"disconnectedNodeAcknowledged\":false,\"variableRegistry\": " + object.toJSONString() + "}");
//        System.out.println(json);
    }

    /**
     * 根据组id查询出所有的全局变量
     *
     * @param groupId
     * @return
     */
    public JSONObject queryAllVariable(String api, String groupId) {
        String json = HttpClientUtil.getMap(api + "/process-groups/" + groupId + "/variable-registry");
        return JSONObject.parseObject(json);
    }

    /**
     * 查询出全局变量的map
     *
     * @param api
     * @param groupId
     * @return
     */
    public Map<String, String> queryMapVariableByGroupId(String api, String groupId) {
        Map<String, String> resultMap = new HashMap<>();
        JSONObject jsonObject = queryAllVariable(api, groupId);
        JSONObject variableRegistry = (JSONObject) jsonObject.get("variableRegistry");
        JSONArray variables = (JSONArray) variableRegistry.get("variables");
        for (Object o : variables) {
            JSONObject object = (JSONObject) o;
            JSONObject variable = (JSONObject) object.get("variable");
            String name = variable.get("name").toString();
            String value = variable.get("value").toString();
            resultMap.put(name, value);
        }
        return resultMap;
    }


    /**
     * 通过源组id和目标组id建立连接
     *
     * @param sourceGroupId
     * @param destGroupId
     */
    public void createGroupConnections(String api, String nifiFlowId, String sourceGroupId, String destGroupId) {
        JSONObject source = (JSONObject) queryAllOutputsByGroupId(api, sourceGroupId).get(0);
        String sourceId = source.get("id").toString();
        JSONObject dest = (JSONObject) queryAllInputsByGroupId(api, destGroupId).get(0);
        String descId = dest.get("id").toString();
        HttpClientUtil.postJson(api + "/process-groups/" + nifiFlowId + "/connections",
                "{\"revision\":{\"version\":0},\"disconnectedNodeAcknowledged\":false," +
                        "\"component\":{\"name\":\"\",\"source\":{\"id\":\"" + sourceId + "\",\"groupId\":\"" + sourceGroupId + "\"," +
                        "\"type\":\"OUTPUT_PORT\"},\"destination\":{\"id\":\"" + descId + "\",\"groupId\":\"" + destGroupId + "\"," +
                        "\"type\":\"INPUT_PORT\"},\"flowFileExpiration\":\"0 sec\",\"backPressureDataSizeThreshold\":\"1 GB\",\"backPressureObjectThreshold\":\"10000\"," +
                        "\"bends\":[],\"prioritizers\":[],\"loadBalanceStrategy\":\"DO_NOT_LOAD_BALANCE\",\"loadBalancePartitionAttribute\":\"\"," +
                        "\"loadBalanceCompression\":\"DO_NOT_COMPRESS\"}}");
//        System.out.println(json);
    }

    /**
     * 根据processorGroupId修改组名
     *
     * @param processorGroupId
     * @param name
     */
    public void updateProcessorGroup(String api, String processorGroupId, String encode, String name) {
        JSONObject jsonObject = queryProcessGroup(api, processorGroupId);
        JSONObject revision = (JSONObject) jsonObject.get("revision");
        HttpClientUtil.putJson(api + "/process-groups/" + processorGroupId,
                "{\"revision\":" + revision.toJSONString() + "," +
                        "\"disconnectedNodeAcknowledged\":false," +
                        "\"component\":{\"id\":\"" + processorGroupId + "\"," +
                        "\"name\":\"" + encode + "\",\"comments\":\"" + name + "\"}}");
//        System.out.println(json);
    }

    /**
     * 修改组的xy坐标
     *
     * @param processorGroupId
     * @param x
     * @param y
     */
    public void updateProcessorGroupXY(String api, String processorGroupId, BigDecimal x, BigDecimal y) {
        JSONObject jsonObject = queryProcessGroup(api, processorGroupId);
        JSONObject revision = (JSONObject) jsonObject.get("revision");
        HttpClientUtil.putJson(api + "/process-groups/" + processorGroupId,
                "{\"revision\":" + revision.toJSONString() + "," +
                        "\"disconnectedNodeAcknowledged\":false,\"component\":{\"id\":\"" + processorGroupId + "\"," +
                        "\"position\":{\"x\":" + x + ",\"y\":" + y + "}}}");
//        System.out.println(json);
    }

    /**
     * 根据组id删除此组
     *
     * @param groupId
     */
    public void deleteProcessorGroup(String api, String groupId) {
        JSONObject jsonObject = queryProcessGroup(api, groupId);
        JSONObject revision = (JSONObject) jsonObject.get("revision");
        String clientId;
        String version = revision.get("version").toString();
        String param = api + "/process-groups/" + groupId + "?version=" + version
                + "&disconnectedNodeAcknowledged=false";
        if (revision.get("clientId") != null) {
            clientId = revision.get("clientId").toString();
            param += "&clientId" + clientId;
        }
        HttpClientUtil.deleteJson(param);
//        System.out.println(json);
    }

    /**
     * 完全终止处理器清空线程
     * @param api
     * @param processId
     */
    public void terminationProcessor(String api, String processId) {
        HttpClientUtil.deleteJson(api + "/processors/" + processId + "/threads");
    }


    /**
     * 根据processor组id查询组信息
     *
     * @param processorGroupId
     * @return
     */
    public JSONObject queryProcessGroup(String api, String processorGroupId) {
//        String processorGroupId = "c937cda3-de5e-3c9e-161c-6d8b53eb76d4";
        String json = HttpClientUtil.getMap(api + "/process-groups/" + processorGroupId);
        return JSONObject.parseObject(json);
    }

    /**
     * 根据组id查询下面的所有组
     *
     * @param groupId
     * @return
     */
    public JSONArray queryAllProcessorGroupByGroupId(String api, String groupId) {
        String json = HttpClientUtil.getMap(api + "/process-groups/" + groupId + "/process-groups");
        if (json == null || "".equals(json)) {
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(json);
        return (JSONArray) jsonObject.get("processGroups");
    }

    /**
     * 根据grouopId获取父级组id
     *
     * @param api
     * @param groupId
     * @return
     */
    public String queryParentProcessorGroupIdByGroupId(String api, String groupId) {
        String json = HttpClientUtil.getMap(api + "/process-groups/" + groupId);
        JSONObject jsonObject = JSONObject.parseObject(json);
        JSONObject component = (JSONObject) jsonObject.get("component");
        return component.get("parentGroupId").toString();
    }

    /**
     * 查询出所有包含encode的list
     *
     * @param api
     * @param encode
     * @return
     */
    public List<String> queryAllEncodeList(String api, String encode) {
        List<String> list = new ArrayList<>();
        JSONArray jsonArray = queryAllEncodeObjectList(api, encode);
        for (Object o : jsonArray) {
            JSONObject object = (JSONObject) o;
            String name = object.get("name").toString();
            list.add(name);
        }
        return list;
    }

    /**
     * 根据encode查询所有相关内容
     * @param api
     * @param encode
     * @return
     */
    public JSONArray queryAllEncodeObjectList(String api, String encode) {
        if (encode == null || "".equals(encode)) {
            return null;
        }
        String json = HttpClientUtil.getMap(api + "/flow/search-results?q=" + encode);
        if ("".equals(json)) {
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(json);
        JSONObject searchResultsDTO = (JSONObject) jsonObject.get("searchResultsDTO");
        return (JSONArray) searchResultsDTO.get("processGroupResults");
    }

    public Map<String, String> queryAllEncodeMapList(String api, String encode) {
        Map<String, String> map = new HashMap<>();
        String json = HttpClientUtil.getMap(api + "/flow/search-results?q=" + encode);
        JSONObject jsonObject = JSONObject.parseObject(json);
        JSONObject searchResultsDTO = (JSONObject) jsonObject.get("searchResultsDTO");
        JSONArray processGroupResults = (JSONArray) searchResultsDTO.get("processGroupResults");
        for (Object o : processGroupResults) {
            JSONObject object = (JSONObject) o;
            String name = object.get("name").toString();
            String id = object.get("id").toString();
            map.put(name, id);
        }
        return map;
    }

    /**
     * 根据组id查询下面的所有name和id的map
     *
     * @param groupId
     * @return
     */
    public Map<String, String> queryAllProcessorGroupMapByGroupId(String api, String groupId) {
        Map<String, String> map = new HashMap<>();
        JSONArray jsonArray = queryAllProcessorGroupByGroupId(api, groupId);
        for (Object o : jsonArray) {
            JSONObject object = (JSONObject) o;
            JSONObject component = (JSONObject) object.get("component");
            String name = component.get("name").toString();
            String id = component.get("id").toString();
            map.put(name, id);
        }
        return map;
    }

    /**
     * 根据groupId查询出下面的所有processor
     *
     * @param processorGroupId
     * @return
     */
    public JSONArray queryAllProcessorByGroupId(String api, String processorGroupId) {
        String json = HttpClientUtil.getMap(api + "/process-groups/" + processorGroupId + "/processors");
//        System.out.println(json);
        JSONObject jsonObject = JSONObject.parseObject(json);
        String processors = jsonObject.get("processors").toString();
        return JSONArray.parseArray(processors);
    }

    /**
     * 根据groupId获取下面的所有input-ports
     *
     * @param groupId
     * @return
     */
    public JSONArray queryAllInputsByGroupId(String api, String groupId) {
        String json = HttpClientUtil.getMap(api + "/process-groups/" + groupId + "/input-ports");
        JSONObject jsonObject = JSONObject.parseObject(json);
        return (JSONArray) jsonObject.get("inputPorts");
    }

    /**
     * 根据groupId获取下面的所有output-ports
     *
     * @param groupId
     * @return
     */
    public JSONArray queryAllOutputsByGroupId(String api, String groupId) {
        String json = HttpClientUtil.getMap(api + "/process-groups/" + groupId + "/output-ports");
        JSONObject jsonObject = JSONObject.parseObject(json);
        return (JSONArray) jsonObject.get("outputPorts");
    }

    /**
     * 根据groupId查询出name及id的map
     *
     * @param groupId
     * @return
     */
    public Map<String, String> findProcessorByGroupId(String api, String groupId) {
        Map<String, String> resultMap = new HashMap<>();
//        String groupId = "c937cda3-de5e-3c9e-161c-6d8b53eb76d4";
        JSONArray jsonArray = queryAllProcessorByGroupId(api, groupId);
//        System.out.println(jsonArray);
        for (Object json : jsonArray) {
            JSONObject jsonObject = (JSONObject) json;
            JSONObject component = (JSONObject) jsonObject.get("component");
            String name = component.get("name").toString();
            String id = component.get("id").toString();
            resultMap.put(name, id);
        }
        return resultMap;
    }


    /**
     * 根据processorId和条件修改
     *
     * @param processorId
     */
    public void updateProcessor(String api, String processorId, Map<String, String> paramMap, String type) {
        JSONObject jsonObject = queryProcessorById(api, processorId);
//        Map<String, Object> map = new HashMap<>();
        JSONObject object = new JSONObject();
        JSONObject revision = (JSONObject) jsonObject.get("revision");
        JSONObject component = (JSONObject) jsonObject.get("component");
        JSONObject config = (JSONObject) component.get("config");
        JSONObject properties = (JSONObject) config.get("properties");
        if (paramMap.get("filter") != null) {
            String filter = paramMap.get("filter");
            if (type.contains("File")) {
                if (!type.contains("Put") && !type.contains("Fetch")) {
                    properties.put("File Filter", filter);
                }
            } else if (type.contains("FTP")) {
                if (!type.contains("Put") && !type.contains("Fetch")) {
                    properties.put("File Filter Regex", filter);
                }
            }
        }
        if (paramMap.get("password") != null) {
            String password = paramMap.get("password");
            if (type.contains("FTP") || type.contains("AMQP")) {
                properties.put("Password", password);
            }
        }
        if (paramMap.get("dmzPassword") != null) {
            String password = paramMap.get("dmzPassword");
            if (type.contains("FTP")) {
                properties.put("Password", password);
            }
        }
        if (paramMap.get("queue") != null) {
            String queue = paramMap.get("queue");
            if (type.contains("AMQP")) {
                properties.put("Queue", queue);
            }
        }
        if (paramMap.get("recursion") != null) {
            String recursion = paramMap.get("recursion");
            if (type.contains("File")) {
                if (!"PutFile".equals(type)) {
                    properties.put("Recurse Subdirectories", recursion);
                }
            } else {
                if (!"FetchFTP".equals(type)) {
                    properties.put("Search Recursively", recursion);
                }
            }
        }
        if (paramMap.get("DBCPConnectionPool") != null) {
            String pool = paramMap.get("DBCPConnectionPool");
            if ("PutSQL".equals(type)) {
                properties.put("JDBC Connection Pool", sqlMap.get(pool));
            }
        }
        config.put("properties", properties);
        component.put("config", config);
        object.put("component", component);
        object.put("disconnectedNodeAcknowledged", false);
        object.put("revision", revision);
//        System.out.println(object);
        HttpClientUtil.putJson(api + "/processors/" + processorId,
                object.toJSONString());
//        System.out.println(json);
    }

    /**
     * 修改频次
     *
     * @param processorId
     * @param frequency
     */
    public void updateProcessorFrequency(String api, String processorId, String frequency) {
//        String clientId = getClientId(processorId);
        JSONObject jsonObject = queryProcessorById(api, processorId);
//        Map<String, Object> map = new HashMap<>();
        JSONObject object = new JSONObject();
        JSONObject revision = (JSONObject) jsonObject.get("revision");
        JSONObject component = (JSONObject) jsonObject.get("component");
        JSONObject config = (JSONObject) component.get("config");
        config.put("schedulingPeriod", frequency + " sec");
        component.put("config", config);
        object.put("component", component);
        object.put("disconnectedNodeAcknowledged", false);
        object.put("revision", revision);
//        System.out.println(object);
        HttpClientUtil.putJson(api + "/processors/" + processorId,
                object.toJSONString());
//        System.out.println(json);
    }

    /**
     * 根据processor的id查询出全部信息
     *
     * @param processorId
     * @return
     */
    public JSONObject queryProcessorById(String api, String processorId) {
        String json = HttpClientUtil.getMap(api + "/processors/" + processorId);
        return JSONObject.parseObject(json);
    }

    public Map<String, String> queryMapConnectionByGroupId(String api, String groupId) {
        Map<String, String> map = new HashMap<>();
        JSONArray jsonArray = queryAllConnectionsByGroupId(api, groupId);
        for (Object o : jsonArray) {
            JSONObject object = (JSONObject) o;
            JSONObject component = (JSONObject) object.get("component");
            String connectionId = component.get("id").toString();
            JSONObject source = (JSONObject) component.get("source");
            String sourceGroupId = source.get("groupId").toString();
            map.put(sourceGroupId, connectionId);
            JSONObject destination = (JSONObject) component.get("destination");
            String destGroupId = destination.get("groupId").toString();
            map.put(destGroupId, connectionId);
        }
        return map;
    }

    public List<String> queryConnectionListByGroupId(String api, String parentGroupId, String groupId) {
        List<String> list = new ArrayList<>();
        JSONArray jsonArray = queryAllConnectionsByGroupId(api, parentGroupId);
        for (Object o : jsonArray) {
            JSONObject object = (JSONObject) o;
            String sourceGroupId = object.get("sourceGroupId").toString();
            String connectionId = object.get("id").toString();
            if (sourceGroupId.equals(groupId)) {
                list.add(connectionId);
            }
        }
        return list;
    }

    /**
     * 获取最右侧采集的坐标
     * 因为分发为采集的x+400
     * @param api
     * @param groupId
     * @return
     */
    public static JSONObject getMaxXY(String api, String groupId) {
        Map<String, JSONObject> XYMap = queryAllGroupXYByGroupId(api, groupId);
        Map<BigDecimal, BigDecimal> xyMap = new HashMap<>();
        BigDecimal xMax = new BigDecimal("0.0");
        BigDecimal yMax = new BigDecimal("0.0");
        for (Map.Entry<String, JSONObject> entry : XYMap.entrySet()) {
            String key = entry.getKey();
            JSONObject value = entry.getValue();
            if (!key.contains("_")) {
                BigDecimal x = value.get("x") == null ? new BigDecimal("0.0")
                        : (BigDecimal) value.get("x");
//                xMax = xMax.max(x);
                BigDecimal y = value.get("y") == null ? new BigDecimal("0.0")
                        : (BigDecimal) value.get("y");
                //如果横坐标相同就取最大的纵坐标
                if (xMax.equals(x)) {
                    yMax = yMax.max(y);
                    xyMap.put(x, yMax);
                } else {
                    xMax = xMax.max(x);
                    xyMap.put(xMax, y);
                }
                //坐标定位
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("x", xMax);
        jsonObject.put("y", xyMap.get(xMax));
        return jsonObject;
    }


    /**
     * 根据组id查询出下面所有组的xy坐标
     * @param api
     * @param groupId
     * @return
     */
    public static Map<String, JSONObject> queryAllGroupXYByGroupId(String api, String groupId) {
        Map<String, JSONObject> map = new HashMap<>();
//        http://10.225.1.24:7777/nifi-api/flow/process-groups/36203869-800d-13b0-0000-0000513a401e
        String json = HttpClientUtil.getMap(api + "/flow/process-groups/" + groupId);
        JSONObject jsonObject = JSONObject.parseObject(json);
        JSONObject processGroupFlow = (JSONObject) jsonObject.get("processGroupFlow");
        JSONObject flow = (JSONObject) processGroupFlow.get("flow");
        JSONArray jsonArray = (JSONArray) flow.get("processGroups");
        for (Object o : jsonArray) {
            JSONObject object = (JSONObject) o;
            JSONObject component = (JSONObject) object.get("component");
            JSONObject position = (JSONObject) component.get("position");
            String name = component.get("name").toString();
            map.put(name, position);
        }
        return map;
    }

    /**
     * 根据groupId查询下面所有connections
     *
     * @param groupId
     * @return
     */
    public JSONArray queryAllConnectionsByGroupId(String api, String groupId) {
        String json = HttpClientUtil.getMap(api + "/process-groups/" + groupId + "/connections");
        JSONObject jsonObject = JSONObject.parseObject(json);
        return (JSONArray) jsonObject.get("connections");
    }

    /**
     * 根据组id查询下面所有的connectionId
     *
     * @param api
     * @param groupId
     * @return
     */
    public List<String> queryAllConnectionIdsByGroupId(String api, String groupId) {
        List<String> connectionIdList = new ArrayList<>();
        JSONArray connections = queryAllConnectionsByGroupId(api, groupId);
        for (Object connection : connections) {
            JSONObject object = (JSONObject) connection;
            String id = object.get("id").toString();
            connectionIdList.add(id);
        }
        return connectionIdList;
    }

    /**
     * 根据connectionId查询
     *
     * @param connectionId
     * @return
     */
    public JSONObject queryConnectionById(String api, String connectionId) {
        String json = HttpClientUtil.getMap(api + "/connections/" + connectionId);
        return JSONObject.parseObject(json);
    }

    /**
     * 根据connectionId修改优先级
     *
     * @param connectionId
     */
    public void updateConnectionByIdAndPrioritizer(String api, String connectionId, String prioritizers) {
        String pre = "org.apache.nifi.prioritizer.";
        String json = HttpClientUtil.getMap(api + "/connections/" + connectionId);
        JSONObject jsonObject = JSONObject.parseObject(json);
        JSONObject revision = (JSONObject) jsonObject.get("revision");
        JSONObject component = (JSONObject) jsonObject.get("component");
        List<String> list = new ArrayList<>();
        list.add(pre + prioritizers);
        component.put("prioritizers", list);
        HttpClientUtil.putJson(api + "/connections/" + connectionId,
                "{\"revision\":" + revision.toJSONString() + "," +
                        "\"disconnectedNodeAcknowledged\":false," +
                        "\"component\":" + component.toJSONString() + "}");
//        System.out.println(putJson);
    }


    /**
     * 根据connectionId删除此连接
     *
     * @param connectionId
     */
    public String deleteConnectionById(String api, String connectionId) {
        JSONObject jsonObject = queryConnectionById(api, connectionId);
        //暂停sourceGroup
        String sourceGroupId = jsonObject.get("sourceGroupId").toString();
        String destinationGroupId = jsonObject.get("destinationGroupId").toString();
        updateProcessorGroupStatus(api, sourceGroupId, STOPPED);
        //重复关一次当前处理器
        updateProcessorGroupStatus(api, destinationGroupId, STOPPED);
        //清空connection
        emptyQueue(api, connectionId);
        JSONObject revision = (JSONObject) jsonObject.get("revision");
//        System.out.println(revision.toJSONString());
        String clientId = null;
        String version = revision.get("version").toString();
        String param = api + "/connections/" + connectionId + "?version=" + version
                + "&disconnectedNodeAcknowledged=false";
        if (revision.get("clientId") != null) {
            clientId = revision.get("clientId").toString();
            param += "&clientId" + clientId;
        }
        HttpClientUtil.deleteJson(param);
        //删除此connection后再重启source组
//        updateProcessorGroupStatus(api, sourceGroupId, RUNNING);
        return sourceGroupId;
//        System.out.println(json);
    }

    /**
     * 清空connection的数据
     *
     * @param connectionId
     */
    public void emptyQueue(String api, String connectionId) {
        HttpClientUtil.postJson(api + "/flowfile-queues/" + connectionId + "/drop-requests", "");
//        System.out.println(json);
    }

    /**
     * 新增type
     *
     * @param type
     */
    public void addProcessor(String nifiFlowId, String type) {
        originY += 100;
         HttpClientUtil.postJson(api + "/process-groups/" + nifiFlowId + "/processors",
                "{\"revision\":{\"version\":0}," +
                        "\"disconnectedNodeAcknowledged\":false,\"component\":{\"type\":" +
                        "\"org.apache.nifi.processors.standard." + type + "\",\"bundle\":{\"group\":\"org.apache.nifi\"," +
                        "\"artifact\":\"nifi-standard-nar\",\"version\":\"1.9.0\"},\"name\":\"" + type + "\"," +
                        "\"position\":{\"x\":" + originX + ",\"y\":" + originY + "}}}");
//        System.out.println(json);
    }

}
