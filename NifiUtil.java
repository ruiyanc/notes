package com.kafka.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author xurui
 * @description TODO
 * @date 2022/5/18 09:42
 */

public class NifiUtil {

    private static final String STOPPED = "STOPPED";
    private static final String RUNNING = "RUNNING";
    private static final String DO_NOT_LOAD_BALANCE = "DO_NOT_LOAD_BALANCE";
    private static final String ROUND_ROBIN = "ROUND_ROBIN";
    private static final String SINGLE_NODE = "SINGLE_NODE";

    public static Map<String, String> serviceMap = new HashMap<>();

    public static double originX = 0;
    public static double originY = 0;

    /**
     * 查询所有模版
     *
     * @return
     */
    public static JSONArray queryAllTemplates(String api) {
        String json = HttpClientUtil.getMap(api + "/flow/templates");
        JSONObject jsonObject = JSONObject.parseObject(json);
        return (JSONArray) jsonObject.get("templates");
    }

    /**
     * 模版实例化
     */
    public static JSONObject createTemplateInstance(String api, String nifiFlowId, String templateId) {
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
     * 创建模板时获取groupId
     * @param template
     * @return
     */
    public static String getGroupIdByTemplate(JSONObject template) {
        JSONObject flow = (JSONObject) template.get("flow");
        JSONArray processGroups = (JSONArray) flow.get("processGroups");
        JSONObject jsonObject = (JSONObject) processGroups.get(0);
        return jsonObject.get("id").toString();
    }

    /**
     * 查询所有模版返回name和id的map
     *
     * @return
     */
    public static Map<String, String> queryMapTemplates(String api) {
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
    public static String updateProcessorGroupStatus(String api, String groupId, String state) {
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
    public static void createVariable(String api, String groupId, Map<String, String> paramMap) {
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
    public static JSONObject queryAllVariable(String api, String groupId) {
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
    public static void updateProcessorGroup(String api, String processorGroupId, String encode, String name) {
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
    public static void updateProcessorGroupXY(String api, String processorGroupId, BigDecimal x, BigDecimal y) {
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
    public static void deleteProcessorGroup(String api, String groupId) {
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
     *
     * @param api
     * @param processId
     */
    public static void terminationProcessor(String api, String processId) {
        HttpClientUtil.deleteJson(api + "/processors/" + processId + "/threads");
    }

    /**
     * 删除processorGroup
     * @param api
     * @param groupId
     * @return
     */
    public static String delProcessorGroup(String api, String groupId) {
        //先暂停此组
        updateProcessorGroupStatus(api, groupId, "STOPPED");
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
        return "delete success!";
    }



    /**
     * 根据processor组id查询组信息
     *
     * @param processorGroupId
     * @return
     */
    public static JSONObject queryProcessGroup(String api, String processorGroupId) {
//        String processorGroupId = "c937cda3-de5e-3c9e-161c-6d8b53eb76d4";
        String json = HttpClientUtil.getMap(api + "/process-groups/" + processorGroupId);
        if (json == null || "The specified resource could not be found.".equals(json) || json.contains("not be found")) {
            return null;
        }
        return JSONObject.parseObject(json);
    }

    /**
     * 根据组id查询下面的所有组
     *
     * @param groupId
     * @return
     */
    public static JSONArray queryAllProcessorGroupByGroupId(String api, String groupId) {
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
    public static List<String> queryAllEncodeList(String api, String encode) {
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
     *
     * @param api
     * @param encode
     * @returnterminationProcessor
     */
    public static JSONArray queryAllEncodeObjectList(String api, String encode) {
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

    public static Map<String, String> queryAllEncodeMapList(String api, String encode) {
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
    public static JSONArray queryAllProcessorByGroupId(String api, String processorGroupId) {
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
    public static Map<String, String> findProcessorByGroupId(String api, String groupId) {
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
                properties.put("JDBC Connection Pool", serviceMap.get(pool));
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
    public static JSONObject queryProcessorById(String api, String processorId) {
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
     *
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
        jsonObject.put("y", xyMap.get(xMax) == null ? new BigDecimal("0.0") : xyMap.get(xMax));
        return jsonObject;
    }


    /**
     * 根据组id查询出下面所有组的xy坐标
     *
     * @param api
     * @param groupId
     * @return
     */
    public static Map<String, JSONObject> queryAllGroupXYByGroupId(String api, String groupId) {
        Map<String, JSONObject> map = new HashMap<>();
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
    public static JSONArray queryAllConnectionsByGroupId(String api, String groupId) {
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
    public static List<String> queryAllConnectionIdsByGroupId(String api, String groupId) {
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
    public static JSONObject queryConnectionById(String api, String connectionId) {
        String json = HttpClientUtil.getMap(api + "/connections/" + connectionId);
        return JSONObject.parseObject(json);
    }

    /**
     * 根据connectionId修改优先级
     *
     * @param connectionId
     */
    public void updateConnectionByIdAndPrioritizer(String api, String connectionId, String prioritizers) {
//        String json = HttpClientUtil.getMap(api + "/connections/" + connectionId);
//        JSONObject jsonObject = JSONObject.parseObject(json);
        JSONObject jsonObject = queryConnectionById(api, connectionId);
        String pre = "org.apache.nifi.prioritizer.";
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
    public static String deleteConnectionById(String api, String connectionId) {
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
        String clientId;
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
    public static void emptyQueue(String api, String connectionId) {
        HttpClientUtil.postJson(api + "/flowfile-queues/" + connectionId + "/drop-requests", "");
//        System.out.println(json);
    }

    /**
     * 修改connection的传输方式
     *
     * @param api
     * @param connectionId
     */
    public static void updateQueueRound(String api, String connectionId, String loadBalanceStrategy) {
//        http://10.225.1.41:7777/nifi-api/connections/003a30b6-3a64-151e-b72b-488ce38b9ba9
        JSONObject jsonObject = queryConnectionById(api, connectionId);
//        {"backPressureDataSizeThreshold":"1 GB","backPressureObjectThreshold":"10000","prioritizers":[],
//        "loadBalanceStrategy":"ROUND_ROBIN","loadBalancePartitionAttribute":""}}
        JSONObject revision = (JSONObject) jsonObject.get("revision");
        JSONObject component = (JSONObject) jsonObject.get("component");
        component.put("loadBalanceStrategy", loadBalanceStrategy);
        HttpClientUtil.putJson(api + "/connections/" + connectionId,
                "{\"revision\":" + revision.toJSONString() + "," +
                        "\"disconnectedNodeAcknowledged\":false," +
                        "\"component\":" + component.toJSONString() + "}");
    }

    /**
     * listFTP新增Distributed Cache Service
     * @param api
     * @param processorId
     */
    public static void updateListFTPCache(String api, String processorId, String cacheServiceId) {
//        {"component":{"id":"db43319a-91a8-1553-b161-595baf6ee7f2","name":"ListFTP",
//        "config":{"schedulingPeriod":"60 sec","executionNode":"PRIMARY","penaltyDuration":"30 sec",
//        "yieldDuration":"1 sec","bulletinLevel":"WARN","schedulingStrategy":"TIMER_DRIVEN","comments":"",
//        "autoTerminatedRelationships":[],"properties":{"Distributed Cache Service":"d1741109-0180-1000-ffff-ffff8a3faa80"}},
//        "state":"STOPPED"},"revision":{"clientId":"718630d2-f68c-189d-42db-67aa25ab7924","version":2},
//        "disconnectedNodeAcknowledged":false}
        JSONObject jsonObject = queryProcessorById(api, processorId);
//        Distributed Cache Service: "d1741109-0180-1000-ffff-ffff8a3faa80"
        JSONObject component = (JSONObject)jsonObject.get("component");
        JSONObject config = (JSONObject) component.get("config");
        JSONObject properties = (JSONObject) config.get("properties");
        properties.put("Distributed Cache Service", cacheServiceId);
        properties.put("et-state-cache", cacheServiceId);
        properties.put("Password", "${password}");
        config.put("properties", properties);
        component.put("config", config);
        JSONObject revision = (JSONObject)jsonObject.get("revision");
        HttpClientUtil.putJson(api + "/processors/" + processorId,
                "{\"revision\":" + revision.toJSONString() + "," +
                        "\"disconnectedNodeAcknowledged\":false," +
                        "\"component\":" + component.toJSONString() + "}");
    }

    public static void updateFTPPassword(String api, String processorId) {
//        {"component":{"id":"db43319a-91a8-1553-b161-595baf6ee7f2","name":"ListFTP",
//        "config":{"schedulingPeriod":"60 sec","executionNode":"PRIMARY","penaltyDuration":"30 sec",
//        "yieldDuration":"1 sec","bulletinLevel":"WARN","schedulingStrategy":"TIMER_DRIVEN","comments":"",
//        "autoTerminatedRelationships":[],"properties":{"Distributed Cache Service":"d1741109-0180-1000-ffff-ffff8a3faa80"}},
//        "state":"STOPPED"},"revision":{"clientId":"718630d2-f68c-189d-42db-67aa25ab7924","version":2},
//        "disconnectedNodeAcknowledged":false}
        JSONObject jsonObject = queryProcessorById(api, processorId);
//        Distributed Cache Service: "d1741109-0180-1000-ffff-ffff8a3faa80"
        JSONObject component = (JSONObject)jsonObject.get("component");
        JSONObject config = (JSONObject) component.get("config");
        JSONObject properties = (JSONObject) config.get("properties");
        properties.put("Password", "${password}");
        config.put("properties", properties);
        component.put("config", config);
        JSONObject revision = (JSONObject)jsonObject.get("revision");
        HttpClientUtil.putJson(api + "/processors/" + processorId,
                "{\"revision\":" + revision.toJSONString() + "," +
                        "\"disconnectedNodeAcknowledged\":false," +
                        "\"component\":" + component.toJSONString() + "}");
    }

    /**
     * listFile新增cache
     * @param api
     * @param processorId
     */
    public static void updateListFileCache(String api, String processorId, String cacheServiceId) {
        JSONObject jsonObject = queryProcessorById(api, processorId);
//        Distributed Cache Service: "d1741109-0180-1000-ffff-ffff8a3faa80"
        JSONObject component = (JSONObject)jsonObject.get("component");
        JSONObject config = (JSONObject) component.get("config");
        JSONObject properties = (JSONObject) config.get("properties");
        properties.put("Input Directory Location", "Remote");
        properties.put("et-state-cache", cacheServiceId);
        config.put("properties", properties);
        component.put("config", config);
        JSONObject revision = (JSONObject)jsonObject.get("revision");
        HttpClientUtil.putJson(api + "/processors/" + processorId,
                "{\"revision\":" + revision.toJSONString() + "," +
                        "\"disconnectedNodeAcknowledged\":false," +
                        "\"component\":" + component.toJSONString() + "}");
    }

}
