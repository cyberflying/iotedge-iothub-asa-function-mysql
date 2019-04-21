# IoT Edge > IoT hub > Stream Analytics > Azure Function > Azure Database for mysql

# 目录(Table of Contents)
* [install and config iot edge](#IoT-Edge)
* [create and config IoT Hub](#IoT-Hub)
* [create and config mysql](#mysql)
* [create and config Azure Function](#Function)
* [create and config Stream Analytics](#ASA)





# IoT Edge
本例中采用windows 10企业版（一台工作使用的surface book2）做为IoT Edge的运行环境，运行linux container，作为开发测试环境是可以的，IoT Edge的生产环境的架构请参考[https://docs.microsoft.com/en-us/azure/iot-edge/support](https://docs.microsoft.com/en-us/azure/iot-edge/support#operating-systems)。

安装IoT Edge的步骤，请参考[https://docs.microsoft.com/en-us/azure/iot-edge/how-to-install-iot-edge-windows](https://docs.microsoft.com/en-us/azure/iot-edge/how-to-install-iot-edge-windows#all-installation-parameters)  
需要需要提前安装好docker for windows  
然后运行powershell脚本:
```PowerShell
. {Invoke-WebRequest -useb aka.ms/iotedge-win} | Invoke-Expression; `
Install-SecurityDaemon -Manual -ContainerOs Linux -DeviceConnectionString 'HostName=<your iothub hostname>;DeviceId=win10edge;SharedAccessKey=<your key>'
```
# IoT Hub

# mysql

# Function

# ASA