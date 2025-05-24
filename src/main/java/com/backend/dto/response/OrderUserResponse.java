package com.backend.dto.response;

import lombok.Data;

@Data
public class OrderUserResponse {
    private String emailUser;
    private String phoneUser;
    private String addressUser;
    private String nameUser;
}
