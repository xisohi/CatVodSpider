package com.github.catvod.spider;

import android.text.TextUtils;
import android.util.Base64;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import com.github.catvod.net.OkResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Guazi extends Spider {

    private String[] hosts = {
            "https://apinew.uozvr.com",
            "https://api.w32z7vtd.com",
            "https://api.6a7nnf7.com",
            "https://api.umygrx3.com",
            "https://api.rmedphk.com"
    };

    private int hostIndex = 0;
    private String host = hosts[0];

    private final String AES_KEY = "OITxa5OqAYjhswxx";
    private final String AES_IV = "rCMNwZASNBKZ8mXV";

    private final String DEVICE_OLD_KEY = "aLFBMWpxBrIDAD1Si/KVvm41";

    private final String RSA_PUBLIC_KEY =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDUM5+/y8sPsWkd1/RQS64X259EUwxFXFE5HlA65MqrxnPs0JqoSRojSDy5QhwvROlaD6TwRQHKMY2OAZ6SnQeUJsChTEFIR9qUkwrs3/MVUMxjsv6JS6Oe/juclyJGTgVmDhB55EafXsD0SQYVj/QXXsxR6ewR5E2kL52yAAD4yQIDAQAB";

    private final String RSA_PRIVATE_KEY =
            "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGAe6hKrWLi1zQmjTT1" +
                    "ozbE4QdFeJGNxubxld6GrFGximxfMsMB6BpJhpcTouAqywAFppiKetUBBbXwYsYU" +
                    "1wNr648XVmPmCMCy4rY8vdliFnbMUj086DU6Z+/oXBdWU3/b1G0DN3E9wULRSwcK" +
                    "ZT3wj/cCI1vsCm3gj2R5SqkA9Y0CAwEAAQKBgAJH+4CxV0/zBVcLiBCHvSANm0l7" +
                    "HetybTh/j2p0Y1sTXro4ALwAaCTUeqdBjWiLSo9lNwDHFyq8zX90+gNxa7c5EqcW" +
                    "V9FmlVXr8VhfBzcZo1nXeNdXFT7tQ2yah/odtdcx+vRMSGJd1t/5k5bDd9wAvYdI" +
                    "DblMAg+wiKKZ5KcdAkEA1cCakEN4NexkF5tHPRrR6XOY/XHfkqXxEhMqmNbB9U34" +
                    "saTJnLWIHC8IXys6Qmzz30TtzCjuOqKRRy+FMM4TdwJBAJQZFPjsGC+RqcG5UvVM" +
                    "iMPhnwe/bXEehShK86yJK/g/UiKrO87h3aEu5gcJqBygTq3BBBoH2md3pr/W+hUM" +
                    "WBsCQQChfhTIrdDinKi6lRxrdBnn0Ohjg2cwuqK5zzU9p/N+S9x7Ck8wUI53DKm8" +
                    "jUJE8WAG7WLj/oCOWEh+ic6NIwTdAkEAj0X8nhx6AXsgCYRql1klbqtVmL8+95KZ" +
                    "K7PnLWG/IfjQUy3pPGoSaZ7fdquG8bq8oyf5+dzjE/oTXcByS+6XRQJAP/5ciy1b" +
                    "L3NhUhsaOVy55MHXnPjdcTX0FaLi+ybXZIfIQ2P4rb19mVq1feMbCXhz+L1rG8oa" +
                    "t5lYKfpe8k83ZA==";

    private String deviceId;
    private String deviceKey;

    private String token = "";
    private String tokenId = "";

    private boolean registered = false;

    private HashMap<String, String> header;

    @Override
    public void init(android.content.Context context) throws Exception {
        Random random = new Random();
        deviceId = String.valueOf(864150060000000L + random.nextInt(9999));
        deviceKey = randomHex(40);

        header = new HashMap<>();
        header.put("User-Agent", "Lavf/57.83.100");
        header.put("code", "GZ0369");
        header.put("deviceId", deviceId);
        header.put("lang", "zh_cn");
        header.put("Cache-Control", "no-cache");
        header.put("Content-Type", "application/x-www-form-urlencoded");
        header.put("Version", "2604028");
        header.put("PackageName", "com.ae06aebdbb.y286327f5a.ofe849883320260517");
        header.put("Ver", "3.0.3.2");
        header.put("api-ver", "3.0.3.2");
        header.put("Referer", host);

        initToken();
    }

    private String randomHex(int length) {
        String chars = "0123456789ABCDEF";
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // ========== Token 认证模块（已修复：先尝试 signIn，失败再 signUp） ==========

    /**
     * 初始化Token - 先尝试登录，失败则注册
     */
    private void initToken() {
        try {
            // 1. 先尝试登录（设备已存在）
            signIn();
            refreshToken();
        } catch (Exception e) {
            // 2. 登录失败（设备不存在），尝试注册
            try {
                signUp();
                registered = true;
                refreshToken();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    /**
     * 设备登录（设备已存在时使用）
     */
    private void signIn() throws Exception {
        HashMap<String, String> params = new HashMap<>();
        params.put("new_key", deviceKey);
        params.put("old_key", DEVICE_OLD_KEY);
        params.put("phone_type", "1");
        params.put("code", "");

        JSONObject result = authRequest("/App/Authentication/Device/signIn", params);
        applyAuth(result);
    }

    /**
     * 设备注册（设备不存在时使用）
     */
    private void signUp() throws Exception {
        HashMap<String, String> params = new HashMap<>();
        params.put("new_key", deviceKey);
        params.put("old_key", DEVICE_OLD_KEY);
        params.put("phone_type", "1");
        params.put("code", "");

        JSONObject result = authRequest("/App/Authentication/Device/signUp", params);
        applyAuth(result);
        registered = true;
    }

    /**
     * 刷新token
     */
    private void refreshToken() throws Exception {
        HashMap<String, String> params = new HashMap<>();
        JSONObject result = authRequest("/App/Authentication/Authenticator/refresh", params);
        applyAuth(result);
    }

    /**
     * 提取token（增加 token_id 兼容性）
     */
    private void applyAuth(JSONObject json) throws Exception {
        if (json == null)
            throw new Exception("认证返回为空");

        token = json.optString("token", "");
        // 兼容不同字段名
        tokenId = json.optString("app_user_id", json.optString("token_id", ""));

        if (token.length() == 0) {
            throw new Exception("没有token");
        }
    }

    /**
     * 确保 Token 有效（核心修复）
     */
    private void ensureToken() throws Exception {
        if (TextUtils.isEmpty(token) || TextUtils.isEmpty(tokenId)) {
            if (registered) {
                signIn();
            } else {
                signUp();
                registered = true;
            }
            refreshToken();
        }
    }

    /**
     * 认证请求
     */
    private JSONObject authRequest(String path, HashMap<String, String> data) throws Exception {
        return sendEncryptRequest(data, path, true);
    }

    // ========== 核心加密请求 ==========

    /**
     * 核心加密请求（已加入 ensureToken）
     */
    private JSONObject sendEncryptRequest(HashMap<String, String> data, String path, boolean auth) throws Exception {
        try {
            // 关键修复：非认证请求需要确保 Token 有效
            if (!auth) {
                ensureToken();
            }

            JSONObject obj = new JSONObject(data);
            String json = obj.toString();

            String requestKey = aesEncrypt(json, AES_KEY, AES_IV).toUpperCase();

            JSONObject keyJson = new JSONObject();
            keyJson.put("iv", AES_IV);
            keyJson.put("key", AES_KEY);

            String keys = rsaEncrypt(keyJson.toString(), RSA_PUBLIC_KEY);

            String time = String.valueOf(System.currentTimeMillis() / 1000);

            String sign = "token_id=,token=" + token
                    + ",phone_type=1,"
                    + "request_key=" + requestKey
                    + ",app_id=1,"
                    + "time=" + time
                    + ",keys=" + keys
                    + "*&zvdvdvddbfikkkumtmdwqppp?|4Y!s!2br";

            String signature = md5(sign);

            HashMap<String, String> body = new HashMap<>();
            body.put("token", token);
            body.put("token_id", "");
            body.put("phone_type", "1");
            body.put("time", time);
            body.put("phone_model", "xiaomi-25031");
            body.put("keys", keys);
            body.put("request_key", requestKey);
            body.put("signature", signature);
            body.put("app_id", "1");
            body.put("ad_version", "1");

            String url = host + path;

            OkResult result = OkHttp.post(url, body, header);
            String text = result.getBody();

            JSONObject response = new JSONObject(text);
            JSONObject dataObj = response.optJSONObject("data");

            if (dataObj == null)
                return null;

            String responseKey = dataObj.optString("response_key");
            String encryptKeys = dataObj.optString("keys");

            // RSA解密返回key（使用简化可靠的解密方式）
            String keyText = rsaDecrypt(encryptKeys, RSA_PRIVATE_KEY);
            JSONObject keyInfo = new JSONObject(keyText);

            String respKey = keyInfo.optString("key");
            String respIv = keyInfo.optString("iv");

            String decrypt = aesDecrypt(responseKey, respKey, respIv);
            return new JSONObject(decrypt);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ========== 加解密工具（已简化 RSA 解密） ==========

    private String aesEncrypt(String text, String key, String iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes("UTF-8"));

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] result = cipher.doFinal(text.getBytes("UTF-8"));

        return bytesToHex(result);
    }

    private String aesDecrypt(String hex, String key, String iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes("UTF-8"));

        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] data = hexToBytes(hex);
        byte[] result = cipher.doFinal(data);

        return new String(result, "UTF-8");
    }

    private String rsaEncrypt(String text, String key) throws Exception {
        byte[] bytes = Base64.decode(key, Base64.DEFAULT);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = factory.generatePublic(spec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] result = cipher.doFinal(text.getBytes("UTF-8"));
        return Base64.encodeToString(result, Base64.NO_WRAP);
    }

    /**
     * RSA私钥解密（简化可靠版本，直接使用 PKCS8EncodedKeySpec）
     */
    private String rsaDecrypt(String text, String privateKey) throws Exception {
        byte[] keyBytes = Base64.decode(privateKey, Base64.DEFAULT);
        // 直接使用 PKCS8EncodedKeySpec，无需手动转换
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PrivateKey key = factory.generatePrivate(keySpec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] encrypted = Base64.decode(text, Base64.DEFAULT);
        byte[] decrypted = cipher.doFinal(encrypted);

        return new String(decrypted, "UTF-8");
    }

    private String md5(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] result = md.digest(text.getBytes("UTF-8"));
        return bytesToHex(result);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String s = Integer.toHexString(b & 0xff);
            if (s.length() == 1)
                sb.append("0");
            sb.append(s);
        }
        return sb.toString().toUpperCase();
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return data;
    }

    // ========== 业务数据请求 ==========

    private JSONObject getData(HashMap<String, String> body, String path) {
        try {
            for (int i = 0; i < hosts.length; i++) {
                int currentIndex = (hostIndex + i) % hosts.length;
                String currentHost = hosts[currentIndex];

                header.put("Referer", currentHost);
                host = currentHost;

                JSONObject result = sendEncryptRequest(body, path, false);
                if (result != null) {
                    hostIndex = currentIndex;
                    return result;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ========== TVBox 接口 ==========

    @Override
    public String homeContent(boolean filter) throws Exception {
        ArrayList<Class> classes = new ArrayList<>();
        classes.add(new Class("1", "电影"));
        classes.add(new Class("2", "电视剧"));
        classes.add(new Class("4", "动漫"));
        classes.add(new Class("3", "综艺"));
        classes.add(new Class("64", "短剧"));

        return Result.string(classes, new ArrayList<Vod>());
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        ArrayList<Vod> list = new ArrayList<>();
        HashMap<String, String> body = new HashMap<>();

        body.put("area", extend.get("area") == null ? "0" : extend.get("area"));
        body.put("year", extend.get("year") == null ? "0" : extend.get("year"));
        body.put("pageSize", "30");
        body.put("sort", extend.get("sort") == null ? "d_id" : extend.get("sort"));
        body.put("page", pg);
        body.put("tid", tid);

        JSONObject data = getData(body, "/App/IndexList/indexList");

        if (data != null) {
            JSONArray arr = data.optJSONArray("list");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = arr.optJSONObject(i);
                    String id = item.optString("vod_id");
                    String continu = item.optString("vod_continu", "0");
                    String remark = continu.equals("0") ? "电影" : "更新至" + continu + "集";

                    list.add(new Vod(
                            id + "/" + continu,
                            item.optString("vod_name"),
                            item.optString("vod_pic"),
                            remark
                    ));
                }
            }
        }

        return Result.string(Integer.parseInt(pg), 9999, 30, 999999, list);
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        if (ids == null || ids.size() == 0)
            return Result.string(new Vod());

        String vodId = ids.get(0).split("/")[0];

        HashMap<String, String> body = new HashMap<>();
        body.put("token_id", tokenId);
        body.put("vod_id", vodId);
        body.put("mobile_time", String.valueOf(System.currentTimeMillis() / 1000));
        body.put("token", token);

        JSONObject info = getData(body, "/App/IndexPlay/playInfo");

        HashMap<String, String> body2 = new HashMap<>();
        body2.put("vurl_cloud_id", "2");
        body2.put("vod_d_id", vodId);

        JSONObject urls = getData(body2, "/App/Resource/Vurl/show");

        Vod vod = new Vod();
        vod.setVodId(vodId);

        if (info != null) {
            JSONObject vodInfo = info.optJSONObject("vodInfo");
            if (vodInfo != null) {
                vod.setVodName(vodInfo.optString("vod_name"));
                vod.setVodPic(vodInfo.optString("vod_pic"));
                vod.setVodYear(vodInfo.optString("vod_year"));
                vod.setVodArea(vodInfo.optString("vod_area"));
                vod.setVodActor(vodInfo.optString("vod_actor"));
                vod.setVodDirector(vodInfo.optString("vod_director"));
                vod.setVodContent(vodInfo.optString("vod_use_content"));
            }
        }

        ArrayList<String> play = new ArrayList<>();

        if (urls != null) {
            JSONArray arr = urls.optJSONArray("list");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = arr.optJSONObject(i);
                    JSONObject p = item.optJSONObject("play");

                    if (p != null) {
                        Iterator<String> keys = p.keys();
                        ArrayList<String> names = new ArrayList<>();
                        ArrayList<String> values = new ArrayList<>();

                        while (keys.hasNext()) {
                            String k = keys.next();
                            JSONObject value = p.optJSONObject(k);
                            if (value != null) {
                                String param = value.optString("param");
                                if (!param.isEmpty()) {
                                    names.add(k);
                                    values.add(param);
                                }
                            }
                        }

                        if (values.size() > 0) {
                            String playUrl = values.get(values.size() - 1) + "||" + TextUtils.join("@", names);
                            play.add((i + 1) + "$" + playUrl);
                        }
                    }
                }
            }
        }

        vod.setVodPlayFrom("瓜子影视");
        vod.setVodPlayUrl(TextUtils.join("#", play));

        return Result.string(vod);
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        ArrayList<Vod> list = new ArrayList<>();
        HashMap<String, String> body = new HashMap<>();

        body.put("keywords", key);
        body.put("order_val", "1");
        body.put("page", "1");

        JSONObject data = getData(body, "/App/Index/findMoreVod");

        if (data != null) {
            JSONArray arr = data.optJSONArray("list");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = arr.optJSONObject(i);
                    String id = item.optString("vod_id");
                    String num = item.optString("vod_continu", "0");

                    list.add(new Vod(
                            id + "/" + num,
                            item.optString("vod_name"),
                            item.optString("vod_pic"),
                            num.equals("0") ? "电影" : "更新至" + num + "集"
                    ));
                }
            }
        }

        return Result.string(list);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        String[] split = id.split("\\|\\|");
        if (split.length < 2)
            return Result.get().url("").string();

        HashMap<String, String> params = new HashMap<>();
        String[] arr = split[0].split("&");

        for (String s : arr) {
            if (s.contains("=")) {
                String[] kv = s.split("=");
                params.put(kv[0], kv.length > 1 ? kv[1] : "");
            }
        }

        // 优化：选择最高分辨率
        String[] resolution = split[1].split("@");
        if (resolution.length > 0) {
            int maxRes = 0;
            for (String r : resolution) {
                try {
                    int val = Integer.parseInt(r);
                    if (val > maxRes) maxRes = val;
                } catch (Exception ignored) {}
            }
            params.put("resolution", String.valueOf(maxRes));
        }

        JSONObject data = getData(params, "/App/Resource/VurlDetail/showOne");

        if (data != null) {
            String url = data.optString("url");
            if (!url.isEmpty()) {
                // 构造播放所需的 Header Map（与 Python 版一致）
                HashMap<String, String> playHeaders = new HashMap<>();
                playHeaders.put("User-Agent", "Lavf/57.83.100");
                playHeaders.put("Referer", "http://WJiZxLXA2.com/");

                return Result.get()
                        .url(url)
                        .header(playHeaders)
                        .string();
            }
        }

        return Result.get().url("").string();
    }

    @Override
    public boolean isVideoFormat(String url) throws Exception {
        String u = url.toLowerCase();
        return u.endsWith(".m3u8")
                || u.endsWith(".mp4")
                || u.endsWith(".avi")
                || u.endsWith(".mkv")
                || u.endsWith(".flv")
                || u.endsWith(".ts");
    }

    @Override
    public boolean manualVideoCheck() throws Exception {
        return false;
    }

    @Override
    public void destroy() {
    }
}