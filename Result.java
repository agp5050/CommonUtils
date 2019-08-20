

@SuppressWarnings({"hiding","rawtypes"})
public class Result<T>{
	private int status;
	private int error;
	private String message;
	private T data;
	private static String SUCCESS_STR="success";
	// general exception  
	public static Result SUCCESS = new Result(200,"Success");  
	public static Result LOGIN_SUCCESS = new Result(200,"Login Success"); 
	public static Result LOGOUT_SUCCESS = new Result(200,"Logout Success"); 
	public static Result LOGIN_ERROR = new Result(530,530000,"用戶名或密码错误。");  
	public static Result UPLOAD_ERROR = new Result(500,500400,"上传文件失败。");  
	public static Result LOGOUT_ERROR = new Result(401,401000,"Login Error");  
    public static Result SERVER_EXCEPTION = new Result(1,500100,"Server internal error");  
    public static Result PARAMETER_ISNULL = new Result(1,500101,"No input parameter");  
    public static Result PARAMETER_INVALID = new Result(1,500106,"Parameter is invalid");
    // business exception
    public static Result USER_NOT_EXSIST = new Result(1,500102,"User not exist");   
    public static Result ONLINE_USER_OVER = new Result(1,500103,"Online users exceeds the maximum number");   
    public static Result SESSION_NOT_EXSIST =  new Result(1,500104,"No offline session");  
    public static Result NOT_FIND_DATA = new Result(1,500105,"Can not find data");  
    public static Result INSUFFICIENT_DATA = new Result(1,500107,"Insufficient data amount");
    public static Result INTERNAL_EXCEPTION = new Result(1,500108,"Internal Exception in pageable query.");
    public static Result INTERNAL_EXCEPTION(String reason){
    	return new Result(1,500108,reason);
	}


	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getError() {
		return error;
	}
	public void setError(int error) {
		this.error = error;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
	@Override
	public String toString() {
		return "ReturnObject [status=" + status + ", error=" + error + ", message=" + message
				+ ", data=" + data + "]";
	}
	public Result(int status, String message) {
		super();
		this.status = status;
		this.message = message;
	}
	public Result(int status, String message, T data) {
		super();
		this.status = status;
		this.message = message;
		this.data = data;
	}
	public Result(int status, int error, String message) {
		super();
		this.status = status;
		this.error = error;
		this.message = message;
	}
	public Result() {
		super();
	}

	public static Result getSuccessInstance(){
		Result result=new Result();
		result.setStatus(200);
		result.setMessage(SUCCESS_STR);
		return result;
	}
	
}
