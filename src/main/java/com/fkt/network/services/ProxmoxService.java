package com.fkt.network.services;

import com.lumaserv.proxmox.ve.ProxMoxVEClient;
import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.model.firewall.FirewallGroup;
import com.lumaserv.proxmox.ve.model.firewall.FirewallIPSet;
import com.lumaserv.proxmox.ve.model.firewall.FirewallRule;
import com.lumaserv.proxmox.ve.model.nodes.Node;
import com.lumaserv.proxmox.ve.model.nodes.lxc.LXC;
import com.lumaserv.proxmox.ve.model.nodes.qemu.QemuVM;
import com.lumaserv.proxmox.ve.model.pools.Pool;
import com.lumaserv.proxmox.ve.model.resource.Resource;
import com.lumaserv.proxmox.ve.model.sdn.SDNVNet;
import com.lumaserv.proxmox.ve.model.sdn.SDNZone;
import com.lumaserv.proxmox.ve.model.storage.Storage;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProxmoxService {
    @Value("${pve.host}")
    String host;
    @Value("${pve.username}")
    String username;
    @Value("${pve.password}")
    String password;
    ProxMoxVEClient client;
    // All API
    public ProxmoxService(){
    }
    @PostConstruct
    public void connect() throws ProxMoxVEException {
        this.client = new ProxMoxVEClient(host,"pam",username,password);
        this.client.debug();
    }
    // Client API
    public ResponseEntity<List<Node>> getNodes() throws ProxMoxVEException {
        this.connect();
        List<Node> nodeList = this.client.getNodes();
        return new ResponseEntity<>(nodeList, HttpStatus.OK);
    }
    public ResponseEntity<List<Storage>> getStorages() throws ProxMoxVEException{
        this.connect();
        return new ResponseEntity<>(this.client.getStorages(),HttpStatus.OK);
    }
    public ResponseEntity<List<Pool>> getPools() throws ProxMoxVEException{
        this.connect();
        return new ResponseEntity<>(this.client.getPools(),HttpStatus.OK);
    }
    // Node API
    public ResponseEntity<List<LXC>> getNodeLXCs(String node) throws ProxMoxVEException{
        this.connect();
        return new ResponseEntity<>(this.client.nodes(node).getLXCs(),HttpStatus.OK);
    }
    public ResponseEntity<List<QemuVM>> getNodeQEMUs(String node) throws ProxMoxVEException{
        this.connect();
        return new ResponseEntity<>(this.client.nodes(node).getQemuVMs(),HttpStatus.OK);
    }
    public ResponseEntity<List<Storage>> getNodeStorages(String node) throws ProxMoxVEException{
        this.connect();
        return new ResponseEntity<>(this.client.nodes(node).getStorages(),HttpStatus.OK);
    }
    public ResponseEntity<List<FirewallRule>> getNodeFirewallRules(String node) throws ProxMoxVEException{
        this.connect();
        return new ResponseEntity<>(this.client.nodes(node).getFirewallRules(),HttpStatus.OK);
    }
    // Cluster API
    public ResponseEntity<List<SDNZone>> getClusterSDNZones() throws ProxMoxVEException{
        this.connect();
        return new ResponseEntity<>(this.client.cluster().getSDNZones(),HttpStatus.OK);
    }
    public ResponseEntity<List<FirewallGroup>> getClusterFirewallGroups() throws ProxMoxVEException{
        this.connect();
        return new ResponseEntity<>(this.client.cluster().getFirewallGroups(),HttpStatus.OK);
    }
    public ResponseEntity<List<FirewallRule>> getClusterFirewallRules() throws ProxMoxVEException{
        this.connect();
        return new ResponseEntity<>(this.client.cluster().getFirewallRules(),HttpStatus.OK);
    }
    public ResponseEntity<List<FirewallIPSet>> getClusterFirewallIPSets() throws ProxMoxVEException{
        this.connect();
        return new ResponseEntity<>(this.client.cluster().getFirewallIPSets(),HttpStatus.OK);
    }
    public ResponseEntity<List<Resource>> getClusterResources() throws ProxMoxVEException{
        this.connect();
        return new ResponseEntity<>(this.client.cluster().getResources(),HttpStatus.OK);
    }
    public ResponseEntity<List<SDNVNet>> getClusterSDNVNets() throws ProxMoxVEException{
        this.connect();
        return new ResponseEntity<>(this.client.cluster().getSDNVNets(),HttpStatus.OK);
    }
    // QEMU VM API
}
