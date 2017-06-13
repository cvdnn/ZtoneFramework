package android.framework.exception;


public class InternalErrorException extends Exception{

	private static final long serialVersionUID=-7602780450333802613L;
	
	/**
	 * 内部错误
	 *
	 */
	public InternalErrorException(){
		super();
	}
	
	/**
	 * 内部错误
	 * 
	 * @param strErr
	 * 					异常信息
	 */
	public InternalErrorException(String strErr){
		super(strErr);
	}
}
