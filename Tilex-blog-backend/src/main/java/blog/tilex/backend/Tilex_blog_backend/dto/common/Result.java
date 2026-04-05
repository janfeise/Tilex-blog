package blog.tilex.backend.Tilex_blog_backend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified API Response DTO
 * Format: { code, data, message, timestamp }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    /**
     * Response code: 0 = success, non-zero = failure
     */
    private int code;

    /**
     * Response data
     */
    private T data;

    /**
     * Response message
     */
    private String message;

    /**
     * Response timestamp
     */
    private long timestamp;

    /**
     * Success response with data and message
     */
    public static <T> Result<T> ok(T data, String message) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.data = data;
        result.message = message;
        result.timestamp = System.currentTimeMillis();
        return result;
    }

    /**
     * Success response with data only
     */
    public static <T> Result<T> ok(T data) {
        return ok(data, "Success");
    }

    /**
     * Success response without data
     */
    public static <T> Result<T> ok() {
        return ok(null, "Success");
    }

    /**
     * Failure response with message and data
     */
    public static <T> Result<T> fail(T data, String message) {
        Result<T> result = new Result<>();
        result.code = 500;
        result.data = data;
        result.message = message;
        result.timestamp = System.currentTimeMillis();
        return result;
    }

    /**
     * Failure response with message only
     */
    public static <T> Result<T> fail(String message) {
        return fail(null, message);
    }

    /**
     * Failure response with custom code
     */
    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.data = null;
        result.message = message;
        result.timestamp = System.currentTimeMillis();
        return result;
    }

    /**
     * Failure response with custom code, data, and message
     */
    public static <T> Result<T> fail(int code, T data, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.data = data;
        result.message = message;
        result.timestamp = System.currentTimeMillis();
        return result;
    }
}
