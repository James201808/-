package glai.com.cn.yiqing.scan;

public class AddressClass {
    //地址名称
    private static String AddressName="";
    //地址经纬度
    private static String Locator = "";

    public static String getAddressName() {
        return AddressName;
    }
    public static String getLocator() {
        return Locator;
    }
    public static void setAddressName(String name) {
        AddressClass.AddressName = name;
    }

    public static void setLocator(String locator) {
        AddressClass.Locator = locator;
    }
}
