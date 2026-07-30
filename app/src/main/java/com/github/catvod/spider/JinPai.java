package com.github.catvod.spider;

import android.content.Context;
import android.text.TextUtils;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JinPai extends Spider {

    private final String homeUrl = "https://m.sdzhgt.com/";
    private final String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";
    private final String errorUrl = "https://sf1-cdn-tos.huoshanstatic.com/obj/media-fe/xgplayer_doc_video/mp4/xgplayer-demo-720p.mp4";
    private final String apiKey = "cb808529bae6b6be45ecfab29a4889bc";

    private HashMap<String, String> getHeaders(String t, String sign) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", ua);
        headers.put("Referer", homeUrl);
        if (!TextUtils.isEmpty(t)) {
            headers.put("t", t);
        }
        if (!TextUtils.isEmpty(sign)) {
            headers.put("sign", sign);
        }
        return headers;
    }

    /**
     * 计算双层加密签名: SHA1(MD5(data))
     */
    private String getSign(String data) {
        try {
            // 1. MD5
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] md5Bytes = md5.digest(data.getBytes("UTF-8"));
            String md5Hex = bytesToHex(md5Bytes).toLowerCase();

            // 2. SHA1
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] sha1Bytes = sha1.digest(md5Hex.getBytes("UTF-8"));
            return bytesToHex(sha1Bytes).toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String s = Integer.toHexString(b & 0xff);
            if (s.length() == 1) sb.append("0");
            sb.append(s);
        }
        return sb.toString();
    }

    @Override
    public void init(Context context, String extend) throws Exception {
        super.init(context, extend);
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        ArrayList<Class> classes = new ArrayList<>();
        classes.add(new Class("1", "电影"));
        classes.add(new Class("2", "电视剧"));
        classes.add(new Class("3", "综艺"));
        classes.add(new Class("4", "动漫"));

        // 如果需要启用筛选，可以在此处将 Python 代码中的 filters 构造为 LinkedHashMap 或 JSONObject
        return Result.string(classes, new ArrayList<Vod>());
    }

    @Override
    public String homeVideoContent() throws Exception {
        ArrayList<Vod> list = new ArrayList<>();
        String t = String.valueOf(System.currentTimeMillis());
        String dataStr = "key=" + apiKey + "&t=" + t;
        String sign = getSign(dataStr);

        try {
            String url = homeUrl + "/api/mw-movie/anonymous/home/hotSearch";
            String response = OkHttp.string(url, getHeaders(t, sign));
            JSONObject json = new JSONObject(response);
            JSONArray dataList = json.optJSONArray("data");

            if (dataList != null) {
                for (int i = 0; i < dataList.length(); i++) {
                    JSONObject item = dataList.optJSONObject(i);
                    int typeId1 = item.optInt("typeId1", 0);
                    String remark = (typeId1 == 1) ? item.optString("vodVersion") : item.optString("vodRemarks");

                    list.add(new Vod(
                            item.optString("vodId"),
                            item.optString("vodName"),
                            item.optString("vodPic"),
                            remark
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.string(list);
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        ArrayList<Vod> list = new ArrayList<>();

        String type = extend.containsKey("type") ? extend.get("type") : "";
        String clazz = extend.containsKey("class") ? extend.get("class") : "";
        String area = extend.containsKey("area") ? extend.get("area") : "";
        String year = extend.containsKey("year") ? extend.get("year") : "";
        String lang = extend.containsKey("lang") ? extend.get("lang") : "";
        String by = extend.containsKey("by") ? extend.get("by") : "";

        String url = homeUrl + "/vod/show/id/" + tid + type + clazz + area + year + lang + by + "/page/" + pg;

        try {
            String html = OkHttp.string(url, getHeaders("", ""));
            Pattern pattern = Pattern.compile("\\\\\"list\\\\\":(.*?)\\}\\}\\}\\]");
            Matcher matcher = pattern.matcher(html);

            if (matcher.find()) {
                String jsonStr = matcher.group(1).replace("\\\"", "\"");
                JSONArray dataList = new JSONArray(jsonStr);

                for (int i = 0; i < dataList.length(); i++) {
                    JSONObject item = dataList.optJSONObject(i);
                    int typeId1 = item.optInt("typeId1", 0);
                    String remark = (typeId1 == 1) ? item.optString("vodVersion") : item.optString("vodRemarks");

                    list.add(new Vod(
                            item.optString("vodId"),
                            item.optString("vodName"),
                            item.optString("vodPic"),
                            remark
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.string(Integer.parseInt(pg), 9999, 30, 999999, list);
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        if (ids == null || ids.isEmpty()) return Result.string(new Vod());

        String id = ids.get(0);
        String t = String.valueOf(System.currentTimeMillis());
        String dataStr = "id=" + id + "&key=" + apiKey + "&t=" + t;
        String sign = getSign(dataStr);

        Vod vod = new Vod();
        try {
            String url = homeUrl + "/api/mw-movie/anonymous/video/detail?id=" + id;
            String response = OkHttp.string(url, getHeaders(t, sign));
            JSONObject data = new JSONObject(response).optJSONObject("data");

            if (data != null) {
                vod.setVodId(id);
                vod.setVodName(data.optString("vodName"));
                vod.setTypeName(data.optString("typeName"));
                vod.setVodRemarks(data.optString("vodRemarks"));
                vod.setVodYear(data.optString("vodYear"));
                vod.setVodArea(data.optString("vodArea"));
                vod.setVodActor(data.optString("vodActor"));
                vod.setVodDirector(data.optString("vodDirector"));
                vod.setVodContent(data.optString("vodContent"));

                JSONArray playList = data.optJSONArray("episodeList");
                ArrayList<String> playUrls = new ArrayList<>();

                if (playList != null) {
                    for (int i = 0; i < playList.length(); i++) {
                        JSONObject ep = playList.optJSONObject(i);
                        String name = ep.optString("name");
                        String nid = ep.optString("nid");
                        playUrls.add(name + "$" + id + "/" + nid);
                    }
                }

                vod.setVodPlayFrom("老僧酿酒");
                vod.setVodPlayUrl(TextUtils.join("#", playUrls));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.string(vod);
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        return searchContent(key, quick, "1");
    }

    public String searchContent(String key, boolean quick, String pg) throws Exception {
        ArrayList<Vod> list = new ArrayList<>();
        String t = String.valueOf(System.currentTimeMillis());
        String dataStr = "keyword=" + key + "&pageNum=" + pg + "&pageSize=12&key=" + apiKey + "&t=" + t;
        String sign = getSign(dataStr);

        try {
            String url = homeUrl + "/api/mw-movie/anonymous/video/searchByWord?keyword=" + key + "&pageNum=" + pg + "&pageSize=12";
            String response = OkHttp.string(url, getHeaders(t, sign));
            JSONObject data = new JSONObject(response).optJSONObject("data");

            if (data != null) {
                JSONObject resultObj = data.optJSONObject("result");
                if (resultObj != null) {
                    JSONArray dataList = resultObj.optJSONArray("list");
                    if (dataList != null) {
                        for (int i = 0; i < dataList.length(); i++) {
                            JSONObject item = dataList.optJSONObject(i);
                            int typeId1 = item.optInt("typeId1", 0);
                            String remark = (typeId1 == 1) ? item.optString("vodVersion") : item.optString("vodRemarks");

                            list.add(new Vod(
                                    item.optString("vodId"),
                                    item.optString("vodName"),
                                    item.optString("vodPic"),
                                    remark
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.string(list);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        String playUrl = errorUrl;
        String[] parts = id.split("/");

        if (parts.length >= 2) {
            String vodId = parts[0];
            String nid = parts[1];
            String t = String.valueOf(System.currentTimeMillis());

            String dataStr = "id=" + vodId + "&nid=" + nid + "&key=" + apiKey + "&t=" + t;
            String sign = getSign(dataStr);

            try {
                String url = homeUrl + "/api/mw-movie/anonymous/v2/video/episode/url?id=" + vodId + "&nid=" + nid;
                String response = OkHttp.string(url, getHeaders(t, sign));
                JSONObject data = new JSONObject(response).optJSONObject("data");

                if (data != null) {
                    JSONArray list = data.optJSONArray("list");
                    if (list != null && list.length() > 0) {
                        playUrl = list.optJSONObject(0).optString("url", errorUrl);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        HashMap<String, String> playHeader = new HashMap<>();
        playHeader.put("User-Agent", ua);

        return Result.get()
                .url(playUrl)
                .header(playHeader)
                .string();
    }
}