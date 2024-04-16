package com.fkt.network.controllers;

import com.fkt.network.dtos.ExecuteCommandRequestDTO;
import com.fkt.network.dtos.NetworkRecordCreateDTO;
import com.fkt.network.dtos.request.NetworkRecordRequestDTO;
import com.fkt.network.models.NetworkRecord;
import com.fkt.network.services.NetworkRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Tag(name = "Network Record")
@RestController
@RequestMapping("/api")
public class NetworkRecordController {
    private NetworkRecordService service;
    @Autowired
    public NetworkRecordController(NetworkRecordService service){
        this.service =service;
    }

    @Operation(summary = "Hello World")
    @GetMapping("/hello")
    public ResponseEntity<String> hello(){
        return new ResponseEntity<>("hello-world!", HttpStatus.OK);
    }

    @Operation(summary = "Execute Command")
    @PostMapping("/command")
    public ResponseEntity<?> execute_command(@RequestBody ExecuteCommandRequestDTO dto){
        return this.service.execute_command(dto);
    }
    @Operation(summary = "List NAT rules from os")
    @GetMapping("/nat/iptables")
    public ResponseEntity<?> findAllIptablesRules(){
        return this.service.findAllIptablesRules();
    }

    @Operation(summary = "List All Network Record")
    @GetMapping("/nat/record")
    public ResponseEntity<List<NetworkRecord>> findAllNetworkRecord(){
        return this.service.findAllNetworkRecord();
    }

    @Operation(summary = "Find By Id Network Record")
    @GetMapping("/nat/record/{id}")
    public ResponseEntity<NetworkRecord> findById(@PathVariable("id") String id){
        return this.service.find_network_record_by_id(id);
    }
    @Operation(summary = "Patch By Id Network Record")
    @PatchMapping("/nat/record/{id}")
    public ResponseEntity<NetworkRecord> patchById(@PathVariable("id") String id ,@RequestBody NetworkRecordRequestDTO dto){
        return this.service.patch_network_record_by_id(id,dto);
    }
    @Operation(summary = "Create NAT Iptables Service")
    @PostMapping("/nat/record")
    public ResponseEntity<?> create_nat(@RequestBody NetworkRecordCreateDTO dto) throws IOException {
        return this.service.create_service(dto);
    }
    @Operation(summary = "Delete NAT Iptables Service")
    @DeleteMapping("/nat/record/{id}")
    public ResponseEntity<?> delete_nat(@PathVariable("id") String id) throws IOException {
        return this.service.delete_nat_service(id);
    }
}
