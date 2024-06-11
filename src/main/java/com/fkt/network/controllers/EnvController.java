package com.fkt.network.controllers;

import com.fkt.network.configs.EnvConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Env")
@RestController
@RequestMapping("/api/environment")
public class EnvController {


    // Proxmox Config
    @Value("${pve.host}")
    String pve_host;

    @Value("${pve.username}")
    String pve_username;

    @Value("${pve.password}")
    String pve_password;

    // Server Config
    @Value("${host.ip}")
    String host_ip;
    @Value("${iptables.operate}")
    String iptables_operate;
    @Value("${iptables.legacy}")
    Boolean iptables_legacy;

    @Operation(summary = "List all Config")
    @GetMapping("")
    public ResponseEntity<EnvConfig> getEnvConfig(){
        EnvConfig envConfig = new EnvConfig();
        envConfig.setHost_ip(this.host_ip);
        envConfig.setIptables_operate(this.iptables_operate);
        envConfig.setIptables_legacy(this.iptables_legacy);
        envConfig.setPve_host(this.pve_host);
        envConfig.setPve_username(this.pve_username);
        return new ResponseEntity<>(envConfig,HttpStatus.OK);
    }
}
