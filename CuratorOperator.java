package rui.zookeep;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * @program: bootstudy
 * @author: yanrui
 * @description
 * @create: 2019-11-21 16:51
 */
//实例化zk客户端
public class CuratorOperator {
    public CuratorFramework client = null;
    public static final String zkServerPath = "localhost:2181";

    public CuratorOperator() {
        //curator链接zookeeper的策略，及重试次数间隔
        //namespace为命令空间
        ExponentialBackoffRetry retry = new ExponentialBackoffRetry(3, 5000);
        client =  CuratorFrameworkFactory.builder().connectString(zkServerPath)
                    .sessionTimeoutMs(10000).retryPolicy(retry)
                    .namespace("workspace").build();
        client.start();
    }


    public void closeZKClient() {
        if (client != null) {
            this.client.close();
        }
    }

    public static void main(String[] args) throws Exception {
        //实例化
        CuratorOperator curatorOperator = new CuratorOperator();

        System.out.println("连接中");

        //创建节点并赋值
        String nodePath = "/super/yanrui";
        byte[] bytes = "xxx".getBytes();
        //creatingParentsIfNeeded为递归创建路径
        curatorOperator.client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                .forPath(nodePath, bytes);

        //更新节点数据
        curatorOperator.client.setData().withVersion(0).forPath(nodePath, "yyy".getBytes());

        //删除节点
        //guaranteed删除失败则还会在后台继续删，deletingChildrenIfNeeded如果有子节点就删除
        curatorOperator.client.delete().guaranteed().deletingChildrenIfNeeded()
                .withVersion(0).forPath(nodePath);

        //读取节点数据
        Stat stat = new Stat();
        byte[] data = curatorOperator.client.getData().storingStatIn(stat).forPath(nodePath);
        System.out.println("节点" + nodePath + "的数据为：" + new String(data));
        System.out.println("版本号为：" + stat.getVersion());

        //查询子节点
        List<String> childNodes = curatorOperator.client.getChildren().forPath(nodePath);
        System.out.println("开始打印子节点：");
        for (String childNode : childNodes) {
            System.out.println(childNode);
        }

        //判断节点是否存在
        Stat statExist = curatorOperator.client.checkExists().forPath(nodePath);
        System.out.println(statExist);

        //watcher事件,usingWatcher表示只会监听一次，监听完毕后就销毁
        curatorOperator.client.getData().usingWatcher(
                (Watcher) watchedEvent -> System.out.println("触发watcher事件"))
                .forPath(nodePath);

        //监听数据节点变更并触发事件,一次注册多次监听
        //判断事件为增删改查
        NodeCache nodeCache = new NodeCache(curatorOperator.client, nodePath);
        //buildInitial初始化的时候获取node的值并缓存
        nodeCache.start(true);
        if (nodeCache.getCurrentData() != null) {
            System.out.println("节点初始化数据为：" + new String(nodeCache.getCurrentData().getData()));
        } else {
            System.out.println("节点初始化数据为空....");
        }
        nodeCache.getListenable().addListener(() -> {
            if (nodeCache.getCurrentData() != null) {
                String data1 = new String(nodeCache.getCurrentData().getData());
                System.out.println("节点路径：" + nodeCache.getCurrentData().getPath() + "数据：" + data1);
            }
        });


        Thread.sleep(3000);

        curatorOperator.closeZKClient();
        System.out.println("已关闭");

    }
}
