package cn.kdream.utils;

/**
 * @Author Bxsheng
 * @BlogAddress www.kdream.cn
 * @CreateTime 2020-08-02
 * JDK 1.8
 */

public class UnifyJsonResult {
    /**响应业务状态**/
    private Integer status;
    /** 响应消息 **/
    private String msg;

    /** 响应中的数据**/
    private Object data;

    public UnifyJsonResult() {
    }


    public UnifyJsonResult(Integer status, String msg, Object data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public static UnifyJsonResult build(Integer status, String msg, Object data){
        return new UnifyJsonResult( status,  msg,  data);
    }
    
    public static UnifyJsonResult errorMsg(String msg){
        return new UnifyJsonResult(500,msg,null);
    }

    public static UnifyJsonResult errorTokenMsg(String msg){
        return new UnifyJsonResult(502,msg,null);
    }
    
    public static UnifyJsonResult errorException(String msg){
        return new UnifyJsonResult(555,msg,null);
    }

    public static UnifyJsonResult success(){
        return new UnifyJsonResult(null);
    }

    public static UnifyJsonResult success(Object o){
        return new UnifyJsonResult(o);
    }




    public static UnifyJsonResult errorMap(Object data){
        return new UnifyJsonResult(501,"error",data);
    }

    public UnifyJsonResult(Object data) {
        this.status = 200;
        this.msg ="success";
        this.data = data;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
