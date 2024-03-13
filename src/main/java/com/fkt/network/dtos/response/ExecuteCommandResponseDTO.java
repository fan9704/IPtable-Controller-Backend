package com.fkt.network.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteCommandResponseDTO {
    private String response;
    private String message;
    private Boolean status;
}
