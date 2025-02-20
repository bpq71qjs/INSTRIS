import java.util.Arrays;
import java.util.List;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import com.google.gson.Gson;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.io.FileReader;
import java.io.BufferedReader;
import java.time.Instant;
import java.net.http.HttpClient.Redirect;
import org.json.simple.parser.JSONParser;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


class Program {
    private static String version = "BETA 1.0";

    public static void banner() throws Exception{
        String banner = """

        '####:'##::: ##::'######::'########:'########::'####::'######::
        . ##:: ###:: ##:'##... ##:... ##..:: ##.... ##:. ##::'##... ##:
        : ##:: ####: ##: ##:::..::::: ##:::: ##:::: ##:: ##:: ##:::..::
        : ##:: ## ## ##:. ######::::: ##:::: ########::: ##::. ######::
        : ##:: ##. ####::..... ##:::: ##:::: ##.. ##:::: ##:::..... ##:
        : ##:: ##:. ###:'##::: ##:::: ##:::: ##::. ##::: ##::'##::: ##:
        '####: ##::. ##:. ######::::: ##:::: ##:::. ##:'####:. ######::
        ....::..::::..:::......::::::..:::::..:::::..::....:::......:::
        
""";
        System.out.println(banner);
        System.out.println("Made By \"tetris._2189\"");
        System.out.println("Version: " + version);
    }
}

class Account {
    private String self_username, self_password, self_enc_password;

    private static HttpClient client;
    private static CookieManager cookieManager;
    private static HttpRequest.Builder requestBuilder;
    private static Gson gson = new Gson();

    //init Account
    public Account(String username, String password) {
        requestBuilder = HttpRequest.newBuilder()
        .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36")
        .header("X-Ig-App-Id", "936619743392459")
        .header("X-Asbd-Id", "129477")
        .header("x-requested-with", "XMLHttpRequest")
        .header("Referer", "https://www.instagram.com/")
        .header("Content-Type", "application/x-www-form-urlencoded");
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        cookieManager.getCookieStore().add(null, new HttpCookie("ig_cb", "2"));
        client = HttpClient.newBuilder()
        .followRedirects(Redirect.ALWAYS)
        .cookieHandler(cookieManager).build();

        this.self_username = username;
        this.self_password = password;
        long timestamp = Instant.now().getEpochSecond();
        this.self_enc_password = "#PWD_INSTAGRAM_BROWSER:0:" + timestamp + ":" + this.self_password; //create encrypted password
    }


    //scrape media info
    public MediaResponse.Item scrapeMedia(String mediaUrl) throws Exception{
        String url;
        if(mediaUrl.endsWith("/")){
            url = mediaUrl + "?__a=1&__d=dis";
        }else{
            url = mediaUrl + "/?__a=1&__d=dis";
        }
        HttpRequest request = requestBuilder
        .uri(URI.create(url))
        .header("Content-Type", "application/json")
        .GET()
        .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        MediaResponse.Item media = Arrays.asList(gson.fromJson(response.body(), MediaResponse.class).getItem()).get(0);
        return media;
    }

    //scrape User Info
    public UserResponse.User scrapeUser(String username) throws Exception{
        String url = String.format("https://i.instagram.com/api/v1/users/web_profile_info/?username=%s", username);
        
        HttpRequest request = requestBuilder
        .uri(URI.create(url))
        .GET()
        .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        UserResponse user_data = gson.fromJson(response.body(), UserResponse.class);
        UserResponse.User user = user_data.getData().getUser();

        return user;
    }

    //Get cookie info
    public HttpCookie getCookie(String name) throws Exception{
        List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
        for(HttpCookie cookie : cookies){
            if(cookie.getName().equals(name)){
                return cookie;
            }
        }
        return null;
    }

    //save headers/cookies to autologin later.
    @SuppressWarnings("unchecked")
    public void saveCredentials(HttpRequest request) throws Exception,IOException {
        String jsonString = "";

        //modify previous json file
        try{
            BufferedReader reader = new BufferedReader(new FileReader("./resources/account.json"));
            String str;
            while((str = reader.readLine()) != null){
                jsonString += str;
            }
            reader.close();
        } catch(Exception e){
            e.printStackTrace();
        }

        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = new JSONArray(); //create empty array

        //if jsonString is not empty, parse
        if (!jsonString.isEmpty()) 
            jsonArray = (JSONArray) jsonParser.parse(jsonString);

        int flag = 0;
        
        for(Object obj : jsonArray){
            JSONObject acc = (JSONObject) obj;
            JSONObject info = (JSONObject) acc.get("INFO");
            JSONObject headers = (JSONObject) acc.get("HEADERS");
            JSONObject cookies = (JSONObject) acc.get("COOKIES");
            if(info.get("username").equals(this.self_username)) {
                request.headers().map().forEach((key, values) -> {
                    for(String value : values){
                        headers.replace(key, value);
                    }
                });
                for(HttpCookie cookie : cookieManager.getCookieStore().getCookies()){
                    cookies.replace(cookie.getName(), cookie.getValue());
                }
                info.replace("password", this.self_password);
                flag = 1;
                break;
            }
        }
        if(flag == 0)
        {
            JSONObject acc = new JSONObject();
            JSONObject cred = new JSONObject();
    
            JSONObject cookiesjs = new JSONObject();
            for(HttpCookie cookie : cookieManager.getCookieStore().getCookies()){
                cookiesjs.put(cookie.getName(), cookie.getValue());
            }
            acc.put("COOKIES", cookiesjs);
    
            JSONObject headersjs = new JSONObject();
            //init headers
            request.headers().map().forEach((key, values) -> {
                for(String value : values){
                    headersjs.put(key, value);
                }
            });
            acc.put("HEADERS", headersjs);
    
            //init basic info
            cred.put("username", this.self_username);
            cred.put("password", this.self_password);
            
            acc.put("INFO", cred);

            jsonArray.add(acc);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("./resources/account.json"))){
            writer.write(jsonArray.toJSONString());
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void autoLogin() throws Exception {
        //read headers.txt file
        JSONParser parser = new JSONParser();
        BufferedReader reader = new BufferedReader(new FileReader("./resources/account.json"));
        String jsonString = "";
        String str;
        while((str = reader.readLine()) != null){
            jsonString += str;
        }
        reader.close();
        JSONArray jsonArray = new JSONArray(); //create empty array

        //if jsonString is not empty, parse
        if (!jsonString.isEmpty())  jsonArray = (JSONArray) parser.parse(jsonString);

        for(Object obj : jsonArray) {
            JSONObject acc = (JSONObject)obj;
            if (((JSONObject)acc.get("INFO")).get("username").equals(this.self_username)) {
                cookieManager.getCookieStore().removeAll();

                JSONObject headers = (JSONObject)acc.get("HEADERS");
                for (Object key : headers.keySet()) {
                    String value = (String) headers.get(key);
                    requestBuilder.setHeader((String) key, value);
                }
                JSONObject cookies = (JSONObject)acc.get("COOKIES");
                for (Object key : cookies.keySet()) {
                    String value = (String) cookies.get(key);
                    cookieManager.getCookieStore().add(null, (new HttpCookie((String) key, value)));
                }
                break;
            }
        }
        HttpRequest request = requestBuilder
        .uri(URI.create("https://www.instagram.com"))
        .GET()
        .build();

        request.headers().map().forEach((key, values) -> {
            for(String value : values){
                System.out.printf("%s : %s\n", key, value);
            }
        });
        for(HttpCookie cookie : cookieManager.getCookieStore().getCookies()){
            System.out.printf("%s : %s\n",cookie.getName(), cookie.getValue());
        }
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
    }

    //Login into Instagram Account
    public void login() throws Exception{
        String csrftoken = null;

        HttpRequest request = requestBuilder
        .uri(URI.create("https://www.instagram.com"))
        .GET()
        .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        csrftoken = getCookie("csrftoken").getValue();
        requestBuilder.header("X-CSRFToken", csrftoken);

        String loginData = String.format("username=%s&enc_password=%s", this.self_username, this.self_enc_password);
        request = requestBuilder
        .uri(URI.create("https://www.instagram.com/api/v1/web/accounts/login/ajax/"))
        .POST(HttpRequest.BodyPublishers.ofString(loginData))
        .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body() + "\n\n");

        saveCredentials(request);
    }
}

public class tetris2189 {
    public static void main(String[] args) throws Exception {
        Program.banner();
        Account account = new Account("_21892189", "**wlsgml81");
        //account.login();
        account.autoLogin();
    }
}
