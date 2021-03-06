# IoT Edge > IoT hub > Stream Analytics > Azure Function > Azure Database for mysql

# Overview
* [Create and Config IoT Hub](#Create-and-Config-IoT-Hub)
* [Install and Config IoT Edge on a Device](#Install-and-Config-IoT-Edge-on-a-Device)
* [Create and Config mysql](#Create-and-Config-mysql)
* [Create and Config Azure Function](#Create-and-Config-Azure-Function)
* [Create and Config Stream Analytics](#Create-and-Config-Stream-Analytics)
* [Summary](#总结)

本例基于客户的一个真实场景，将设备信息通过IoT Hub上传到Azure，将数据处理后存储在Azure Database for mysql中。客户应用主要由Java开发的系统，数据库采用mysql。  
这个“经典”的架构如何快速以云原生的方式使用Azure中的PaaS服务，参照本文可以快速实现。

整体架构如下图所示：
![](/img/architecture.jpg)

# Create and Config IoT Hub
1. 具体步骤可以参考[https://docs.azure.cn/zh-cn/iot-hub/iot-hub-create-through-portal](https://docs.azure.cn/zh-cn/iot-hub/iot-hub-create-through-portal)。

创建IoT Hub的基本过程不再赘述，本例主要介绍如何添加一个edge device连接到IoT Hub中，并在部署一个module到这个edge device。
![](/img/iothub1.jpg) 

2. 注册一个edge device，可以参考文档[https://docs.azure.cn/zh-cn/iot-edge/how-to-register-device-portal](https://docs.azure.cn/zh-cn/iot-edge/how-to-register-device-portal)。

添加一个edge设备：
![](/img/iothub2.jpg)
设备ID自己起名字避免重复即可，然后保存，保存后密钥会自动生成：
![](/img/iothub3.jpg)
完成后，可以看到edge设备列表中多了刚刚添加的设备ID：
![](/img/iothub4.jpg)
点击该设备ID，即可看到默认的两个module：
![](/img/iothub5.jpg)

3. 部署一个名字为tempSensor测试module，这个module会模拟温度、湿度等数据发送到IoT Hub。具体步骤可以参考文档[https://docs.azure.cn/zh-cn/iot-edge/how-to-deploy-modules-portal](https://docs.azure.cn/zh-cn/iot-edge/how-to-deploy-modules-portal)。

添加一个IoT Edge module，本例为tempSensor的名称，说明从官方地址**mcr.microsoft.com/azureiotedge-simulated-temperature-sensor:1.0**拉取,然后保存：
![](/img/iotedge_module1.jpg)
看到部署模块中的名称，然后下一步：
![](/img/iotedge_module2.jpg)
这儿是定义edge上多个module之间数据流向的，当部署多个的module时，可以根据module的具体功能和业务需要，很方便的在云端进行远程操作，控制module之间的数据流向，不必到具体设备端进行修改，**这是Azure IoT Edge很重要的一个功能**，这儿先知道这个功能，以后再单独介绍。这儿保持默认点击下一步即可：
![](/img/iotedge_module3.jpg)
最后提交，Azure会根据这些配置信息，将向远端设备进行推送配置，使其在设备端生效，由设备端根据配置去真正拉取container image，然后启动运行：
![](/img/iotedge_module4.jpg)

至此，在Azure上对IoT Hub和IoT Edge的基本操作完成了，后续需要在设备端进行安装配置IoT Edge运行时。

# Install and Config IoT Edge on a Device
具体步骤可以参考[https://docs.microsoft.com/en-us/azure/iot-edge/how-to-install-iot-edge-windows](https://docs.microsoft.com/en-us/azure/iot-edge/how-to-install-iot-edge-windows)。  
本例中采用windows 10企业版（一台工作中使用的[surface book 2](https://www.microsoftstore.com.cn/c/surface)）做为运行IoT Edge的Host OS，运行windows container，IoT Edge支持的操作系统请参考[https://docs.microsoft.com/en-us/azure/iot-edge/support](https://docs.microsoft.com/en-us/azure/iot-edge/support#operating-systems)。

安装IoT Edge的步骤，请参考[https://docs.microsoft.com/en-us/azure/iot-edge/how-to-install-iot-edge-windows](https://docs.microsoft.com/en-us/azure/iot-edge/how-to-install-iot-edge-windows#all-installation-parameters)  
然后以`管理员`身份运行powershell脚本:
```PowerShell
. {Invoke-WebRequest -useb aka.ms/iotedge-win} | Invoke-Expression; `
Install-SecurityDaemon -Manual -ContainerOs Windows -DeviceConnectionString '<your iot edge device connect string>'
```
运行结果如下
```PowerShell
PS C:\windows\system32> . {Invoke-WebRequest -useb aka.ms/iotedge-win} | Invoke-Expression; `
Install-SecurityDaemon -Manual -ContainerOs Windows -DeviceConnectionString '<your iot edge device connect string>'


ModuleType Version    Name                                ExportedCommands                                                                         
---------- -------    ----                                ----------------                                                                         
Script     0.0        IotEdgeSecurityDaemon               {Install-SecurityDaemon, Uninstall-SecurityDaemon}                                       
The container host is on supported build version 17763.
Downloading Moby Engine...
Using Moby Engine from C:\Users\keya\AppData\Local\Temp\iotedge-moby-engine.zip
Downloading Moby CLI...
Using Moby CLI from C:\Users\keya\AppData\Local\Temp\iotedge-moby-cli.zip
Downloading IoT Edge security daemon...
Using IoT Edge security daemon from C:\Users\keya\AppData\Local\Temp\iotedged-windows.zip
Skipping VC Runtime installation because it is already installed.
Generating config.yaml...
Configured device for manual provisioning.
Configured device with hostname 'kevin-book2'.
Configured device with Moby Engine URL 'npipe://./pipe/iotedge_moby_engine' and network 'nat'.
Updated system PATH.
Added IoT Edge registry values.
Initialized the IoT Edge service.

This device is now provisioned with the IoT Edge runtime.
Check the status of the IoT Edge service with `Get-Service iotedge`
List running modules with `iotedge list`
Display logs from the last five minutes in chronological order with
    Get-WinEvent -ea SilentlyContinue -FilterHashtable @{ProviderName='iotedged';LogName='application';StartTime=[datetime]::Now.AddMinutes(-5)} |
    Select TimeCreated, Message |
    Sort-Object @{Expression='TimeCreated';Descending=$false} |
    Format-Table -AutoSize -Wrap
```

安装完成后，查看服务运行状态：
```PowerShell
PS C:\windows\system32> Get-Service iotedge

Status   Name               DisplayName                           
------   ----               -----------                           
Running  iotedge            iotedge  
```
然后Azure IoT Edge Security Daemon会启动，并拉取edgeAgent, edgeHub等module，如果配置其他模块也会到相应的container registry中去拉取下来，如本例中的tempSensor。  
等待下载完成后，可以通过以下命令查看，PowerShell和CMD都可以：
```CMD
C:\Users\keya>iotedge list
NAME             STATUS           DESCRIPTION      CONFIG
edgeHub          running          Up 25 minutes    mcr.microsoft.com/azureiotedge-hub:1.0
tempSensor       running          Up 25 minutes    mcr.microsoft.com/azureiotedge-simulated-temperature-sensor:1.0
edgeAgent        running          Up 26 minutes    mcr.microsoft.com/azureiotedge-agent:1.0

```

# Create and Config mysql
1. 具体步骤可以参考文档[https://docs.azure.cn/zh-cn/mysql/quickstart-create-mysql-server-database-using-azure-portal](https://docs.azure.cn/zh-cn/mysql/quickstart-create-mysql-server-database-using-azure-portal)。  

2. 本例为了测试方便在配置mysql的连接安全性时，以测试为目的，实际生成环境下，请慎重考虑。  
添加本机的ip地址，打开允许Azure中的其他服务访问，禁用SSL等，都是为了测试方便：
![](/img/mysql1.jpg) 

3. 连接到mysql实例，创建相应的数据库和表  
本例以mysql workbench连接到Azure Database for mysql，创建数据库iotdb和表iottab，仅作测试使用，具体结构以实际情况而定：
```SQL
create database iotdb;
use iotdb;
create table iottab (id serial PRIMARY KEY, envtemp VARCHAR(30), envhumity ARCHAR(30));
```
![](/img/mysql2.jpg) 

# Create and Config Azure Function
因客户主要的开发采用的Java，因此在Azure Function的语言选择上为保持一致也将采用Java。

## 首先通过Azure portal创建好Function APP  
登录portal后，选择**创建资源**，点击**函数应用**:
![](/img/function1.jpg)

命名Function APP的名称，新建或选择已有的资源组，存储账户和应用服务计划（App service plan），本例全为新建的：
![](/img/function2.jpg)

创建App service plan时，可以选在不同的SKU，本例是选了一个开发测试的定价层B1：
![](/img/function3.jpg)

创建完App service plan，回到Function app页面，点击**创建**按钮：
![](/img/function4.jpg)

整个Function app创建完成后，可以通过导航菜单查看刚刚创建的**fndemoapp**:
![](/img/function5.jpg)
可以看到Function app已经创建完成，但是具体function还没有，所以在**函数**菜单下是空的。  
**这儿的Function app相对于function来说，更像一个容器，app里面可以有很多的function。**

## 准备工具和依赖项
上面在Azure portal的操作暂时告一段落。下面的步骤将会创建function和其代码逻辑，在这之前，需要先准备一些工具和依赖项，具体步骤，请参考文档：[https://docs.azure.cn/zh-cn/azure-functions/functions-create-first-java-maven](https://docs.azure.cn/zh-cn/azure-functions/functions-create-first-java-maven)。

## 使用maven生成新的Function项目  
在windows cmd下执行以下代码：
```CMD
mvn archetype:generate ^
    -DarchetypeGroupId=com.microsoft.azure ^
    -DarchetypeArtifactId=azure-functions-archetype
```
下图示例中显示下载了Maven的Azure function插件新版本，高亮部分是需要在交互时提供的参数，其中的**appName**即是在Azure portal创建的Function app的名称，一定要一致，同时**appRegion**和**resourceGroup**也要和Azure portal创建时保持一致：
![](/img/function6.jpg)

继续执行等待Function项目创建完成：
![](/img/function7.jpg)

然后在项目所在目录，可以看到产生了一些目录和文件：
![](/img/function8.jpg)

并且自动生成了一个**Function.java**文件，这个文件即为Function的逻辑代码：
![](/img/function9.jpg)

## 完成function的逻辑代码和相关配置  
该function的主要功能是接受Stream Analytics传来的数据，然后写入到mysql中。本例作为测试的目的，仅在Stream Analytics中将IoT hub传来的数据，取其中的温度和湿度两个数值，然后output到function。

 1. 修改代码  
该function的名称是Fn2mysql, 具体代码参考本repo中的Fn2mysql.java文件。

 1. 修改pom.xml文件  
在代码中使用了json和mysql等对象，需要有相关的依赖，需要在项目的pom.xml文件中声明：
![](/img/function10.jpg)

 1. 在Function app配置中添加环境变量  
在代码中使用了mysql的连接字符串，是从Function App的应用程序设置中读取的环境变量：
![](/img/function11.jpg)

具体设置可以点击进入编辑页面，**New application setting**可以单个增加环境变量，**Advanced edit**可以以json的格式批量添加环境变量：
![](/img/function12.jpg)

## 在本地测试执行function

1. 打包测试function  
在function的目录下执行命令：
```CMD
mvn clean package
```
如下图所示：
![](/img/function13.jpg)
![](/img/function14.jpg)

2. 本地设置session级别的环境变量，方便本地测试  
因为在java代码中调用了环境变量获取mysql的连接信息，在运行function之前，先设置好，以便测试通过：
![](/img/function15.jpg)
 测试时，指定了mysql连接信息禁用ssl，因为连接字符串中含有`"&"`符号，在CMD中需要使用转义字符`"^"`来标识`"&"`符号。

3. 本地运行function：
```CMD
mvn azure-functions:run
```
![](/img/function16.jpg)

4. 验证测试结果  
本例以postman对本地运行的function进行测试：
![](/img/function17.jpg)

查看function本地运行状态，显示成功插入一行记录：
![](/img/function18.jpg)

查看mysql表中数据，显示已经存在：
![](/img/function19.jpg)

本地测试function成功，可以Ctrl+C结束本地运行。

## 在CMD中使用Azure CLI登录Azure账户
下面为将function发布到Azure中做准备，需要先登录Azure账户，使用Azure CLI进行登录，需要指定中国区Azure，按照提示操作即可：
```CMD
az cloud set -n AzureChinaCloud
az login
```
![](/img/function20.jpg)

## 部署function到Azure中
运行以下命令将function部署到Azure中：
```CMD
mvn azure-functions:deploy
```
![](/img/function21.jpg)
在Azure portal中查看function部署成功：
![](/img/function22.jpg)

## 测试Azure中部署的function
在Azure portal中进行测试function是否可以正常运行，点击function页面中的右侧边栏**测试**，选择**POST**方法，填入模拟的json数据，点击**运行**按钮：
![](/img/function23.jpg)
运行后在页面的输出和日志窗口中看到成功的状态信息：
![](/img/function24.jpg)
然后查看mysql中也成功插入一行数据:
![](/img/function25.jpg)

# Create and Config Stream Analytics
Azure Stream Analytics（后面简称为ASA）是Azure中可以实时处理流式数据的PaaS服务，有很多内置的函数可以处理很多复杂的逻辑，适用于普遍的流数据分析场景。ASA已类似SQL的查询语言表示，可以处理采用 CSV、JSON 和 Avro 数据格式的事件数据。
ASA的创建和使用，可以参考[https://docs.azure.cn/zh-cn/stream-analytics/stream-analytics-quick-create-portal](https://docs.azure.cn/zh-cn/stream-analytics/stream-analytics-quick-create-portal)，另外，对于刚才提到的各种流数据分析场景，ASA可以很方便的进行处理，具体示例请参考[https://docs.azure.cn/zh-cn/stream-analytics/stream-analytics-stream-analytics-query-patterns](https://docs.azure.cn/zh-cn/stream-analytics/stream-analytics-stream-analytics-query-patterns)。

本例中，需要在ASA中创建一个新的输出，类型为Azure Function，如图所示：
![](/img/asa1.jpg)

在输出的定义中，选择前面创建的Azure Function并保存：
![](/img/asa2.jpg)

在ASA的查询中，是真正的逻辑处理，会用到类似SQL的语言进行表示。本例中会从作为输入的IoT Hub中获取环境温度和湿度数据，然后输出给Azure Function进行处理：
![](/img/asa3.jpg)

配置完成后，启动ASA。查看IoT Edge tempSensor模块发出的数据，经过IoT Hub > Stream Analytics > Azure Function的处理，已经存入到Azure Database for mysql中了。
![](/img/asa4.jpg)
![](/img/asa5.jpg)

# 总结
本文主要介绍了Azure IoT相关的服务以及如何使用，在具体应用场景中，物联网只是采集数据的一种技术手段，应用的实际目的还是要对业务数据进行分析，发现更多业务洞察，因此后续的数据分析和数据可视化等服务尤为重要。可以参考Azure数据分析相关的[更多文档](https://docs.azure.cn/zh-cn/index#pivot=products&panel=analytics)。

文中提到的Azure IoT服务，是为了让使用者更方便、快捷的搭建IoT服务，而不需要更多地关注底层实现，这也是Azure对实现业务需求带来的好处。