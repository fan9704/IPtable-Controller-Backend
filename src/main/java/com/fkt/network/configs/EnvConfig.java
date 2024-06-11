package com.fkt.network.configs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnvConfig {
    // Proxmox Config
    private String pve_host;
    private String pve_username;
    private String pve_password;

    // Server Config
    private String host_ip;
    private String iptables_operate;
    private Boolean iptables_legacy;
}
