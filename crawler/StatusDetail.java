package project;

public class StatusDetail {
	private Integer code;
	private String message;
	
	public StatusDetail(Integer code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public Integer getCode(){
		return this.code;
	}
	
	public String getMessage(){
		return this.message;
	}
	
}
