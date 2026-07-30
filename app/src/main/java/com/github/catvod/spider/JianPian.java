package com.github.catvod.spider;

import android.text.TextUtils;
import android.util.Log;

import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JianPian extends Spider {

    private static final String TAG = "JianPian";
    private String host = "https://ev5356.970xw.com";
    private Map<String, String> headers;
    private String imgHost = "";

    @Override
    public void init(android.content.Context context, String extend) throws Exception {
        headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Linux; Android 9; V2196A Build/PQ3A.190705.08211809; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/91.0.4472.114 Mobile Safari/537.36;webank/h5face;webank/1.0;netType:NETWORK_WIFI;appVersion:416;packageName:com.jp3.xg3");
        headers.put("Referer", host);

        // 获取图片域名
        String resp = OkHttp.string(host + "/api/appAuthConfig", headers);
        JSONObject obj = new JSONObject(resp);
        String domain = obj.getJSONObject("data").getString("imgDomain");
        imgHost = domain.startsWith("http") ? domain : "https://" + domain;
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        JSONObject result = new JSONObject();

        JSONArray classes = new JSONArray();
        String[][] cls = {{"1", "电影"}, {"2", "电视剧"}, {"3", "动漫"}, {"4", "综艺"}};
        for (String[] c : cls) {
            JSONObject item = new JSONObject();
            item.put("type_id", c[0]);
            item.put("type_name", c[1]);
            classes.put(item);
        }
        result.put("class", classes);

        // 构建 filters（直接使用 Python 中的 filterObj 转 JSON）
        String filtersJson = "{\"1\":[{\"key\":\"cateId\",\"name\":\"分类\",\"value\":[{\"v\":\"1\",\"n\":\"剧情\"},{\"v\":\"2\",\"n\":\"爱情\"},{\"v\":\"3\",\"n\":\"动画\"},{\"v\":\"4\",\"n\":\"喜剧\"},{\"v\":\"5\",\"n\":\"战争\"},{\"v\":\"6\",\"n\":\"歌舞\"},{\"v\":\"7\",\"n\":\"古装\"},{\"v\":\"8\",\"n\":\"奇幻\"},{\"v\":\"9\",\"n\":\"冒险\"},{\"v\":\"10\",\"n\":\"动作\"},{\"v\":\"11\",\"n\":\"科幻\"},{\"v\":\"12\",\"n\":\"悬疑\"},{\"v\":\"13\",\"n\":\"犯罪\"},{\"v\":\"14\",\"n\":\"家庭\"},{\"v\":\"15\",\"n\":\"传记\"},{\"v\":\"16\",\"n\":\"运动\"},{\"v\":\"18\",\"n\":\"惊悚\"},{\"v\":\"20\",\"n\":\"短片\"},{\"v\":\"21\",\"n\":\"历史\"},{\"v\":\"22\",\"n\":\"音乐\"},{\"v\":\"23\",\"n\":\"西部\"},{\"v\":\"24\",\"n\":\"武侠\"},{\"v\":\"25\",\"n\":\"恐怖\"}]},{\"key\":\"area\",\"name\":\"地區\",\"value\":[{\"v\":\"1\",\"n\":\"国产\"},{\"v\":\"3\",\"n\":\"香港\"},{\"v\":\"6\",\"n\":\"台湾\"},{\"v\":\"5\",\"n\":\"美国\"},{\"v\":\"18\",\"n\":\"韩国\"},{\"v\":\"2\",\"n\":\"日本\"}]},{\"key\":\"year\",\"name\":\"年代\",\"value\":[{\"v\":\"107\",\"n\":\"2025\"},{\"v\":\"119\",\"n\":\"2024\"},{\"v\":\"153\",\"n\":\"2023\"},{\"v\":\"101\",\"n\":\"2022\"},{\"v\":\"118\",\"n\":\"2021\"},{\"v\":\"16\",\"n\":\"2020\"},{\"v\":\"7\",\"n\":\"2019\"},{\"v\":\"2\",\"n\":\"2018\"},{\"v\":\"3\",\"n\":\"2017\"},{\"v\":\"22\",\"n\":\"2016\"},{\"v\":\"2015\",\"n\":\"2015以前\"}]},{\"key\":\"sort\",\"name\":\"排序\",\"value\":[{\"v\":\"update\",\"n\":\"最新\"},{\"v\":\"hot\",\"n\":\"最热\"},{\"v\":\"rating\",\"n\":\"评分\"}]}],\"2\":[{\"key\":\"area\",\"name\":\"地區\",\"value\":[{\"v\":\"1\",\"n\":\"国产\"},{\"v\":\"3\",\"n\":\"香港\"},{\"v\":\"6\",\"n\":\"台湾\"},{\"v\":\"5\",\"n\":\"美国\"},{\"v\":\"18\",\"n\":\"韩国\"},{\"v\":\"2\",\"n\":\"日本\"}]},{\"key\":\"year\",\"name\":\"年代\",\"value\":[{\"v\":\"107\",\"n\":\"2025\"},{\"v\":\"119\",\"n\":\"2024\"},{\"v\":\"153\",\"n\":\"2023\"},{\"v\":\"101\",\"n\":\"2022\"},{\"v\":\"118\",\"n\":\"2021\"},{\"v\":\"16\",\"n\":\"2020\"},{\"v\":\"7\",\"n\":\"2019\"},{\"v\":\"2\",\"n\":\"2018\"},{\"v\":\"3\",\"n\":\"2017\"},{\"v\":\"22\",\"n\":\"2016\"},{\"v\":\"2015\",\"n\":\"2015以前\"}]},{\"key\":\"sort\",\"name\":\"排序\",\"value\":[{\"v\":\"update\",\"n\":\"最新\"},{\"v\":\"hot\",\"n\":\"最热\"},{\"v\":\"rating\",\"n\":\"评分\"}]}],\"3\":[{\"key\":\"area\",\"name\":\"地區\",\"value\":[{\"v\":\"1\",\"n\":\"国产\"},{\"v\":\"3\",\"n\":\"香港\"},{\"v\":\"6\",\"n\":\"台湾\"},{\"v\":\"5\",\"n\":\"美国\"},{\"v\":\"18\",\"n\":\"韩国\"},{\"v\":\"2\",\"n\":\"日本\"}]},{\"key\":\"year\",\"name\":\"年代\",\"value\":[{\"v\":\"107\",\"n\":\"2025\"},{\"v\":\"119\",\"n\":\"2024\"},{\"v\":\"153\",\"n\":\"2023\"},{\"v\":\"101\",\"n\":\"2022\"},{\"v\":\"118\",\"n\":\"2021\"},{\"v\":\"16\",\"n\":\"2020\"},{\"v\":\"7\",\"n\":\"2019\"},{\"v\":\"2\",\"n\":\"2018\"},{\"v\":\"3\",\"n\":\"2017\"},{\"v\":\"22\",\"n\":\"2016\"},{\"v\":\"2015\",\"n\":\"2015以前\"}]},{\"key\":\"sort\",\"name\":\"排序\",\"value\":[{\"v\":\"update\",\"n\":\"最新\"},{\"v\":\"hot\",\"n\":\"最热\"},{\"v\":\"rating\",\"n\":\"评分\"}]}],\"4\":[{\"key\":\"area\",\"name\":\"地區\",\"value\":[{\"v\":\"1\",\"n\":\"国产\"},{\"v\":\"3\",\"n\":\"香港\"},{\"v\":\"6\",\"n\":\"台湾\"},{\"v\":\"5\",\"n\":\"美国\"},{\"v\":\"18\",\"n\":\"韩国\"},{\"v\":\"2\",\"n\":\"日本\"}]},{\"key\":\"year\",\"name\":\"年代\",\"value\":[{\"v\":\"107\",\"n\":\"2025\"},{\"v\":\"119\",\"n\":\"2024\"},{\"v\":\"153\",\"n\":\"2023\"},{\"v\":\"101\",\"n\":\"2022\"},{\"v\":\"118\",\"n\":\"2021\"},{\"v\":\"16\",\"n\":\"2020\"},{\"v\":\"7\",\"n\":\"2019\"},{\"v\":\"2\",\"n\":\"2018\"},{\"v\":\"3\",\"n\":\"2017\"},{\"v\":\"22\",\"n\":\"2016\"},{\"v\":\"2015\",\"n\":\"2015以前\"}]},{\"key\":\"sort\",\"name\":\"排序\",\"value\":[{\"v\":\"update\",\"n\":\"最新\"},{\"v\":\"hot\",\"n\":\"最热\"},{\"v\":\"rating\",\"n\":\"评分\"}]}]}";
        result.put("filters", new JSONObject(filtersJson));
        return result.toString();
    }

    @Override
    public String homeVideoContent() throws Exception {
        String url = host + "/api/slide/list?pos_id=88";
        String resp = OkHttp.string(url, headers);
        JSONObject obj = new JSONObject(resp);
        JSONArray data = obj.getJSONArray("data");
        JSONArray list = new JSONArray();
        for (int i = 0; i < data.length(); i++) {
            JSONObject item = data.getJSONObject(i);
            JSONObject vod = new JSONObject();
            vod.put("vod_id", item.getString("jump_id"));
            vod.put("vod_name", item.getString("title"));
            vod.put("vod_pic", imgHost + item.getString("thumbnail"));
            vod.put("vod_remarks", "");
            JSONObject style = new JSONObject();
            style.put("type", "rect");
            style.put("ratio", 1.33);
            vod.put("style", style);
            list.put(vod);
        }
        JSONObject result = new JSONObject();
        result.put("list", list);
        return result.toString();
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("fcate_pid", tid);
        params.put("page", pg);
        params.put("category_id", extend.getOrDefault("cateId", ""));
        params.put("area", extend.getOrDefault("area", ""));
        params.put("year", extend.getOrDefault("year", ""));
        params.put("type", extend.getOrDefault("cateId", "")); // 注意：type 与 category_id 相同
        params.put("sort", extend.getOrDefault("sort", ""));

        StringBuilder urlBuilder = new StringBuilder(host + "/api/crumb/list?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!TextUtils.isEmpty(entry.getValue())) {
                urlBuilder.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
            }
        }
        String url = urlBuilder.toString();
        if (url.endsWith("&")) url = url.substring(0, url.length() - 1);
        String resp = OkHttp.string(url, headers);
        JSONObject obj = new JSONObject(resp);
        JSONArray data = obj.getJSONArray("data");
        JSONArray list = new JSONArray();
        for (int i = 0; i < data.length(); i++) {
            JSONObject item = data.getJSONObject(i);
            JSONObject vod = new JSONObject();
            vod.put("vod_id", item.getString("id"));
            vod.put("vod_name", item.getString("title"));
            vod.put("vod_pic", imgHost + item.getString("path"));
            vod.put("vod_remarks", item.optString("mask", ""));
            vod.put("vod_year", "");
            list.put(vod);
        }
        JSONObject result = new JSONObject();
        result.put("list", list);
        result.put("page", Integer.parseInt(pg));
        result.put("pagecount", 99999);
        result.put("limit", 15);
        result.put("total", 99999);
        return result.toString();
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        String id = ids.get(0);
        String url = host + "/api/video/detailv2?id=" + id;
        String resp = OkHttp.string(url, headers);
        JSONObject obj = new JSONObject(resp);
        JSONObject res = obj.getJSONObject("data");

        // 处理线路
        List<String> playFrom = new ArrayList<>();
        List<String> playUrls = new ArrayList<>();

        // 查找“常规线路”
        JSONArray sourceList = res.optJSONArray("source_list_source");
        if (sourceList != null) {
            for (int i = 0; i < sourceList.length(); i++) {
                JSONObject source = sourceList.getJSONObject(i);
                if ("常规线路".equals(source.optString("name"))) {
                    JSONArray parts = source.getJSONArray("source_list");
                    List<String> partUrls = new ArrayList<>();
                    for (int j = 0; j < parts.length(); j++) {
                        JSONObject part = parts.getJSONObject(j);
                        String name = part.optString("source_name");
                        if (TextUtils.isEmpty(name)) name = part.optString("weight");
                        String urlPart = part.optString("url");
                        if (!TextUtils.isEmpty(urlPart)) {
                            partUrls.add(name + "$" + urlPart);
                        }
                    }
                    if (!partUrls.isEmpty()) {
                        playFrom.add("边下边播");
                        playUrls.add(TextUtils.join("#", partUrls));
                    }
                    break; // 只取第一个常规线路
                }
            }
        }

        JSONObject vod = new JSONObject();
        vod.put("vod_id", id);
        // 类型
        JSONArray types = res.optJSONArray("types");
        List<String> typeNames = new ArrayList<>();
        if (types != null) {
            for (int i = 0; i < types.length(); i++) {
                typeNames.add(types.getJSONObject(i).getString("name"));
            }
        }
        vod.put("type_name", TextUtils.join("/", typeNames));
        vod.put("vod_year", res.optString("year", ""));
        vod.put("vod_area", res.optString("area", ""));
        vod.put("vod_remarks", res.optString("mask", ""));
        vod.put("vod_content", res.optString("description", ""));
        vod.put("vod_play_from", TextUtils.join("$$$", playFrom));
        vod.put("vod_play_url", TextUtils.join("$$$", playUrls));

        JSONArray list = new JSONArray();
        list.put(vod);
        JSONObject result = new JSONObject();
        result.put("list", list);
        return result.toString();
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        JSONObject result = new JSONObject();
        result.put("parse", 0);
        if (id.contains(".m3u8")) {
            result.put("url", id);
        } else {
            result.put("url", "tvbox-xg:" + id);
        }
        return result.toString();
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        return searchContent(key, quick, "1");
    }

    @Override
    public String searchContent(String key, boolean quick, String pg) throws Exception {
        String url = host + "/api/v2/search/videoV2?key=" + URLEncoder.encode(key, "UTF-8") + "&category_id=88&page=" + pg + "&pageSize=20";
        String resp = OkHttp.string(url, headers);
        JSONObject obj = new JSONObject(resp);
        JSONArray data = obj.getJSONArray("data");

        // 本地过滤（原Python做了大小写不敏感过滤）
        String keyLower = key.toLowerCase();
        JSONArray list = new JSONArray();
        for (int i = 0; i < data.length(); i++) {
            JSONObject item = data.getJSONObject(i);
            String title = item.optString("title", "").toLowerCase();
            if (title.contains(keyLower)) {
                JSONObject vod = new JSONObject();
                vod.put("vod_id", item.getString("id"));
                vod.put("vod_name", item.getString("title"));
                vod.put("vod_pic", imgHost + item.getString("thumbnail"));
                vod.put("vod_remarks", item.optString("mask", ""));
                vod.put("vod_year", "");
                list.put(vod);
            }
        }
        JSONObject result = new JSONObject();
        result.put("list", list);
        result.put("limit", 20);
        return result.toString();
    }

    @Override
    public boolean isVideoFormat(String url) throws Exception {
        return url != null && (url.endsWith(".m3u8") || url.endsWith(".mp4") || url.endsWith(".ts"));
    }

    @Override
    public void destroy() {
    }

    @Override
    public Object[] proxyLocal(Map<String, String> params) throws Exception {
        return null;
    }
}