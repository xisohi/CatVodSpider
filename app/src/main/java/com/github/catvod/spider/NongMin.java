package com.github.catvod.spider;

import android.text.TextUtils;
import android.util.Base64;

import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import com.github.catvod.net.OkResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NongMin extends Spider {

    private String host = "https://vip.wwgz.cn:5200";
    private Map<String, String> headers;
    private Map<String, List<Map<String, Object>>> cateConfig;

    @Override
    public void init(android.content.Context context, String extend) throws Exception {
        headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36");
        headers.put("Referer", host + "/");
        headers.put("Accept", "text/html");

        // 分类配置（与 Python 一致）
        cateConfig = new LinkedHashMap<>();
        String[][] categories = {
                {"12", "国产剧"}, {"4", "动漫"}, {"1", "电影"},
                {"2", "电视剧"}, {"3", "综艺"}, {"26", "短剧"}
        };
        for (String[] cat : categories) {
            List<Map<String, Object>> filters = new ArrayList<>();
            Map<String, Object> filter = new LinkedHashMap<>();
            filter.put("key", "cateId");
            filter.put("name", "类型");
            List<Map<String, String>> values = new ArrayList<>();
            Map<String, String> value = new LinkedHashMap<>();
            value.put("n", cat[1]);
            value.put("v", cat[0]);
            values.add(value);
            filter.put("value", values);
            filters.add(filter);
            cateConfig.put(cat[0], filters);
        }
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        JSONObject result = new JSONObject();

        // 分类列表
        JSONArray classes = new JSONArray();
        String[][] classData = {
                {"12", "国产剧"}, {"4", "动漫"}, {"1", "电影"},
                {"2", "电视剧"}, {"3", "综艺"}, {"26", "短剧"}
        };
        for (String[] cls : classData) {
            JSONObject item = new JSONObject();
            item.put("type_name", cls[1]);
            item.put("type_id", cls[0]);
            classes.put(item);
        }
        result.put("class", classes);
        result.put("filters", new JSONObject(cateConfig));

        // 获取首页列表
        JSONArray videos = new JSONArray();
        try {
            String html = OkHttp.string(host, headers);  // 返回 String
            Document doc = Jsoup.parse(html);
            Elements items = doc.select(".globalPicList li:has(img)");
            List<String> seenIds = new ArrayList<>();
            for (Element item : items) {
                Element aTag = item.selectFirst("a");
                if (aTag == null) continue;
                String vodId = host + aTag.attr("href");
                if (seenIds.contains(vodId)) continue;
                seenIds.add(vodId);

                Element img = item.selectFirst("img");
                String picUrl = "";
                if (img != null) {
                    picUrl = img.attr("data-echo");
                    if (TextUtils.isEmpty(picUrl)) picUrl = img.attr("data-src");
                    if (TextUtils.isEmpty(picUrl)) picUrl = img.attr("src");
                }
                if (!TextUtils.isEmpty(picUrl) && picUrl.contains("pic.lzzypic.com")) {
                    picUrl = picUrl.replace("https://pic.lzzypic.com", "https://img.lzzyimg.com");
                }

                Element title = item.selectFirst(".sTit");
                Element remark = item.selectFirst(".sBottom");

                JSONObject vod = new JSONObject();
                vod.put("vod_id", vodId);
                vod.put("vod_name", title != null ? title.text() : "");
                vod.put("vod_pic", picUrl);
                vod.put("vod_remarks", remark != null ? remark.text() : "");
                videos.put(vod);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        result.put("list", videos);
        return result.toString();
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        JSONObject result = new JSONObject();
        JSONArray videos = new JSONArray();

        try {
            String url;
            if ("4-dm".equals(tid)) {
                url = "https://www.wwgz.cn/vod-list-id-4-pg-" + pg + "-order--by-hits-class-0-year-0-letter--area-大陆-lang-.html";
            } else {
                url = host + "/vod-list-id-" + tid + "-pg-" + pg + ".html";
            }

            String html = OkHttp.string(url, headers);  // 返回 String
            Document doc = Jsoup.parse(html);
            Elements items = doc.select(".globalPicList li");
            for (Element item : items) {
                Element aTag = item.selectFirst("a");
                if (aTag == null) continue;

                Element img = item.selectFirst("img");
                String picUrl = "";
                if (img != null) {
                    picUrl = img.attr("data-echo");
                    if (TextUtils.isEmpty(picUrl)) picUrl = img.attr("data-src");
                    if (TextUtils.isEmpty(picUrl)) picUrl = img.attr("src");
                }
                if (!TextUtils.isEmpty(picUrl) && picUrl.contains("pic.lzzypic.com")) {
                    picUrl = picUrl.replace("https://pic.lzzypic.com", "https://img.lzzyimg.com");
                }

                Element title = item.selectFirst(".sTit");
                Element remark = item.selectFirst(".sBottom");

                JSONObject vod = new JSONObject();
                vod.put("vod_id", host + aTag.attr("href"));
                vod.put("vod_name", title != null ? title.text() : "");
                vod.put("vod_pic", picUrl);
                vod.put("vod_remarks", remark != null ? remark.text() : "");
                videos.put(vod);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        result.put("list", videos);
        result.put("page", Integer.parseInt(pg));
        result.put("pagecount", 9999);
        result.put("limit", 90);
        result.put("total", 999999);
        return result.toString();
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        JSONObject result = new JSONObject();
        JSONArray list = new JSONArray();

        try {
            String url = ids.get(0);
            String html = OkHttp.string(url, headers);  // 返回 String
            Document doc = Jsoup.parse(html);

            List<String> playFrom = new ArrayList<>();
            List<String> playUrls = new ArrayList<>();

            Element tabBox = doc.selectFirst("#leftTabBox");
            if (tabBox != null) {
                Elements tabs = tabBox.select("ul li");
                for (Element tab : tabs) {
                    playFrom.add(tab.text());
                }

                Elements numLists = tabBox.select(".numList");
                for (Element numList : numLists) {
                    Elements liItems = numList.select("li");
                    List<String> episodes = new ArrayList<>();
                    // 反转顺序
                    for (int i = liItems.size() - 1; i >= 0; i--) {
                        Element ep = liItems.get(i);
                        Element aTag = ep.selectFirst("a");
                        if (aTag != null) {
                            episodes.add(aTag.text() + "$" + host + aTag.attr("href"));
                        }
                    }
                    playUrls.add(TextUtils.join("#", episodes));
                }
            }

            Element h1 = doc.selectFirst("h1 a");
            Element year = doc.selectFirst("span:contains(年代：)");
            Element actor = doc.selectFirst(".sDes:contains(主演:)");
            Element content = doc.selectFirst(".detail-con p");

            JSONObject vod = new JSONObject();
            vod.put("vod_name", h1 != null ? h1.text() : "");
            vod.put("vod_year", year != null ? year.text().replace("年代：", "").trim() : "");
            vod.put("vod_area", "");
            vod.put("vod_actor", actor != null ? actor.text().replace("主演:", "").trim() : "");
            vod.put("vod_director", "");
            vod.put("vod_content", content != null ? content.text().replace("简介:", "").trim() : "");
            vod.put("vod_play_from", TextUtils.join("$$$", playFrom));
            vod.put("vod_play_url", TextUtils.join("$$$", playUrls));

            list.put(vod);
        } catch (Exception e) {
            e.printStackTrace();
        }

        result.put("list", list);
        return result.toString();
    }

    @Override
    public String searchContent(String key, boolean quick, String pg) throws Exception {
        JSONObject result = new JSONObject();
        JSONArray videos = new JSONArray();

        try {
            String url = host + "/index.php?m=vod-search";
            Map<String, String> data = new HashMap<>();
            data.put("wd", key);

            // POST 请求返回 OkResult
            OkResult okResult = OkHttp.post(url, data, headers);
            if (okResult.getCode() == 200) {
                String html = okResult.getBody();
                Document doc = Jsoup.parse(html);
                Elements items = doc.select("#data_list li");
                for (Element item : items) {
                    Element aTag = item.selectFirst("a");
                    if (aTag == null) continue;

                    Element img = item.selectFirst(".lazyload");
                    String picUrl = img != null ? img.attr("data-src") : "";
                    if (!TextUtils.isEmpty(picUrl) && picUrl.contains("pic.lzzypic.com")) {
                        picUrl = picUrl.replace("https://pic.lzzypic.com", "https://img.lzzyimg.com");
                    }

                    Element title = item.selectFirst(".sTit");
                    Elements des = item.select(".sDes");
                    String remark = des.size() > 0 ? des.last().text() : "";

                    JSONObject vod = new JSONObject();
                    vod.put("vod_id", host + aTag.attr("href"));
                    vod.put("vod_name", title != null ? title.text() : "");
                    vod.put("vod_pic", picUrl);
                    vod.put("vod_remarks", remark);
                    videos.put(vod);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        result.put("list", videos);
        result.put("page", Integer.parseInt(pg));
        return result.toString();
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        JSONObject result = new JSONObject();

        try {
            if (id.contains("@")) {
                String[] parts = id.split("@");
                if (parts[0].isEmpty()) {
                    throw new Exception("未找到播放地址");
                }

                // 获取 JS 文件
                String jsUrl = host + "/player/" + parts[0] + ".js";
                String jsData = OkHttp.string(jsUrl, headers);  // 返回 String
                Matcher matcher = Pattern.compile("http.*?url=").matcher(jsData);
                if (!matcher.find()) {
                    throw new Exception("未找到解析地址");
                }
                String jxurl = matcher.group();

                // 获取播放数据
                String data = OkHttp.string(jxurl + parts[1], headers);  // 返回 String
                Matcher urlMatcher = Pattern.compile("http.*?url=").matcher(data);

                if (urlMatcher.find()) {
                    // 多条线路
                    JSONArray urlArr = new JSONArray();
                    int i = 1;
                    do {
                        String x = urlMatcher.group();
                        JSONObject js = new JSONObject();
                        js.put("jx", x);
                        js.put("id", parts[1]);
                        String purl = getProxyUrl() + "&wdict=" + e64(js.toString());
                        urlArr.put("线路" + i);
                        urlArr.put(purl);
                        i++;
                    } while (urlMatcher.find());
                    result.put("url", urlArr);
                } else {
                    // 单条线路
                    Matcher singleMatcher = Pattern.compile("url='(.*?)'").matcher(data);
                    if (singleMatcher.find()) {
                        result.put("url", singleMatcher.group(1));
                    } else {
                        throw new Exception("未找到播放地址");
                    }
                }
                result.put("parse", 0);
            } else {
                result.put("parse", 1);
                result.put("url", id);
            }
            result.put("header", new JSONObject(headers));
        } catch (Exception e) {
            e.printStackTrace();
            result.put("parse", 1);
            result.put("url", id);
            result.put("header", new JSONObject(headers));
        }

        return result.toString();
    }

    @Override
    public Object[] proxyLocal(Map<String, String> params) throws Exception {
        try {
            String wdictJson = d64(params.get("wdict"));
            JSONObject wdict = new JSONObject(wdictJson);
            String url = wdict.getString("jx") + wdict.getString("id");
            String html = OkHttp.string(url, headers);  // 返回 String
            Document doc = Jsoup.parse(html);
            Elements scripts = doc.select("script");
            String scriptText = scripts.last() != null ? scripts.last().html() : "";
            Matcher matcher = Pattern.compile("src=\"(.*?)\"").matcher(scriptText);
            if (matcher.find()) {
                String location = matcher.group(1);
                Map<String, String> redirectHeaders = new HashMap<>();
                redirectHeaders.put("Location", location);
                return new Object[]{302, "text/html", null, redirectHeaders};
            }
            return new Object[]{500, "text/plain", "未找到跳转地址".getBytes()};
        } catch (Exception e) {
            e.printStackTrace();
            return new Object[]{500, "text/plain", e.getMessage().getBytes()};
        }
    }

    // ========== 工具方法 ==========
    private String e64(String text) {
        return Base64.encodeToString(text.getBytes(), Base64.NO_WRAP);
    }

    private String d64(String encodedText) {
        return new String(Base64.decode(encodedText, Base64.NO_WRAP));
    }

    private String getProxyUrl() {
        return "http://127.0.0.1:9978/proxy?do=local";
    }

    @Override
    public boolean isVideoFormat(String url) throws Exception {
        return url != null && (url.endsWith(".m3u8") || url.endsWith(".mp4") || url.endsWith(".ts"));
    }

    @Override
    public void destroy() {
    }
}