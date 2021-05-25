package constants;

public enum SoapAction {
    CHECK_TEXT("checkText", "CheckTextRequest"),
    CHECK_TEXTS("checkTexts", "CheckTextsRequest");
    String method;
    String reqName;
    public String getMethod(){return method;}
    public String getReqName(){return reqName;}

    private SoapAction(String action, String reqName) {
        this.method = action;
        this.reqName = reqName;
    }
}
