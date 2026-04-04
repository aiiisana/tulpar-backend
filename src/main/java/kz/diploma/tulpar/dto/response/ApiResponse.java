package kz.diploma.tulpar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified API envelope for all responses.
 * Success: {"success":true,"data":{...},"error":null}
 * Failure: {"success":false,"data":null,"error":"message"}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String error;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .error(null)
                .build();
    }

    public static ApiResponse<Void> fail(String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .data(null)
                .error(message)
                .build();
    }
}
