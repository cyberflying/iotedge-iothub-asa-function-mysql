# IoT Edge > IoT hub > Stream Analytics > Azure Function > Azure Database for mysql

# 目录(Table of Contents)
* [Create and Config IoT Hub](#Create-and-Config-IoT-Hub)
* [Install and Config IoT Edge on a Device](#Install-and-Config-IoT-Edge-on-a-Device)
* [Create and Config mysql](#Create-and-Config-mysql)
* [Create and Config Azure Function](#Create-and-Config-Azure-Function)
* [Create and Config Stream Analytics](#Create-and-Config-Stream-Analytics)



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
这儿是定义edge上多个module之间数据流向的，当部署不同的module时，可以根据module的具体功能和业务需要，很方便的在云端进行远程操作，不必到具体设备端进行修改，这是Azure IoT Edge很重要的一个功能，后面找时间再具体说明。这儿保持默认点击下一步即可：
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/iotedge_module3.jpg)
最后提交，Azure会根据这些配置信息，将向远端设备进行推送配置，使其在设备端生效，由设备端根据配置去真正拉取container image，然后启动运行：
![](https://github.com/cyberflying/iotedge-iothub-asa-function-mysql/blob/master/img/iotedge_module4.jpg)

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

# Create and Config Azure Function

# Create and Config Stream Analytics