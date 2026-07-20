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
        String[] typeNameList = {"\u7535\u5f71", "\u7535\u89c6\u5267", "\u7efc\u827a", "\u52a8\u6f2b", "\u4f53\u80b2"};
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
                // \u5ffd\u7565
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
                // \u5ffd\u7565
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
                Matcher ym = Pattern.compile("(\\d{4})").matcher(yearText);
                if (ym.find()) year = ym.group(1);
            }
            if (year.isEmpty()) {
                Elements dataElements = doc.select("p.data");
                for (Element e : dataElements) {
                    if (e.text().contains("\u5e74\u4efd") || e.text().contains("\u66f4\u65b0")) {
                        Matcher ym2 = Pattern.compile("(\\d{4})").matcher(e.text());
                        if (ym2.find()) {
                            year = ym2.group(1);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) { /* \u5ffd\u7565 */ }

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
            if (tabName.contains("\u4e0b\u8f7d\u5730\u5740") || tabName.contains("\u731c\u4f60\u559c\u6b22")) continue;

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
                // \u5ffd\u7565
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

        // \u65b9\u5f0f1: \u5339\u914d player_aaaa \u53d8\u91cf\uff0c\u652f\u6301 encrypt:2 (\u53cc\u91cdURL\u7f16\u7801)
        Pattern playerPattern = Pattern.compile("var player_aaaa=\\\\{.*?\\\\\"url\\\\\":\\\\\"(.*?)\\\\\".*?\\\\}");
        Matcher playerMatcher = playerPattern.matcher(html);
        if (playerMatcher.find()) {
            String encodedUrl = playerMatcher.group(1);
            // encrypt:2 \u662f\u53cc\u91cd URL \u7f16\u7801\uff0c\u9700\u8981\u89e3\u7801\u4e24\u6b21
            try {
                String firstDecode = URLDecoder.decode(encodedUrl, "UTF-8");
                videoUrl = URLDecoder.decode(firstDecode, "UTF-8");
            } catch (Exception e) {
                videoUrl = encodedUrl;
            }
        }

        // \u65b9\u5f0f2: \u65e7\u7684 base64 \u89e3\u5bc6\uff08\u517c\u5bb9\u65e7\u683c\u5f0f\uff09
        if (videoUrl.isEmpty()) {
            Pattern pattern = Pattern.compile("var now=base64decode\\\\(['\"](.*?)['\"]\\\\)");
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                videoUrl = decodeBase64(matcher.group(1));
            }
        }

        // \u65b9\u5f0f3: \u76f4\u63a5\u5339\u914dm3u8\u6216mp4\u94fe\u63a5
        if (videoUrl.isEmpty()) {
            Pattern urlPattern = Pattern.compile("(https?://[^\\s'\"]+\\.(m3u8|mp4)[^\\s'\"]*)");
            Matcher urlMatcher = urlPattern.matcher(html);
            if (urlMatcher.find()) {
                videoUrl = urlMatcher.group(1);
            }
        }

        // \u65b9\u5f0f4: iframe\u94fe\u63a5
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
            Pattern pattern = Pattern.compile("/vod(?:detail|play)/(\\d+)");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            // \u5ffd\u7565
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