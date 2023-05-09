# wifi-auto-connect

#### 1. 介绍

河南科技学院校园网自动登陆（新乡的很多系统相似，可能也可以用？），java版。可以实现电脑，路由器，软路由的自动认证wifi,后续会上传docker版本的。
系统如下图。
![img.png](img%2Fimg.png)
#### 2.软件架构

**<font color = green>采用jdk8</font>**

**1.大致实现思想：**

抓包最后的请求接口，通过请求接口达到认证

最后发现`/quickauth.do`的get接口是最后实现的关键

**2.通过该get接口我发现可以直接登陆校园wifi**：

`[http://172.18.249.222:6060/quickauth.do?userid=20211554217@kjxyyd&passwd=Wang15238791265&wlanuserip=10.102.41.73&wlanacname=HIST-BRAS&wlanacIp=172.18.247.17&ssid=&vlan=32003272&mac=f4:ce:23:f9:9b:88&version=0&portalpageid=47×tamp=1682562683888&uuid=3c30a228-3e44-4dd7-8aee-ac1100bd2578&portaltype=0&hostname=&bindCtrlId=](http://172.18.249.222:6060/quickauth.do?userid=20211554217@kjxyyd&passwd=Wang15238791265&wlanuserip=10.102.41.73&wlanacname=HIST-BRAS&wlanacIp=172.18.247.17&ssid=&vlan=32003272&mac=f4:ce:23:f9:9b:88&version=0&portalpageid=47&timestamp=1682562683888&uuid=3c30a228-3e44-4dd7-8aee-ac1100bd2578&portaltype=0&hostname=&bindCtrlId=)`



3.**下面是我对该get接口的query参数的简单说明**

**下面是参数说明**，带“*”的是参数必须项：
```text
* uerid ： 校园网帐号+学院缩写+运营商

* passwd ： 校园网密码

* wlanuserip ： 被分配ip，ac所分配的ap,和mac同时作为入网证明。

* wlanacname ： wifi 名称，不能不填，用于区分教师和学生wifi

* vlan ： vlan

* mac ： ap mac地址  
  wlanacIp ： ac地址，经测试可以不填，猜测是valn和分配的地址直接限定了ac设备
  version ： 版本 应该没什么卵用
  portalpageid ： 暂时不知道是什么
  timestamp ： 毫秒时间戳
  uuid ： 登陆唯一识别id 我猜测随便编一个就成，没卵用
  portaltype ： 认证方式 我们学校好像就一种，没卵用
  hostname ： 暂时没什么用
  bindCtrlId ： 暂时没什么用
```
  
  <font color = green>我舍弃不必要的参数，获取一个较为精简的脚本，发现可以认证wifi。</font>

```sheel
  curl 'http://172.18.249.222:6060/quickauth.do?userid=20211554217@kjxyyd&passwd=Wang15238791265&wlanuserip=10.102.41.73&wlanacname=HIST-BRAS&vlan=32003272&mac=f4:ce:23:f9:9b:88'
```

当然这样写不具有通用性，按照这个思路我写了一个较为通用的java程序，类似我们学校的校园网应该都可以用的。。。吧？。

#### 安装教程

##### 1.源码版本（release版本使用类似）

1. **git源码（或者下载源码）**

```sheel
git clone https://gitee.com/chenbaifu/wifi-auto-connect.git
```

2. **切换至工程目录**

```sheel
cd wifi-auto-connect
```

3. **编译源码**

```sheel
javac AutoConnectWifi.java
```

4.**设置配置文件**

对工程里的`authentication.conf`进行校园网帐号，密码，运营商的配置

```ini
#校园网帐号
username=13678593474
#校园网密码
password=swa32323
#校园网运营商 移动为=yd,联通为lt,电信为dx.
operator=yd
```

5.运行

```sheel
java AutoConnectWifi
```

##### 2.docker安装
1.**建立配置文件(这里随意即可，不一定非得是home目录)**
```shell
mkdir /home/wifi-config/conf
cd /home/wifi-config/conf
vi authentication.conf
```
2.**编辑配置文件**
```shell
#校园网帐号
username=
#校园网密码
password=
#校园网运营商 移动为=yd,联通为lt,电信为dx.
operator=yd
```
wq保存退出即可。  

3.**启动容器**
```shell
sudo docker run -d --net=host -v /home/wifi-config/conf:/opt/conf --name=autowifi-demo chenbaifu/schoolwifi-auto
```
尽量用host网络，网桥模式可能被自己的路由拦截而请求。
#### 参与贡献

暂无
