package project;

public class StatusType {

	private Integer code;
	private String message;
	private Integer count;
	
	public StatusType(Integer code, String message, Integer count) {
		this.code = code;
		this.message = message;
		this.count = count;
	}
	
	public Integer getCode(){
		return this.code;
	}
	
	public String getMessage(){
		return this.message;
	}
	
	public Integer getCount() {
		return this.count;
	}
	
	public void setCount(Integer count) {
		this.count = count;
	}
}
