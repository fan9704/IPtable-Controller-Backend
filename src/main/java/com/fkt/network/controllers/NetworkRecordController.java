package com.fkt.network.controllers;

import com.fkt.network.dtos.ExecuteCommandRequestDTO;
import com.fkt.network.dtos.NetworkRecordCreateDTO;
import com.fkt.network.dtos.response.ExecuteCommandResponseDTO;
import com.fkt.network.models.NetworkRecord;
import com.fkt.network.services.NetworkRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Tag(name = "Network Record")
@RestController
@ConditionalOnExpression("${network.controller.enabled:true}")
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
    public ResponseEntity<ExecuteCommandResponseDTO> execute_command(@RequestBody ExecuteCommandRequestDTO dto){
        return this.service.executeCommand(dto);
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
        return this.service.findNetworkRecordById(id);
    }
    @Operation(summary = "Refresh All Network Record")
    @GetMapping("/nat/refresh/record")
    public ResponseEntity<List<NetworkRecord>> refreshAll() {
        return this.service.refreshAllNetworkRecord();
    }
    @Operation(summary = "Refresh By Id Network Record")
    @GetMapping("/nat/refresh/record/{id}")
    public ResponseEntity<NetworkRecord> refreshById(@PathVariable("id") String id) {
        return this.service.refreshNetworkRecordById(id);
    }
    @Operation(summary = "Patch By Id Network Record")
    @PatchMapping("/nat/record/{id}")
    public ResponseEntity<NetworkRecord> patchById(@PathVariable("id") String id ,@RequestBody NetworkRecordCreateDTO dto) throws IOException {
        return this.service.patchNetworkRecordByIdWithResponseEntity(id,dto);
    }
    @Operation(summary = "Create NAT Iptables Service")
    @PostMapping("/nat/record")
    public ResponseEntity<NetworkRecord> createNat(@RequestBody NetworkRecordCreateDTO dto) {
        return this.service.createWithResponseEntity(dto);
    }
    @Operation(summary = "Delete NAT Iptables Service")
    @DeleteMapping("/nat/record/{id}")
    public ResponseEntity<?> deleteNat(@PathVariable("id") String id) throws IOException {
        return this.service.deleteNetworkRecordByIdWithResponseEntity(id);
    }
}
