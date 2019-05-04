# IoT Edge > IoT hub > Stream Analytics > Azure Function > Azure Database for mysql

# 目录(Table of Contents)
* [Create and Config IoT Hub](#Create-and-Config-IoT-Hub)
* [Install and Config IoT Edge on a Device](#Install-and-Config-IoT-Edge-on-a-Device)
* [Create and Config mysql](#Create-and-Config-mysql)
* [Create and Config Azure Function](#Create-and-Config-Azure-Function)
* [Create and Config Stream Analytics](#Create-and-Config-Stream-Analytics)

本例基于一个客户的真实需求，客户应用主要由java开发的系统，数据库采用mysql。

# Create and Config IoT Hub
1. 具体步骤可以参考[https://docs.azure.cn/zh-cn/iot-hub/iot-hub-create-through-portal](https://docs.azure.cn/zh-cn/iot-hub/iot-hub-create-through-portal)。

创建IoT Hub的基本过程不再赘述，本例主要介绍如何添加一个edge device连接到IoT Hub中，并在部署一个module到这个edge device。
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/iothub1.jpg) 

2. 注册一个edge device，可以参考文档[https://docs.azure.cn/zh-cn/iot-edge/how-to-register-device-portal](https://docs.azure.cn/zh-cn/iot-edge/how-to-register-device-portal)。

添加一个edge设备：
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/iothub2.jpg)
设备ID自己起名字避免重复即可，然后保存，保存后密钥会自动生成：
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/iothub3.jpg)
完成后，可以看到edge设备列表中多了刚刚添加的设备ID：
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/iothub4.jpg)
点击该设备ID，即可看到默认的两个module：
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/iothub5.jpg)

3. 部署一个名字为tempSensor测试module，这个module会模拟温度、湿度等数据发送到IoT Hub。具体步骤可以参考文档[https://docs.azure.cn/zh-cn/iot-edge/how-to-deploy-modules-portal](https://docs.azure.cn/zh-cn/iot-edge/how-to-deploy-modules-portal)。

添加一个IoT Edge module，本例为tempSensor的名称，说明从官方地址**mcr.microsoft.com/azureiotedge-simulated-temperature-sensor:1.0**拉取,然后保存：
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/iotedge_module1.jpg)
看到部署模块中的名称，然后下一步：
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/iotedge_module2.jpg)
这儿是定义edge上多个module之间数据流向的，当部署多个的module时，可以根据module的具体功能和业务需要，很方便的在云端进行远程操作，控制module之间的数据流向，不必到具体设备端进行修改，**这是Azure IoT Edge很重要的一个功能**，这儿先知道这个功能，以后再单独介绍。这儿保持默认点击下一步即可：
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/iotedge_module3.jpg)
最后提交，Azure会根据这些配置信息，将向远端设备进行推送配置，使其在设备端生效，由设备端根据配置去真正拉取container image，然后启动运行：
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/iotedge_module4.jpg)

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
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/mysql1.jpg) 

# Create and Config Azure Function
因客户主要的开发采用的Java，因此在Azure Function的语言选择上为保持一致也将采用Java。

1. 首先通过Azure portal创建好Function APP  
登录portal后，选择**创建资源**，点击**函数应用**:
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/function1.jpg)

命名Function APP的名称，新建或选择已有的资源组，存储账户和应用服务计划（App service plan），本例全为新建的：
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/function2.jpg)

创建App service plan时，可以选在不同的SKU，本例是选了一个开发测试的定价层B1：
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/function3.jpg)

创建完App service plan，回到Function app页面，点击**创建**按钮：
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/function4.jpg)

整个Function app创建完成后，可以通过导航菜单查看刚刚创建的**fndemoapp**:
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/function5.jpg)

2. 上面在Azure portal的操作暂时告一段落。下面的步骤将会创建function和其代码逻辑，在这之前，需要先准备一些工具和依赖项，具体步骤，请参考文档：[https://docs.azure.cn/zh-cn/azure-functions/functions-create-first-java-maven](https://docs.azure.cn/zh-cn/azure-functions/functions-create-first-java-maven)。

3. 使用maven生成新的Function项目  
在windows cmd下执行以下代码：
```CMD
mvn archetype:generate ^
    -DarchetypeGroupId=com.microsoft.azure ^
    -DarchetypeArtifactId=azure-functions-archetype
```
下图示例中显示下载了Maven的Azure function插件新版本，高亮部分是需要在交互时提供的参数，其中的**appName**即是在Azure portal创建的Function app的名称，一定要一致，同时**appRegion**和**resourceGroup**也要和Azure portal创建时保持一致：
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/function6.jpg)

继续执行等待Function项目创建完成：
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/function7.jpg)

然后在项目所在目录，可以看到产生了一些目录和文件：
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/function8.jpg)

并且自动生成了一个**Function.java**文件，这个文件即为Function的逻辑代码：
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/function9.jpg)
# Create and Config Stream Analytics