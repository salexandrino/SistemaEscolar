package br.com.kutuar.seguranca.dtos;

public class RegisterResponseDTO {
    private String message;

    public RegisterResponseDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
