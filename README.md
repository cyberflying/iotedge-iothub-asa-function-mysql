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

2. 注册一个edge device，可以参考文档[https://docs.azure.cn/zh-cn/iot-edge/how-to-register-device-portal](https://docs.azure.cn/zh-cn/iot-edge/how-to-register-device-portal)。

3. 部署一个名字为tempSensor测试module，这个module会模拟温度、湿度等数据发送到IoT Hub。具体步骤可以参考文档[https://docs.azure.cn/zh-cn/iot-edge/how-to-deploy-modules-portal](https://docs.azure.cn/zh-cn/iot-edge/how-to-deploy-modules-portal)。

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
然后Azure IoT Edge Security Daemon会启动，并拉取edgeAgent, edgeHub等module，如果配置其他模块也会到相应的container registry中去拉取下来，如本例中的tempSensor
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