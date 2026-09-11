package br.com.kutuar.seguranca.dtos;

import java.util.UUID;

public class ApproveUserDTO {
    private UUID userId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
