# IPTable-Backend

> Author: FKT

---

## 前言

這是一個適用於 Linux Server 上，目的於映射 Server 上的 VM/CT 甚至是其他服務層級的單元，一般想要存取 Server VLAN 底下的 Service 基本上會有幾種作法

1. SSH Tunnel
2. Proxy
3. Zero Trust Access
4. Self-defined iptables

**SSH Tunnel**
作者認為 SSH Tunnel 對於長期使用不占優勢，且會有 Tunnel 斷掉後需要重新建立的風險，對於維運來說過於麻煩。

**Proxy**
Proxy 的設定稍微麻煩一點，不! 其實是我懶

**Zero Trust Access**
通常是要搭配有網域，對於沒有網域的情境實在是無法做

最終作者選擇最後一項方式自訂義 iptables 並且搭配網頁來協助操作

## System Architecture

![](/doc/structure.png)

---

## Future Planning

![](/doc/future-structure.png)

### Description

在大致建構好本專案之後，會希望配合 Proxmox VE 的分散式叢集管理方式，透過本系統一同進行叢集的網路 NAT 管理，因此為達成會需要透過 AMQP 來進行 Spring Boot Service 之間的訊息傳遞，最後構建一個 Master 的 Service 來彙整叢集內的所有 NAT 內容。

---

## Docker Usage

### Packed to jar

```shell
mvn clean package
```

### Packed to Docker Image

```shell
docker build -t iptable_controller_backend .
```

### Environment Variables

```shell
# Application Config
PROFILE_ACTIVE=dev
# Database
MONGO_DB_HOST=127.0.0.1
MONGO_DB_USERNAME=test
MONGO_DB_PASSWORD=123456
MONGO_DB_DATABASE=network
MONGO_DB_PORT=27017
MONGO_EXPRESS_EXPORT_PORT=8081
MONGO_EXPRESS_USERNAME=test
MONGO_EXPRESS_PASSWORD=123456
```

### Run Docker Container

```shell
docker run -p 9990:9990  --network=host --link mongodb iptable_controller_backend
```


### Push to Docker Hub

```shell
docker push fan9704/iptable_controller_backend:tagname
```

---

## PS

### Get Host Network IP

**You Can Change Your Network Device ID replace vmbr0**

```shell=
echo `ifconfig  vmbr0 | head -n2 | grep inet | awk '{print$2}'`
```

### Host with Docker

```shell=
docker run --network=host --privileged --cap-add=NET_ADMIN -d fan9704/iptable_controller_backend  
# If need edit env please add -e MONGO_DB_HOST=XX.XX.XX.XX

```