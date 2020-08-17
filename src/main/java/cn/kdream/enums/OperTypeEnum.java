package cn.kdream.enums;

public enum OperTypeEnum {
    IGNORE(0, "忽略"),
    PASS(1, "通过");
    public final Integer type;
    public final String msg;

    OperTypeEnum(Integer type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public Integer getType(){
        return type;
    }
    public static String getMsgByType(Integer type){
        for (OperTypeEnum typeEnum : OperTypeEnum.values()){
            if (typeEnum.getType() == type){
                return typeEnum.msg;
            }
        }
        return null;
    }
}
