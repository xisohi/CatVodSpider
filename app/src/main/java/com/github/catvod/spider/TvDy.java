package com.github.catvod.spider;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.util.Base64;

public class TvDy extends Spider {

    private static final String siteUrl = "https://www.tvdy.xyz";
    private static final String cateUrl = siteUrl + "/vodtype/";
    private static final String detailUrl = siteUrl + "/voddetail/";
    private static final String searchUrl = siteUrl + "/vodsearch/-------------.html?wd=";
    private static final String playUrl = siteUrl + "/vodplay/";

    private HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", Util.CHROME);
        headers.put("Referer", siteUrl);
        return headers;
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        List<Vod> list = new ArrayList<>();
        List<Class> classes = new ArrayList<>();
        String[] typeIdList = {"dianying", "dianshiju", "zongyi", "dongman", "tiyu"};
        String[] typeNameList = {"电影", "电视剧", "综艺", "动漫", "体育"};
        for (int i = 0; i < typeNameList.length; i++) {
            classes.add(new Class(typeIdList[i], typeNameList[i]));
        }

        Document doc = Jsoup.parse(OkHttp.string(siteUrl, getHeaders()));
        for (Element element : doc.select("a.stui-vodlist__thumb")) {
            try {
                String pic = element.attr("data-original");
                String url = element.attr("href");
                String name = element.attr("title");
                if (pic.isEmpty()) continue;
                if (!pic.startsWith("http")) {
                    pic = siteUrl + pic;
                }
                String id = extractId(url);
                if (id != null) {
                    list.add(new Vod(id, name, pic));
                }
            } catch (Exception e) {
                // 忽略
            }
        }
        return Result.string(classes, list);
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        List<Vod> list = new ArrayList<>();
        String target = cateUrl + tid + ".html";
        if (!pg.equals("1")) {
            target = cateUrl + tid + "-" + pg + ".html";
        }

        Document doc = Jsoup.parse(OkHttp.string(target, getHeaders()));
        for (Element element : doc.select("a.stui-vodlist__thumb")) {
            try {
                String pic = element.attr("data-original");
                String url = element.attr("href");
                String name = element.attr("title");
                if (pic.isEmpty()) continue;
                if (!pic.startsWith("http")) {
                    pic = siteUrl + pic;
                }
                String id = extractId(url);
                if (id != null) {
                    list.add(new Vod(id, name, pic));
                }
            } catch (Exception e) {
                // 忽略
            }
        }

        int currentPage = Integer.parseInt(pg);
        int pageSize = 20;
        int total = (currentPage + 1) * pageSize;
        return Result.string(currentPage, currentPage + 1, pageSize, total, list);
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        if (ids == null || ids.isEmpty()) return Result.string(new Vod());

        String detailId = ids.get(0);
        Document doc = Jsoup.parse(OkHttp.string(detailUrl.concat(detailId), getHeaders()));

        String name = doc.select("h1.title").text();
        if (name.isEmpty()) {
            name = doc.select(".title").text();
        }

        String pic = doc.select(".stui-content__thumb img.lazyload").attr("data-original");
        if (pic.isEmpty()) {
            pic = doc.select(".stui-content__thumb img").attr("src");
        }
        if (!pic.startsWith("http")) {
            pic = siteUrl + pic;
        }

        String year = "";
        try {
            Element titleFont = doc.select("h1.title font").first();
            if (titleFont != null) {
                String yearText = titleFont.text();
                // 4个反斜杠：Java字符串中\\d表示正则\d
                Matcher ym = Pattern.compile("(\\\\d{4})").matcher(yearText);
                if (ym.find()) year = ym.group(1);
            }
            if (year.isEmpty()) {
                Elements dataElements = doc.select("p.data");
                for (Element e : dataElements) {
                    if (e.text().contains("年份") || e.text().contains("更新")) {
                        Matcher ym2 = Pattern.compile("(\\\\d{4})").matcher(e.text());
                        if (ym2.find()) {
                            year = ym2.group(1);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) { /* 忽略 */ }

        String desc = doc.select("span.detail-content").text();
        if (desc.isEmpty()) {
            desc = doc.select(".detail-sketch").text();
        }

        Elements heads = doc.select("div.stui-vodlist__head");
        String playFrom = "";
        String playUrl = "";

        for (Element head : heads) {
            Element h4 = head.select("h4").first();
            if (h4 == null) continue;

            String tabName = h4.text().trim();
            if (tabName.contains("下载地址") || tabName.contains("猜你喜欢")) continue;

            tabName = tabName.replaceAll("<i.*?</i>", "").replaceAll("&nbsp;", " ").trim();
            if (tabName.isEmpty()) continue;

            if (!playFrom.isEmpty()) playFrom += "$$$";
            playFrom += tabName;

            Elements lis = head.select("ul.stui-content__playlist li a");
            String liUrl = "";
            for (Element link : lis) {
                String linkText = link.text().trim();
                String linkHref = link.attr("href");
                String playId = linkHref.replace("/vodplay/", "").replace(".html", "");
                if (!linkText.isEmpty() && !playId.isEmpty()) {
                    if (!liUrl.isEmpty()) liUrl += "#";
                    liUrl += linkText + "$" + playId;
                }
            }
            if (!liUrl.isEmpty()) {
                if (!playUrl.isEmpty()) playUrl += "$$$";
                playUrl += liUrl;
            }
        }

        Vod vod = new Vod();
        vod.setVodId(detailId);
        vod.setVodPic(pic);
        vod.setVodYear(year);
        vod.setVodName(name);
        vod.setVodContent(desc);
        vod.setVodPlayFrom(playFrom);
        vod.setVodPlayUrl(playUrl);
        return Result.string(vod);
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        List<Vod> list = new ArrayList<>();
        String target = searchUrl.concat(URLEncoder.encode(key, "UTF-8"));
        Document doc = Jsoup.parse(OkHttp.string(target, getHeaders()));

        for (Element element : doc.select("a.stui-vodlist__thumb")) {
            try {
                String pic = element.attr("data-original");
                String url = element.attr("href");
                String name = element.attr("title");
                if (pic.isEmpty()) continue;
                if (!pic.startsWith("http")) {
                    pic = siteUrl + pic;
                }
                String id = extractId(url);
                if (id != null) {
                    list.add(new Vod(id, name, pic));
                }
            } catch (Exception e) {
                // 忽略
            }
        }
        return Result.string(list);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        String target = playUrl.concat(id + ".html");
        Document doc = Jsoup.parse(OkHttp.string(target, getHeaders()));
        String html = doc.html();

        String videoUrl = "";

        // 方式1: 匹配 player_aaaa 变量，支持 encrypt:2 (双重URL编码)
        // 4个反斜杠：Java字符串中\\{表示正则\{，\\\"表示正则\"
        Pattern playerPattern = Pattern.compile("var player_aaaa=\\\\{.*?\\\\\"url\\\\\":\\\\\"(.*?)\\\\\".*?\\\\}");
        Matcher playerMatcher = playerPattern.matcher(html);
        if (playerMatcher.find()) {
            String encodedUrl = playerMatcher.group(1);
            // encrypt:2 是双重 URL 编码，需要解码两次
            try {
                String firstDecode = URLDecoder.decode(encodedUrl, "UTF-8");
                videoUrl = URLDecoder.decode(firstDecode, "UTF-8");
            } catch (Exception e) {
                videoUrl = encodedUrl;
            }
        }

        // 方式2: 旧的 base64 解密（兼容旧格式）
        if (videoUrl.isEmpty()) {
            Pattern pattern = Pattern.compile("var now=base64decode\\\\(['\"](.*?)['\"]\\\\)");
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                videoUrl = decodeBase64(matcher.group(1));
            }
        }

        // 方式3: 直接匹配m3u8或mp4链接
        if (videoUrl.isEmpty()) {
            Pattern urlPattern = Pattern.compile("(https?://[^\\s'\"]+\\.(m3u8|mp4)[^\\s'\"]*)");
            Matcher urlMatcher = urlPattern.matcher(html);
            if (urlMatcher.find()) {
                videoUrl = urlMatcher.group(1);
            }
        }

        // 方式4: iframe链接
        if (videoUrl.isEmpty()) {
            Element iframe = doc.select("iframe").first();
            if (iframe != null) {
                videoUrl = iframe.attr("src");
            }
        }

        if (!videoUrl.isEmpty()) {
            return Result.get().url(videoUrl).header(getHeaders()).string();
        }
        return Result.get().url("").string();
    }

    private String extractId(String url) {
        if (url == null || url.isEmpty()) return null;
        try {
            // 4个反斜杠：Java字符串中\\d表示正则\d
            Pattern pattern = Pattern.compile("/vod(?:detail|play)/(\\\\d+)");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            // 忽略
        }
        return null;
    }

    public static String decodeBase64(String encodedString) {
        try {
            return new String(Base64.decode(encodedString, Base64.DEFAULT));
        } catch (Exception e) {
            return encodedString;
        }
    }
}