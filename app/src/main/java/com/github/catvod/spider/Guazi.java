package com.github.catvod.spider;

import android.util.Base64;
import android.util.Log;

import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import com.github.catvod.net.OkResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Guazi extends Spider {

    private static final String TAG = "Guazi";
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private String[] hosts = {
            "https://apinew.uozvr.com",
            "https://api.w32z7vtd.com",
            "https://api.6a7nnf7.com",
            "https://api.umygrx3.com",
            "https://api.rmedphk.com"
    };
    private int hostIndex = 0;
    private String host = hosts[hostIndex];

    // AES 固定密钥
    private static final String AES_KEY = "OITxa5OqAYjhswxx";
    private static final String AES_IV = "rCMNwZASNBKZ8mXV";

    // RSA 公钥
    private static final String RSA_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDUM5+/y8sPsWkd1/RQS64X259EUwxFXFE5HlA65MqrxnPs0JqoSRojSDy5QhwvROlaD6TwRQHKMY2OAZ6SnQeUJsChTEFIR9qUkwrs3/MVUMxjsv6JS6Oe/juclyJGTgVmDhB55EafXsD0SQYVj/QXXsxR6ewR5E2kL52yAAD4yQIDAQAB";

    // RSA 私钥
    private static final String RSA_PRIVATE_KEY =
            "-----BEGIN RSA PRIVATE KEY-----\n" +
                    "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGAe6hKrWLi1zQmjTT1\n" +
                    "ozbE4QdFeJGNxubxld6GrFGximxfMsMB6BpJhpcTouAqywAFppiKetUBBbXwYsYU\n" +
                    "1wNr648XVmPmCMCy4rY8vdliFnbMUj086DU6Z+/oXBdWU3/b1G0DN3E9wULRSwcK\n" +
                    "ZT3wj/cCI1vsCm3gj2R5SqkA9Y0CAwEAAQKBgAJH+4CxV0/zBVcLiBCHvSANm0l7\n" +
                    "HetybTh/j2p0Y1sTXro4ALwAaCTUeqdBjWiLSo9lNwDHFyq8zX90+gNxa7c5EqcW\n" +
                    "V9FmlVXr8VhfBzcZo1nXeNdXFT7tQ2yah/odtdcx+vRMSGJd1t/5k5bDd9wAvYdI\n" +
                    "DblMAg+wiKKZ5KcdAkEA1cCakEN4NexkF5tHPRrR6XOY/XHfkqXxEhMqmNbB9U34\n" +
                    "saTJnLWIHC8IXys6Qmzz30TtzCjuOqKRRy+FMM4TdwJBAJQZFPjsGC+RqcG5UvVM\n" +
                    "iMPhnwe/bXEehShK86yJK/g/UiKrO87h3aEu5gcJqBygTq3BBBoH2md3pr/W+hUM\n" +
                    "WBsCQQChfhTIrdDinKi6lRxrdBnn0Ohjg2cwuqK5zzU9p/N+S9x7Ck8wUI53DKm8\n" +
                    "jUJE8WAG7WLj/oCOWEh+ic6NIwTdAkEAj0X8nhx6AXsgCYRql1klbqtVmL8+95KZ\n" +
                    "K7PnLWG/IfjQUy3pPGoSaZ7fdquG8bq8oyf5+dzjE/oTXcByS+6XRQJAP/5ciy1b\n" +
                    "L3NhUhsaOVy55MHXnPjdcTX0FaLi+ybXZIfIQ2P4rb19mVq1feMbCXhz+L1rG8oa\n" +
                    "t5lYKfpe8k83ZA==\n" +
                    "-----END RSA PRIVATE KEY-----";

    private static final String DEVICE_OLD_KEY = "aLFBMWpxBrIDAD1Si/KVvm41";

    private String deviceId;
    private String deviceKey;
    private String token = "";
    private String tokenId = "";
    private boolean registered = false;

    private Gson gson = new Gson();

    public Guazi() {
        Random random = new Random();
        this.deviceId = String.valueOf(864150060000000L + random.nextInt(10000));
        StringBuilder sb = new StringBuilder();
        String chars = "0123456789ABCDEF";
        for (int i = 0; i < 40; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        this.deviceKey = sb.toString();
        initToken();
    }

    private HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Lavf/57.83.100");
        headers.put("code", "GZ0369");
        headers.put("deviceId", deviceId);
        headers.put("lang", "zh_cn");
        headers.put("Cache-Control", "no-cache");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Version", "2604028");
        headers.put("PackageName", "com.ae06aebdbb.y286327f5a.ofe849883320260517");
        headers.put("Ver", "3.0.3.2");
        headers.put("api-ver", "3.0.3.2");
        headers.put("Referer", host);
        return headers;
    }

    // ========== 兼容低版本 API 的工具函数 ==========
    private static String getOrDefault(Map<String, Object> map, String key, String defaultValue) {
        if (map != null && map.containsKey(key) && map.get(key) != null) {
            return String.valueOf(map.get(key));
        }
        return defaultValue;
    }

    private static String joinStrings(String delimiter, List<String> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    // ========== 设备认证 ==========
    private void initToken() {
        try {
            if (!registered) {
                signUp();
            }
            refreshToken();
        } catch (Exception e) {
            Log.e(TAG, "initToken failed: " + e.getMessage());
            token = "024212ef0975c5306a1434e113a46463.bc77313e11a248558a6ca244ca980944ec3421fa480c50e0229ad91f1cb15aea582603202cd71796885c9e5163e500f1b72f737059aff1ddb8beea47c5a331d6760540345b7f88b2302a0e6e09589f9dcf3ff9175d8c905f990203f5fc04748008ea7a366571cbf5b09509a873dcfba3cf1d559038f5f5f7ef6e01d1850974aa220eb5178c89e61c24411af9b9a19435e.06fde789ece48d9b33c5dc857e04e9b5838f08264d928b87237d3476c4484b46";
        }
    }

    private void signUp() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("new_key", deviceKey);
        params.put("old_key", DEVICE_OLD_KEY);
        params.put("phone_type", 1);
        params.put("code", "");
        Map<String, Object> result = authRequest("/App/Authentication/Device/signUp", params);
        applyAuth(result);
        registered = true;
    }

    private void refreshToken() throws Exception {
        Map<String, Object> result = authRequest("/App/Authentication/Authenticator/refresh", new HashMap<String, Object>());
        applyAuth(result);
    }

    private void applyAuth(Map<String, Object> result) {
        if (result == null) return;
        if (result.containsKey("token") && result.get("token") != null) {
            token = String.valueOf(result.get("token"));
        }
        if (result.containsKey("app_user_id") && result.get("app_user_id") != null) {
            tokenId = String.valueOf(result.get("app_user_id"));
        }
    }

    private Map<String, Object> authRequest(String path, Map<String, Object> params) throws Exception {
        return sendEncryptedRequest(params, path, true);
    }

    private void ensureToken() throws Exception {
        if (token.isEmpty()) {
            if (registered) {
                refreshToken();
            } else {
                signUp();
            }
        }
    }

    // ========== 核心加密请求 ==========
    private Map<String, Object> sendEncryptedRequest(Map<String, Object> data, String path, boolean isAuth) throws Exception {
        if (!isAuth) {
            ensureToken();
        }

        // 1. AES 加密参数
        String jsonParams = gson.toJson(data);
        String requestKey = aesEncrypt(jsonParams, AES_KEY, AES_IV).toUpperCase();

        // 2. RSA 加密 keys
        Map<String, String> keyMap = new HashMap<>();
        keyMap.put("iv", AES_IV);
        keyMap.put("key", AES_KEY);
        String keysJson = gson.toJson(keyMap);
        String keys = rsaEncrypt(keysJson, RSA_PUBLIC_KEY);

        // 3. 生成签名
        String t = String.valueOf(System.currentTimeMillis() / 1000);
        String signStr = "token_id=,token=" + token + ",phone_type=1,request_key=" + requestKey
                + ",app_id=1,time=" + t + ",keys=" + keys + "*&zvdvdvddbfikkkumtmdwqppp?|4Y!s!2br";
        String signature = getMD5(signStr);

        // 4. 构建请求体
        Map<String, String> body = new LinkedHashMap<>();
        body.put("token", token);
        body.put("token_id", "");
        body.put("phone_type", "1");
        body.put("time", t);
        body.put("phone_model", "xiaomi-25031");
        body.put("keys", keys);
        body.put("request_key", requestKey);
        body.put("signature", signature);
        body.put("app_id", "1");
        body.put("ad_version", "1");

        // 5. 发送请求
        String url = host + path;

        OkResult result = OkHttp.post(url, body, getHeaders());
        if (result.getCode() != 200) {
            throw new Exception("HTTP " + result.getCode());
        }

        String responseBody = result.getBody();
        Map<String, Object> resp = gson.fromJson(responseBody, new TypeToken<Map<String, Object>>(){}.getType());
        if (resp == null || !resp.containsKey("code") || !"200".equals(String.valueOf(resp.get("code")))) {
            throw new Exception("业务错误: " + (resp != null ? resp.get("code") : "null"));
        }

        Map<String, Object> dataSection = (Map<String, Object>) resp.get("data");
        if (dataSection == null) {
            throw new Exception("缺少data字段");
        }

        String encryptedResponse = (String) dataSection.get("response_key");
        String encryptedKeys = (String) dataSection.get("keys");

        // 6. 解密响应
        String decryptedKeysJson = rsaDecrypt(encryptedKeys, RSA_PRIVATE_KEY);
        Map<String, String> keyInfo = gson.fromJson(decryptedKeysJson, new TypeToken<Map<String, String>>(){}.getType());
        String respKey = keyInfo.get("key");
        String respIv = keyInfo.get("iv");
        String decryptedData = aesDecrypt(encryptedResponse, respKey, respIv);

        return gson.fromJson(decryptedData, new TypeToken<Map<String, Object>>(){}.getType());
    }

    // ========== 加解密工具 ==========
    private String aesEncrypt(String text, String key, String iv) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(UTF_8));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(text.getBytes(UTF_8));
        return bytesToHex(encrypted).toUpperCase();
    }

    private String aesDecrypt(String text, String key, String iv) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(UTF_8));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = hexToBytes(text);
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, UTF_8);
    }

    private String rsaEncrypt(String text, String publicKeyStr) throws Exception {
        byte[] keyBytes = Base64.decode(publicKeyStr, Base64.DEFAULT);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = factory.generatePublic(spec);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encrypted = cipher.doFinal(text.getBytes(UTF_8));
        return Base64.encodeToString(encrypted, Base64.DEFAULT).replace("\n", "");
    }

    private String rsaDecrypt(String encryptedData, String privateKeyStr) throws Exception {
        String pem = privateKeyStr.replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("\n", "");
        byte[] keyBytes = Base64.decode(pem, Base64.DEFAULT);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = factory.generatePrivate(spec);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] encrypted = Base64.decode(encryptedData, Base64.DEFAULT);
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, UTF_8);
    }

    private String getMD5(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(text.getBytes(UTF_8));
        return bytesToHex(digest).toUpperCase();
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private Map<String, Object> getData(Map<String, Object> body, String path) {
        for (int attempt = 0; attempt < 3; attempt++) {
            int tried = 0;
            while (tried < hosts.length) {
                host = hosts[hostIndex];
                try {
                    Map<String, Object> result = sendEncryptedRequest(body, path, false);
                    if (result != null) return result;
                } catch (Exception e) {
                    Log.e(TAG, "请求失败: " + e.getMessage());
                }
                hostIndex = (hostIndex + 1) % hosts.length;
                tried++;
            }
            if (attempt < 2) {
                try {
                    initToken();
                } catch (Exception ignored) {}
                hostIndex = 0;
            }
        }
        return null;
    }

    // ========== TVBox 业务接口 ==========
    @Override
    public String homeContent(boolean filter) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, String>> classes = new ArrayList<>();
        String[][] classData = {{"1", "电影"}, {"2", "电视剧"}, {"4", "动漫"}, {"3", "综艺"}, {"64", "短剧"}};
        for (String[] cls : classData) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("type_name", cls[1]);
            item.put("type_id", cls[0]);
            classes.add(item);
        }
        result.put("class", classes);

        if (filter) {
            Map<String, Object> filters = new HashMap<>();
            for (String[] cls : classData) {
                List<Map<String, Object>> filterGroup = new ArrayList<>();

                // 地区筛选
                Map<String, Object> area = new HashMap<>();
                area.put("key", "area");
                area.put("name", "地区");
                List<Map<String, String>> areaValues = new ArrayList<>();
                String[][] areas = {{"全部", "0"}, {"大陆", "大陆"}, {"香港", "香港"}, {"台湾", "台湾"}, {"美国", "美国"}, {"韩国", "韩国"}, {"日本", "日本"}};
                for (String[] a : areas) {
                    Map<String, String> v = new HashMap<>();
                    v.put("n", a[0]); v.put("v", a[1]);
                    areaValues.add(v);
                }
                area.put("value", areaValues);

                // 年份筛选
                Map<String, Object> year = new HashMap<>();
                year.put("key", "year");
                year.put("name", "年份");
                List<Map<String, String>> yearValues = new ArrayList<>();
                String[][] years = {{"全部", "0"}, {"2025", "2025"}, {"2024", "2024"}, {"2023", "2023"}, {"2022", "2022"}, {"2021", "2021"}, {"2020", "2020"}};
                for (String[] y : years) {
                    Map<String, String> v = new HashMap<>();
                    v.put("n", y[0]); v.put("v", y[1]);
                    yearValues.add(v);
                }
                year.put("value", yearValues);

                // 排序筛选
                Map<String, Object> sort = new HashMap<>();
                sort.put("key", "sort");
                sort.put("name", "排序");
                List<Map<String, String>> sortValues = new ArrayList<>();
                String[][] sorts = {{"最新", "d_id"}, {"最热", "d_hits"}, {"推荐", "d_score"}};
                for (String[] s : sorts) {
                    Map<String, String> v = new HashMap<>();
                    v.put("n", s[0]); v.put("v", s[1]);
                    sortValues.add(v);
                }
                sort.put("value", sortValues);

                filterGroup.add(area);
                filterGroup.add(year);
                filterGroup.add(sort);
                filters.put(cls[0], filterGroup);
            }
            result.put("filters", filters);
        }

        result.put("list", new ArrayList<>());
        return gson.toJson(result);
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("area", extend != null && extend.containsKey("area") ? extend.get("area") : "0");
        body.put("year", extend != null && extend.containsKey("year") ? extend.get("year") : "0");
        body.put("pageSize", "30");
        body.put("sort", extend != null && extend.containsKey("sort") ? extend.get("sort") : "d_id");
        body.put("page", pg);
        body.put("tid", tid);

        Map<String, Object> data = getData(body, "/App/IndexList/indexList");
        List<Map<String, String>> videos = new ArrayList<>();
        if (data != null && data.containsKey("list") && data.get("list") instanceof List) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
            for (Map<String, Object> item : list) {
                int continu = 0;
                if (item.containsKey("vod_continu") && item.get("vod_continu") != null) {
                    continu = ((Number) item.get("vod_continu")).intValue();
                }
                String remarks = continu == 0 ? "电影" : "更新至" + continu + "集";
                Map<String, String> video = new LinkedHashMap<>();
                video.put("vod_id", item.get("vod_id") + "/" + continu);
                video.put("vod_name", getOrDefault(item, "vod_name", ""));
                video.put("vod_pic", getOrDefault(item, "vod_pic", ""));
                video.put("vod_remarks", remarks);
                videos.add(video);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", videos);
        result.put("page", Integer.parseInt(pg));
        result.put("pagecount", 9999);
        result.put("limit", 30);
        result.put("total", 999999);
        return gson.toJson(result);
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        String vodId = ids.get(0).split("/")[0];
        String t = String.valueOf(System.currentTimeMillis() / 1000);

        Map<String, Object> body1 = new HashMap<>();
        body1.put("token_id", tokenId);
        body1.put("vod_id", vodId);
        body1.put("mobile_time", t);
        body1.put("token", token);
        Map<String, Object> qdata = getData(body1, "/App/IndexPlay/playInfo");

        Map<String, Object> body2 = new HashMap<>();
        body2.put("vurl_cloud_id", "2");
        body2.put("vod_d_id", vodId);
        Map<String, Object> jdata = getData(body2, "/App/Resource/Vurl/show");

        if (qdata == null || !qdata.containsKey("vodInfo")) {
            Map<String, Object> emptyResult = new LinkedHashMap<>();
            emptyResult.put("list", new ArrayList<>());
            return gson.toJson(emptyResult);
        }

        Map<String, Object> vodInfo = (Map<String, Object>) qdata.get("vodInfo");
        Map<String, Object> vod = new LinkedHashMap<>();
        vod.put("vod_id", vodId);
        vod.put("vod_name", getOrDefault(vodInfo, "vod_name", ""));
        vod.put("vod_pic", getOrDefault(vodInfo, "vod_pic", ""));
        vod.put("vod_year", getOrDefault(vodInfo, "vod_year", ""));
        vod.put("vod_area", getOrDefault(vodInfo, "vod_area", ""));
        vod.put("vod_actor", getOrDefault(vodInfo, "vod_actor", ""));
        vod.put("vod_director", getOrDefault(vodInfo, "vod_director", ""));
        vod.put("vod_content", getOrDefault(vodInfo, "vod_use_content", "").trim());
        vod.put("vod_play_from", "瓜子影视");

        List<String> playList = new ArrayList<>();
        if (jdata != null && jdata.containsKey("list") && jdata.get("list") instanceof List) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) jdata.get("list");
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> item = list.get(i);
                if (item.containsKey("play") && item.get("play") instanceof Map) {
                    Map<String, Object> playMap = (Map<String, Object>) item.get("play");
                    List<String> names = new ArrayList<>();
                    List<String> params = new ArrayList<>();
                    for (Map.Entry<String, Object> entry : playMap.entrySet()) {
                        if (entry.getValue() instanceof Map) {
                            Map<String, Object> val = (Map<String, Object>) entry.getValue();
                            if (val.containsKey("param") && val.get("param") != null) {
                                names.add(entry.getKey());
                                params.add(String.valueOf(val.get("param")));
                            }
                        }
                    }
                    if (!params.isEmpty()) {
                        String playName = list.size() == 1 ? String.valueOf(vod.get("vod_name")) : String.valueOf(i + 1);
                        String playUrl = params.get(params.size() - 1) + "||" + joinStrings("@", names);
                        playList.add(playName + "$" + playUrl);
                    }
                }
            }
        }
        vod.put("vod_play_url", joinStrings("#", playList));

        List<Map<String, Object>> resultList = new ArrayList<>();
        resultList.add(vod);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", resultList);
        return gson.toJson(result);
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        return searchContent(key, quick, "1");
    }

    @Override
    public String searchContent(String key, boolean quick, String pg) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("keywords", key);
        body.put("order_val", "1");
        body.put("page", pg);

        Map<String, Object> data = getData(body, "/App/Index/findMoreVod");
        List<Map<String, String>> videos = new ArrayList<>();
        if (data != null && data.containsKey("list") && data.get("list") instanceof List) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
            for (Map<String, Object> item : list) {
                int continu = 0;
                if (item.containsKey("vod_continu") && item.get("vod_continu") != null) {
                    continu = ((Number) item.get("vod_continu")).intValue();
                }
                String remarks = continu == 0 ? "电影" : "更新至" + continu + "集";
                Map<String, String> video = new LinkedHashMap<>();
                video.put("vod_id", item.get("vod_id") + "/" + continu);
                video.put("vod_name", getOrDefault(item, "vod_name", ""));
                video.put("vod_pic", getOrDefault(item, "vod_pic", ""));
                video.put("vod_remarks", remarks);
                videos.add(video);
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", videos);
        result.put("page", Integer.parseInt(pg));
        result.put("pagecount", 9999);
        result.put("limit", 30);
        result.put("total", 999999);
        return gson.toJson(result);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        String[] parts = id.split("\\|\\|");
        if (parts.length < 2) {
            Map<String, Object> emptyResult = new LinkedHashMap<>();
            emptyResult.put("parse", 0);
            emptyResult.put("playUrl", "");
            emptyResult.put("url", "");
            return gson.toJson(emptyResult);
        }
        String paramStr = parts[0];
        String[] resolutions = parts[1].split("@");
        Map<String, Object> params = new HashMap<>();
        for (String pair : paramStr.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) params.put(kv[0], kv[1]);
        }
        if (resolutions.length > 0) {
            int maxRes = 0;
            for (String r : resolutions) {
                try {
                    int res = Integer.parseInt(r);
                    if (res > maxRes) maxRes = res;
                } catch (Exception ignored) {}
            }
            params.put("resolution", String.valueOf(maxRes));
            Map<String, Object> data = getData(params, "/App/Resource/VurlDetail/showOne");
            if (data != null && data.containsKey("url")) {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("parse", 0);
                result.put("playUrl", "");
                result.put("url", data.get("url"));
                result.put("header", "{\"User-Agent\":\"Lavf/57.83.100\",\"Referer\":\"http://WJiZxLXA2.com/\"}");
                result.put("danmaku", "http://127.0.0.1:9978/proxy?do=diydanmu");
                return gson.toJson(result);
            }
        }
        Map<String, Object> emptyResult = new LinkedHashMap<>();
        emptyResult.put("parse", 0);
        emptyResult.put("playUrl", "");
        emptyResult.put("url", "");
        return gson.toJson(emptyResult);
    }

    public Object[] proxyLocal(Map<String, String> params) throws Exception {
        return null;
    }

    @Override
    public void destroy() {}
}