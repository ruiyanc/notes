### Netty
1. 核心组件
	*  Channel：传入或传出数据的载体。
	*  回调：回调被触发时，相关事件被ChannelHandler实现处理
	*  Future：ChannelFuture建立连接
		* ChannelFuture future = channel.connect(new InetSocketAddress("127.0.0.1", 25))
	*  	