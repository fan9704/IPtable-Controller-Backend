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
