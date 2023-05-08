import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;
import java.util.function.Function;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class AutoConnectWifi {

    HashMap<String,String>dataMap = new HashMap<>();
    String userNumber;
    String passwd;
    String operator;
    String router;
    String host;
    String portalUrl;
    String school;


    public static void main(String[] args) {
        AutoConnectWifi autoConnectWifi = new AutoConnectWifi();
        autoConnectWifi.autoConnect();
    }
    public void autoConnect() {

        System.out.println("hello world,软件开始运行...并非卡死，请勿终止");
        initConfig();
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            router = "http://" + localHost.getHostAddress().split("\\.")[0] + ".0.0.1";
        } catch (UnknownHostException e) {
            System.out.println("没有获取到网络适配器，请检查wifi或者网线是否连接");
        }
        while (true){
            System.out.println("检测网络连接（50秒检测一次）。。。。，");
            //如果没网就进行连接
            if (!isNodeReachable("www.bing.com")){
                System.out.println("网络未认证，尝试认证。。。");
                getHost();
                searchSchool();
                loginWifi();
            }
            //50秒后再执行
            try {
                Thread.sleep(50000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }

    }

    /**
     *
     * @param hostname 测试网址
     * @return true 网络正常 false 网络不正常
     * 测试是否能访问该地址，判断网络是否正常
     * 感觉这个方法也不是很好用
     */
    public  final boolean isNodeReachable(String hostname) {
        try {
            InetAddress address = InetAddress.getByName(hostname);
             if (!address.isReachable(1000)){
                 return isPingIPReachable(hostname);
             }else {
                 return true;
             }
        } catch (IOException e) {
            // Handle the exception
            return false;
        }
    }

    /**
     *
     * @param hostname 网址
     * @return 是否正常上网
     * 同时同时检测网络
     */
    private static final boolean isPingIPReachable(String hostname) {
        try {
            return 0 == Runtime.getRuntime().exec("ping -c 1 " + hostname).waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 初始化用户参数
     * 获取配置文件信息
     */
    public  void initConfig(){
        Properties properties = new Properties();
        // 获取配置文件的输入流
        InputStream inputStream = AutoConnectWifi.class.getResourceAsStream("authentication.conf");
        try {
            // 加载配置文件
            properties.load(inputStream);
            // 通过键获取值
            userNumber = properties.getProperty("username");
            passwd = properties.getProperty("password");
            operator = properties.getProperty("operator");
            if(userNumber == null || userNumber.length() <= 1){
                while (!inputConfig());
            }
            // 打印输出
            System.out.println("已获取username: " + userNumber);
            System.out.println("已获取password: " + passwd);
            System.out.println("已获取operator:" + operator);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 输入配置信息
     * @return 配置失败
     */
    public boolean inputConfig(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入校园网帐号");
        userNumber = scanner.next();
        System.out.println("请输入校园网密码");
        passwd = scanner.next();
        System.out.println("请输入运营商：1：移动，2：联通，3：电信，输入其他重新输入");
        operator = ((Function<Integer, String>) (Integer i) -> {
            if (i == 1) {return "yd";
            } else if (i == 2) {
                return "lt";
            } else if (i == 3) {
                return "dx";
            } return null;})
                .apply(scanner.nextInt());
        outputConfig();
        if(operator == null){
            return false;
        }else {
            return true;
        }

    }

    /**
     * 输出手动输入的配置到文件
     */
    public void outputConfig(){
        Preferences prefs = Preferences.userRoot().node("myApp");
        prefs.put("username", userNumber);
        prefs.put("password",passwd);
        prefs.put("operator",operator);
        File iniFile = new File("authentication.conf");
        try {
            prefs.exportNode(new FileOutputStream(iniFile));
        } catch (IOException e) {
            System.out.println("保存配置失败");
        } catch (BackingStoreException e) {
            System.out.println("保存配置失败");
        }
    }

    /**
     * 获取接口地址
     */
    public  void getHost () {
        try {
            //创建URL对象
            URL  url=  new URL(router);
            //打开连接
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            //设置请求方法为GET
            con.setRequestMethod("GET");
            //获取响应码
            int responseCode = con.getResponseCode();
            System.out.println("Response Code : " + responseCode);
            //获取响应输入流
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            //读取并打印响应内容
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            host = response.substring(response.indexOf("http"),response.indexOf("/portal"));
            portalUrl = response.substring(response.indexOf("http"),response.indexOf("0.0.1") + 5);
            String url2 = response.substring(response.indexOf("http"), response.indexOf("\")"));
            //创建一个URI对象
            URI uri = new URI(url2);
            //调用getQuery()方法得到query字符串
            String query = uri.getQuery();
            String[]entries = query.split("&");
            for(String s : entries){
                String[]popData = s.split("=");
                dataMap.put(popData[0],popData[1]);
            }
            System.out.println(dataMap);
        } catch (Exception e) {
            System.out.println("接口异常");
        }
    }
    /**
     * 获取学校代码，用于userid参数拼接
     */
    public void  searchSchool(){
        try {
            URL url = new URL(portalUrl);
            //打开连接
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            //设置请求方法为GET

            //获取响应码
            int responseCode = con.getResponseCode();
            System.out.println("Response Code : " + responseCode);
            //获取响应输入流
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            //读取并打印响应内容
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println("读取登陆页面信息:"+response);
            school = response.substring(response.indexOf("\"radio\" name=\"operator\" id=\"") + 28,response.indexOf("yd\" value=\""));
        }catch (Exception e){
            System.out.println("请求接口异常");
        }

    }
    /**
     * 参数获取完毕，拼接参数请求/quickauth接口
     * 登陆wifi
     */
    public void loginWifi(){
        StringBuilder loginUrlBuilder = new StringBuilder(host + "/quickauth.do?");
        loginUrlBuilder.append("userid=").append(userNumber).append("@").append(school).append(operator);
        loginUrlBuilder.append("&passwd=").append(passwd);
        loginUrlBuilder.append("&wlanuserip=").append(dataMap.get("wlanuserip"));
        loginUrlBuilder.append("&wlanacname=").append(dataMap.get("wlanacname"));
        loginUrlBuilder.append("&vlan=").append(dataMap.get("vlan"));
        loginUrlBuilder.append("&mac=").append(dataMap.get("mac"));

        try {
            URL url = new URL(loginUrlBuilder.toString());
            //打开连接
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            //设置请求方法为GET
            //获取响应码
            int responseCode = con.getResponseCode();
            System.out.println("Response Code : " + responseCode);
            //获取响应输入流
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            //读取并打印响应内容
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println("登陆信息:"+response);
        }catch (Exception e){
            System.out.println("请求接口异常");
        }



    }
}