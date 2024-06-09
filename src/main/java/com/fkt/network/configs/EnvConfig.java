package com.fkt.network.configs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnvConfig {

    // Proxmox Config
//    @Value("pve.host")
    private String pve_host;

//    @Value("pve.username")
    private String pve_username;

//    @Value("pve.password")
    private  String pve_password;

    // Server Config
//    @Value("host.ip")
    private String host_ip;
//    @Value("iptables.operate")
    private  String iptables_operate;

}
