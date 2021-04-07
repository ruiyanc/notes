 ## docker+k8s

* docker管理
  * docker search keyword ->查找远程镜像仓库中的镜像  
    * -s可用于筛选
  * docker pull name:tag  ->下载镜像
    * tag参数指定镜像的标签
  * docker images -> 列出本地镜像
  * docker rmi image ...  -> 删除镜像
  * docker inspect  -> 查看镜像
* 部署docker项目
  * docker run -it --entrypoint bash openjdk:latest -->命令行进入容器
  * dockerfile编写
    * FROM -> 指定容器环境,ENTRYPOINT -> 运行命令
  * docker build -t demo:latest . -> 根据Dockerfile文件构建docker项目
  * docker run -it demo:latest --mysql.address=192.168.0.194 ->运行docker项目
* add k8s
    
