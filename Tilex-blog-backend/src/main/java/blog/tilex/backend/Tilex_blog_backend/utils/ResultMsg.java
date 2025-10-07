package blog.tilex.backend.Tilex_blog_backend.utils;


import lombok.Data;

import java.io.Serializable;

@Data
public class ResultMsg<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	private int status;
	private String message;
	private T data;  // 新增数据字段

	public ResultMsg() {
	}

	public ResultMsg(int status, String message, T data) {
		this.status = status;
		this.message = message;
		this.data = data;
	}

	public static ResultMsg<Void> error(String msg) {
		return error(500, msg);
	}

	public static ResultMsg error(int status, String message) {
		return new ResultMsg(status,message,null);
	}

	// 新增带数据的success方法
	public static <T> ResultMsg<T> success(T data) {
		return new ResultMsg<>(200, "成功", data);
	}
	public static ResultMsg<Void> success(String message) {
		return new ResultMsg<>(200, message, null);
	}

	public static ResultMsg success() {
		return success("成功");
	}

}
