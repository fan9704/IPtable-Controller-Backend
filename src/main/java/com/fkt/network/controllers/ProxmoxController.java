package com.fkt.network.controllers;

import com.fkt.network.services.ProxmoxService;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Proxmox")
@RestController
@RequestMapping("/api/pve")
public class ProxmoxController {
    private ProxmoxService service;
    @Autowired
    public ProxmoxController(ProxmoxService service){
        this.service = service;
    }

    @Operation(summary = "Get all nodes")
    @GetMapping("/nodes")
    public ResponseEntity<List<Node>> getNodes() throws ProxMoxVEException {
        return this.service.getNodes();
    }
    @Operation(summary = "Get all storages")
    @GetMapping("/storages")
    public ResponseEntity<List<Storage>> getStorages() throws ProxMoxVEException {
        return this.service.getStorages();
    }

    @Operation(summary = "Get all Pools")
    @GetMapping("/pools")
    public ResponseEntity<List<Pool>> getPools() throws ProxMoxVEException {
        return this.service.getPools();
    }

    // Node API
    @Operation(summary = "Get node LXCs")
    @GetMapping("/nodes/{node}/lxc")
    public ResponseEntity<List<LXC>> getNodeLXCs(@PathVariable("node") String node) throws ProxMoxVEException {
        return this.service.getNodeLXCs(node);
    }
    @Operation(summary = "Get node VMs")
    @GetMapping("/nodes/{node}/qemu")
    public ResponseEntity<List<QemuVM>> getNodeQEMUs(@PathVariable("node") String node) throws ProxMoxVEException {
        return this.service.getNodeQEMUs(node);
    }
    @Operation(summary = "Get node Storages")
    @GetMapping("/nodes/{node}/storages")
    public ResponseEntity<List<Storage>> getNodeStorages(@PathVariable("node") String node) throws ProxMoxVEException {
        return this.service.getNodeStorages(node);
    }
    @Operation(summary = "Get node Firewall Rules")
    @GetMapping("/nodes/{node}/firewall-rules")
    public ResponseEntity<List<FirewallRule>> getNodeFirewallRules(@PathVariable("node") String node) throws ProxMoxVEException {
        return this.service.getNodeFirewallRules(node);
    }
    // Cluster API
    @Operation(summary = "Get Cluster SDNZones")
    @GetMapping("/cluster/sdnzones")
    public ResponseEntity<List<SDNZone>> getClusterSDNZones() throws ProxMoxVEException {
        return this.service.getClusterSDNZones();
    }
    @Operation(summary = "Get Cluster Firewall Groups")
    @GetMapping("/cluster/firewall-groups")
    public ResponseEntity<List<FirewallGroup>> getClusterFirewallGroups() throws ProxMoxVEException {
        return this.service.getClusterFirewallGroups();
    }
    @Operation(summary = "Get Cluster Firewall Rules")
    @GetMapping("/cluster/firewall-rules")
    public ResponseEntity<List<FirewallRule>> getClusterFirewallRules() throws ProxMoxVEException {
        return this.service.getClusterFirewallRules();
    }
    @Operation(summary = "Get Cluster Firewall IP Sets")
    @GetMapping("/cluster/firewall-ip-sets")
    public ResponseEntity<List<FirewallIPSet>> getClusterFirewallIPSets() throws ProxMoxVEException {
        return this.service.getClusterFirewallIPSets();
    }
    @Operation(summary = "Get Cluster Resources")
    @GetMapping("/cluster/resources")
    public ResponseEntity<List<Resource>> getClusterResources() throws ProxMoxVEException {
        return this.service.getClusterResources();
    }
    @Operation(summary = "Get Cluster SDNVNets")
    @GetMapping("/cluster/sdnvnets")
    public ResponseEntity<List<SDNVNet>> getClusterSDNVNets() throws ProxMoxVEException {
        return this.service.getClusterSDNVNets();
    }
}
