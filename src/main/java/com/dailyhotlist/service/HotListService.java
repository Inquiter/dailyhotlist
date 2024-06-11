package com.dailyhotlist.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dailyhotlist.mapper.HotListDataMapper;
import com.dailyhotlist.mapper.HotListMapper;
import com.dailyhotlist.mapper.SubscribeMapper;
import com.dailyhotlist.model.HotList;
import com.dailyhotlist.model.HotListData;
import com.dailyhotlist.vo.HotListDataVo;
import com.dailyhotlist.vo.HotListVo;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class HotListService {
    @Resource
    public HotListMapper hotListMapper;

    @Resource
    public HotListDataMapper hotListDataMapper;

    @Resource
    public SubscribeMapper subscribeMapper;

    //每小时更新一次。
    //更新数据库中的dailyhotlist_hotlist表和dailyhotlist_hotlist_data表中的数据。
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void initDatabaseHotListAndHotListData() {
        Date start = new Date();
        List<HotList> hotListList = selectHotListDataFromOnline();
        subscribeMapper.closeForeignKey();
        hotListDataMapper.deleteHotListData();
        hotListDataMapper.resetAutoIncrement();
        hotListMapper.deleteHotList();
        hotListMapper.resetAutoIncrement();
        subscribeMapper.openForeignKey();
        //向dailyhotlist_hotlist表中写入数据。
        for (HotList hotList : hotListList)
            if (hotList.getHotListName() != null && hotList.getHotListUrl() != null) {
                hotListMapper.insertHotList(new HotList(hotList.getId(), encode(hotList.getHotListName()), encode(hotList.getHotListUrl()), null));
                //向dailyhotlist_hotlist_data表中写入数据。
                for (HotListData hotListData : hotList.getHotListDataList())
                    if (hotListData.getHotListDataId() != null && hotListData.getHotListDataUrl() != null && hotListData.getHotListDataTitle() != null && hotListData.getHotListName() != null)
                        hotListDataMapper.insertHotListData(new HotListData(hotListData.getId(), encode(hotListData.getHotListDataId()), encode(hotListData.getHotListDataUrl()), encode(hotListData.getHotListDataTitle()), hotListData.getHotListDataSubTitle() == null ? "" : encode(hotListData.getHotListDataSubTitle()), hotListData.getHotListDataHeat() == null ? "" : encode(hotListData.getHotListDataHeat()), hotListData.getHotListDataImageUrl() == null ? "" : encode(hotListData.getHotListDataImageUrl()), encode(hotListData.getHotListName())));
            }
        Date end = new Date();
        System.out.println("更新时间: ".concat(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(end)).concat("\t用时: " + (end.getTime() - start.getTime()) / 60000 % 60).concat("分" + (end.getTime() - start.getTime()) / 1000 % 60).concat("钞\t热榜数据更新完成!"));
    }

    //在线获取热榜数据。
    private List<HotList> selectHotListDataFromOnline() {
        List<HotList> hotListList = selectHotListFromDatabase();
        for (HotList hotList : hotListList) {
            hotList.setHotListDataList(new ArrayList<>());
            String imageUrl;
            switch (hotList.getHotListName()) {
                case "知乎": {
                    try {
                        JSONArray jsonArray = JSONObject.parseObject(Objects.requireNonNull(Jsoup.parse(new URL(hotList.getHotListUrl()), 30000).getElementById("js-initialData")).html()).getJSONObject("initialState").getJSONObject("topstory").getJSONArray("hotList");
                        for (int i = 0; i < jsonArray.size(); i++) {
                            HotListData hotListData = new HotListData();
                            JSONObject jsonObject = jsonArray.getJSONObject(i).getJSONObject("target");
                            hotListData.setHotListDataId(String.valueOf(i + 1));
                            hotListData.setHotListDataUrl(jsonObject.getJSONObject("link").getString("url"));
                            hotListData.setHotListDataTitle(jsonObject.getJSONObject("titleArea").getString("text"));
                            hotListData.setHotListDataSubTitle(jsonObject.getJSONObject("excerptArea").getString("text"));
                            hotListData.setHotListDataHeat(jsonObject.getJSONObject("metricsArea").getString("text"));
                            imageUrl = jsonObject.getJSONObject("imageArea").getString("url");
                            if (imageUrl != null && imageUrl.trim().length() > 0) {
                                hotListData.setHotListDataImageUrl("src\\main\\resources\\static\\img\\hot_list_data\\".concat(hotList.getHotListName()).concat("-").concat(hotListData.getHotListDataId()).concat(".jpg"));
                                downloadImage(imageUrl, hotListData);
                            }
                            hotListData.setHotListName(hotList.getHotListName());
                            hotList.getHotListDataList().add(hotListData);
                        }
                    } catch (IOException e) {
                        System.out.println(hotList.getHotListName().concat(" 数据爬取异常!请联系开发人员!"));
                    }
                    sortHotListData(hotList.getHotListDataList());
                    break;
                }
                case "微博": {
                    try {
                        StringBuilder hotListContent = getHotListContentForJSONObject(hotList.getHotListUrl());
                        JSONArray jsonArray = JSONObject.parseObject(hotListContent.toString()).getJSONObject("data").getJSONArray("realtime");
                        for (int i = 0; i < jsonArray.size(); i++) {
                            HotListData hotListData = new HotListData();
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            hotListData.setHotListDataId(String.valueOf(i + 1));
                            hotListData.setHotListDataUrl("https://s.weibo.com/weibo?q=%23".concat(jsonObject.getString("note")).concat("%23"));
                            hotListData.setHotListDataTitle(jsonObject.getString("note"));
                            hotListData.setHotListDataHeat(jsonObject.getString("num").concat("热度"));
                            hotListData.setHotListName(hotList.getHotListName());
                            hotList.getHotListDataList().add(hotListData);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(hotList.getHotListName().concat(" 数据爬取异常!请联系开发人员!"));
                    }
                    sortHotListData(hotList.getHotListDataList());
                    break;
                }
                case "虎扑": {
                    try {
                        Elements elements = Jsoup.parse(new URL(hotList.getHotListUrl()), 30000).getElementsByClass("bbs-sl-web-post").get(0).getElementsByClass("bbs-sl-web-post-body");
                        for (int i = 0; i < elements.size(); i++) {
                            HotListData hotListData = new HotListData();
                            hotListData.setHotListDataId(String.valueOf(i + 1));
                            hotListData.setHotListDataUrl("https://bbs.hupu.com".concat(elements.get(i).getElementsByClass("p-title").get(0).attr("href")));
                            hotListData.setHotListDataTitle(elements.get(i).getElementsByClass("p-title").get(0).text());
                            hotListData.setHotListDataHeat(elements.get(i).getElementsByClass("post-datum").get(0).text().split("/")[1].trim().concat("热度"));
                            hotListData.setHotListName(hotList.getHotListName());
                            hotList.getHotListDataList().add(hotListData);
                        }
                    } catch (IOException e) {
                        System.out.println(hotList.getHotListName().concat(" 数据爬取异常!请联系开发人员!"));
                    }
                    sortHotListData(hotList.getHotListDataList());
                    break;
                }
                case "贴吧": {
                    try {
                        StringBuilder hotListContent = getHotListContentForJSONObject(hotList.getHotListUrl());
                        JSONArray jsonArray = JSONObject.parseObject(hotListContent.toString()).getJSONObject("data").getJSONObject("bang_topic").getJSONArray("topic_list");
                        for (int i = 0; i < jsonArray.size(); i++) {
                            HotListData hotListData = new HotListData();
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            hotListData.setHotListDataId(String.valueOf(i + 1));
                            hotListData.setHotListDataUrl(jsonObject.getString("topic_url"));
                            hotListData.setHotListDataTitle(jsonObject.getString("topic_name"));
                            hotListData.setHotListDataSubTitle(jsonObject.getString("topic_desc"));
                            hotListData.setHotListDataHeat(jsonObject.getString("discuss_num").concat("热度"));
                            imageUrl = jsonObject.getString("topic_pic");
                            if (imageUrl != null && imageUrl.trim().length() > 0) {
                                hotListData.setHotListDataImageUrl("src\\main\\resources\\static\\img\\hot_list_data\\".concat(hotList.getHotListName()).concat("-").concat(hotListData.getHotListDataId()).concat(".jpg"));
                                downloadImage(imageUrl, hotListData);
                            }
                            hotListData.setHotListName(hotList.getHotListName());
                            hotList.getHotListDataList().add(hotListData);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(hotList.getHotListName().concat(" 数据爬取异常!请联系开发人员!"));
                    }
                    sortHotListData(hotList.getHotListDataList());
                    break;
                }
                case "百度": {
                    try {
                        StringBuilder hotListContent = getHotListContentForJSONObject(hotList.getHotListUrl());
                        JSONArray jsonArray = JSONObject.parseObject(hotListContent.toString()).getJSONObject("data").getJSONArray("cards").getJSONObject(0).getJSONArray("content");
                        for (int i = 0; i < jsonArray.size(); i++) {
                            HotListData hotListData = new HotListData();
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            hotListData.setHotListDataId(String.valueOf(i + 1));
                            hotListData.setHotListDataUrl(jsonObject.getString("url"));
                            hotListData.setHotListDataTitle(jsonObject.getString("word"));
                            hotListData.setHotListDataSubTitle(jsonObject.getString("desc"));
                            hotListData.setHotListDataHeat(jsonObject.getString("hotScore").concat("热搜指数"));
                            imageUrl = jsonObject.getString("img");
                            if (imageUrl != null && imageUrl.trim().length() > 0) {
                                hotListData.setHotListDataImageUrl("src\\main\\resources\\static\\img\\hot_list_data\\".concat(hotList.getHotListName()).concat("-").concat(hotListData.getHotListDataId()).concat(".jpg"));
                                downloadImage(imageUrl, hotListData);
                            }
                            hotListData.setHotListName(hotList.getHotListName());
                            hotList.getHotListDataList().add(hotListData);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(hotList.getHotListName().concat(" 数据爬取异常!请联系开发人员!"));
                    }
                    sortHotListData(hotList.getHotListDataList());
                    break;
                }
                case "虎嗅": {
                    try {
                        StringBuilder hotListContent = getHotListContentForJSONObject(hotList.getHotListUrl());
                        JSONArray jsonArray = JSONObject.parseObject(hotListContent.toString()).getJSONObject("data").getJSONArray("dataList");
                        for (int i = 0; i < jsonArray.size(); i++) {
                            HotListData hotListData = new HotListData();
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            hotListData.setHotListDataId(String.valueOf(i + 1));
                            hotListData.setHotListDataUrl(jsonObject.getString("share_url").replaceFirst("m.huxiu", "www.huxiu"));
                            hotListData.setHotListDataTitle(jsonObject.getString("title"));
                            hotListData.setHotListDataSubTitle(jsonObject.getString("summary"));
                            hotListData.setHotListDataHeat(jsonObject.getJSONObject("user_info").getString("username"));
                            imageUrl = jsonObject.getString("pic_path");
                            if (imageUrl.trim().length() > 0) {
                                hotListData.setHotListDataImageUrl("src\\main\\resources\\static\\img\\hot_list_data\\".concat(hotList.getHotListName()).concat("-").concat(hotListData.getHotListDataId()).concat(".jpg"));
                                downloadImage(imageUrl, hotListData);
                            }
                            hotListData.setHotListName(hotList.getHotListName());
                            hotList.getHotListDataList().add(hotListData);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(hotList.getHotListName().concat(" 数据爬取异常!请联系开发人员!"));
                    }
                    break;
                }
                case "豆瓣": {
                    try {
                        Elements elements = Jsoup.parse(new URL(hotList.getHotListUrl()), 30000).getElementsByClass("article").get(0).getElementsByClass("channel-item");
                        for (int i = 0; i < elements.size(); i++) {
                            HotListData hotListData = new HotListData();
                            hotListData.setHotListDataId(String.valueOf(i + 1));
                            hotListData.setHotListDataUrl(elements.get(i).getElementsByClass("bd").get(0).getElementsByTag("h3").get(0).getElementsByTag("a").get(0).attr("href"));
                            hotListData.setHotListDataTitle(elements.get(i).getElementsByClass("bd").get(0).getElementsByTag("h3").get(0).text());
                            hotListData.setHotListDataSubTitle(elements.get(i).getElementsByClass("block").get(0).getElementsByTag("p").get(0).text());
                            imageUrl = elements.get(i).getElementsByClass("likes").get(0).text();
                            hotListData.setHotListDataHeat(imageUrl.substring(0, imageUrl.indexOf(" ")).concat("热度"));
                            if (elements.get(i).getElementsByClass("block").get(0).getElementsByClass("pic-wrap").size() > 0) {
                                imageUrl = elements.get(i).getElementsByClass("block").get(0).getElementsByClass("pic-wrap").get(0).getElementsByTag("img").get(0).attr("src");
                                if (imageUrl.trim().length() > 0) {
                                    hotListData.setHotListDataImageUrl("src\\main\\resources\\static\\img\\hot_list_data\\".concat(hotList.getHotListName()).concat("-").concat(hotListData.getHotListDataId()).concat(".jpg"));
                                    downloadImage(imageUrl, hotListData);
                                }
                            }
                            hotListData.setHotListName(hotList.getHotListName());
                            hotList.getHotListDataList().add(hotListData);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println(hotList.getHotListName().concat(" 数据爬取异常!请联系开发人员!"));
                    }
                    sortHotListData(hotList.getHotListDataList());
                    break;
                }
                case "今日头条": {
                    try {
                        StringBuilder hotListContent = getHotListContentForJSONObject(hotList.getHotListUrl());
                        JSONArray jsonArray = JSONObject.parseObject(hotListContent.toString()).getJSONArray("data");
                        for (int i = 0; i < jsonArray.size(); i++) {
                            HotListData hotListData = new HotListData();
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            hotListData.setHotListDataId(String.valueOf(i + 1));
                            hotListData.setHotListDataUrl(jsonObject.getString("Url"));
                            hotListData.setHotListDataTitle(jsonObject.getString("Title"));
                            hotListData.setHotListDataSubTitle(jsonObject.getString("QueryWord"));
                            hotListData.setHotListDataHeat(jsonObject.getString("HotValue").concat("热度"));
                            imageUrl = jsonObject.getJSONObject("Image").getString("url");
                            if (imageUrl != null && imageUrl.trim().length() > 0) {
                                hotListData.setHotListDataImageUrl("src\\main\\resources\\static\\img\\hot_list_data\\".concat(hotList.getHotListName()).concat("-").concat(hotListData.getHotListDataId()).concat(".jpg"));
                                downloadImage(imageUrl, hotListData);
                            }
                            hotListData.setHotListName(hotList.getHotListName());
                            hotList.getHotListDataList().add(hotListData);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(hotList.getHotListName().concat(" 数据爬取异常!请联系开发人员!"));
                    }
                    sortHotListData(hotList.getHotListDataList());
                    break;
                }
                case "网易新闻": {
                    try {
                        StringBuilder hotListContent = getHotListContentForJSONObject(hotList.getHotListUrl());
                        JSONArray jsonArray = JSONObject.parseObject(hotListContent.toString()).getJSONArray("T1467284926140");
                        for (int i = 0, j = 1; i < jsonArray.size(); i++) {
                            HotListData hotListData = new HotListData();
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            if (jsonObject.getString("url") != null) {
                                hotListData.setHotListDataId(String.valueOf(j++));
                                hotListData.setHotListDataUrl(jsonObject.getString("url"));
                                hotListData.setHotListDataTitle(jsonObject.getString("title"));
                                hotListData.setHotListDataSubTitle(jsonObject.getString("digest"));
                                hotListData.setHotListDataHeat(jsonObject.getString("priority").concat("热度"));
                                imageUrl = jsonObject.getString("imgsrc").replaceFirst("http,", "http:");
                                if (imageUrl.trim().length() > 0) {
                                    hotListData.setHotListDataImageUrl("src\\main\\resources\\static\\img\\hot_list_data\\".concat(hotList.getHotListName()).concat("-").concat(hotListData.getHotListDataId()).concat(".jpg"));
                                    downloadImage(imageUrl, hotListData);
                                }
                                hotListData.setHotListName(hotList.getHotListName());
                                hotList.getHotListDataList().add(hotListData);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(hotList.getHotListName().concat(" 数据爬取异常!请联系开发人员!"));
                    }
                    sortHotListData(hotList.getHotListDataList());
                    break;
                }
                case "澎湃新闻": {
                    try {
                        StringBuilder hotListContent = getHotListContentForJSONObject(hotList.getHotListUrl());
                        JSONArray jsonArray = JSONObject.parseObject(hotListContent.toString()).getJSONObject("data").getJSONArray("hotNews");
                        for (int i = 0; i < jsonArray.size(); i++) {
                            HotListData hotListData = new HotListData();
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            hotListData.setHotListDataId(String.valueOf(i + 1));
                            hotListData.setHotListDataUrl("https://www.thepaper.cn/newsDetail_forward_".concat(jsonObject.getString("contId")));
                            hotListData.setHotListDataTitle(jsonObject.getString("name"));
                            hotListData.setHotListDataSubTitle(jsonObject.getJSONObject("nodeInfo").getString("desc"));
                            hotListData.setHotListDataHeat(jsonObject.getString("praiseTimes").concat("热度"));
                            imageUrl = jsonObject.getString("pic");
                            if (imageUrl.trim().length() > 0) {
                                hotListData.setHotListDataImageUrl("src\\main\\resources\\static\\img\\hot_list_data\\".concat(hotList.getHotListName()).concat("-").concat(hotListData.getHotListDataId()).concat(".jpg"));
                                downloadImage(imageUrl, hotListData);
                            }
                            hotListData.setHotListName(hotList.getHotListName());
                            hotList.getHotListDataList().add(hotListData);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(hotList.getHotListName().concat(" 数据爬取异常!请联系开发人员!"));
                    }
                    sortHotListData(hotList.getHotListDataList());
                    break;
                }
                case "观风闻": {
                    try {
                        Elements elements = Jsoup.parse(new URL(hotList.getHotListUrl()), 30000).getElementsByClass("fengwen-most-hot").get(0).getElementsByTag("li");
                        for (int i = 0; i < elements.size(); i++) {
                            HotListData hotListData = new HotListData();
                            hotListData.setHotListDataId(String.valueOf(i + 1));
                            hotListData.setHotListDataUrl("https://user.guancha.cn".concat(elements.get(i).getElementsByTag("a").attr("href")));
                            hotListData.setHotListDataTitle(elements.get(i).getElementsByTag("a").text());
                            hotListData.setHotListDataHeat(elements.get(i).getElementsByClass("view-counts").text().concat("热度"));
                            hotListData.setHotListName(hotList.getHotListName());
                            hotList.getHotListDataList().add(hotListData);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println(hotList.getHotListName().concat(" 数据爬取异常!请联系开发人员!"));
                    }
                    sortHotListData(hotList.getHotListDataList());
                    break;
                }
                case "少数派": {
                    try {
                        StringBuilder hotListContent = getHotListContentForJSONObject(hotList.getHotListUrl());
                        JSONArray jsonArray = JSONObject.parseObject(hotListContent.toString()).getJSONArray("data");
                        for (int i = 0; i < jsonArray.size(); i++) {
                            HotListData hotListData = new HotListData();
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            hotListData.setHotListDataId(String.valueOf(i + 1));
                            hotListData.setHotListDataUrl("https://sspai.com/post/".concat(jsonObject.getString("id")));
                            hotListData.setHotListDataTitle(jsonObject.getString("title"));
                            hotListData.setHotListDataSubTitle(jsonObject.getString("summary"));
                            hotListData.setHotListDataHeat(jsonObject.getString("like_count").concat("热度"));
                            hotListData.setHotListName(hotList.getHotListName());
                            hotList.getHotListDataList().add(hotListData);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(hotList.getHotListName().concat(" 数据爬取异常!请联系开发人员!"));
                    }
                    sortHotListData(hotList.getHotListDataList());
                    break;
                }
                case "稀土掘金": {
                    try {
                        StringBuilder hotListContent = getHotListContentForJSONObject(hotList.getHotListUrl());
                        JSONArray jsonArray = JSONObject.parseObject(hotListContent.toString()).getJSONArray("data");
                        for (int i = 0; i < jsonArray.size(); i++) {
                            HotListData hotListData = new HotListData();
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            hotListData.setHotListDataId(String.valueOf(i + 1));
                            hotListData.setHotListDataUrl("https://juejin.cn/post/".concat(jsonObject.getJSONObject("content").getString("content_id")));
                            hotListData.setHotListDataTitle(jsonObject.getJSONObject("content").getString("title"));
                            hotListData.setHotListDataSubTitle("作者：".concat(jsonObject.getJSONObject("author").getString("name")));
                            hotListData.setHotListDataHeat(jsonObject.getJSONObject("content_counter").getString("view").concat("热度"));
                            hotListData.setHotListName(hotList.getHotListName());
                            hotList.getHotListDataList().add(hotListData);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(hotList.getHotListName().concat(" 数据爬取异常!请联系开发人员!"));
                    }
                    sortHotListData(hotList.getHotListDataList());
                    break;
                }
                case "IT之家": {
                    try {
                        Elements elements = Jsoup.parse(new URL(hotList.getHotListUrl()), 30000).getElementsByClass("t-b sel clearfix").get(0).getElementsByTag("a");
                        for (int i = 0; i < elements.size(); i++) {
                            HotListData hotListData = new HotListData();
                            hotListData.setHotListDataId(String.valueOf(i + 1));
                            hotListData.setHotListDataUrl(elements.get(i).attr("href"));
                            hotListData.setHotListDataTitle(elements.get(i).text());
                            hotListData.setHotListName(hotList.getHotListName());
                            hotList.getHotListDataList().add(hotListData);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println(hotList.getHotListName().concat(" 数据爬取异常!请联系开发人员!"));
                    }
                    break;
                }
                default:
                    break;
            }
        }
        return hotListList;
    }

    //从文件中读取热榜信息，主要用于初始化数据库中的热榜平台信息。
    private List<HotList> selectHotListFromFile() {
        List<HotList> hotListList = new ArrayList<>();
        RandomAccessFile raf = null;
        String[] s;
        try {
            raf = new RandomAccessFile("src\\main\\resources\\static\\file\\hotListData.txt", "r");
            String line;
            for (int i = 0; (line = raf.readLine()) != null; i++)
                if (line.trim().length() > 0) {
                    s = new String(line.trim().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8).split("\\s+");
                    HotList hotList = new HotList();
                    hotList.setId(i + 1);
                    hotList.setHotListName(s[0]);
                    hotList.setHotListUrl(s[1]);
                    hotListList.add(hotList);
                }
        } catch (IOException e) {
            System.out.println("文件读取异常!请联系开发人员!");
            throw new RuntimeException(e);
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    System.out.println("读取文件流关闭异常!请联系开发人员!");
                }
            }
        }
        return hotListList;
    }

    //从数据库中读取热榜信息，用于持续处理数据库中的热榜平台数据。
    private List<HotList> selectHotListFromDatabase() {
        List<HotList> hotListList = hotListMapper.selectAllHotList();
        for (HotList hotList : hotListList) {
            hotList.setHotListName(decrypt(hotList.getHotListName()));
            hotList.setHotListUrl(decrypt(hotList.getHotListUrl()));
        }
        return hotListList;
    }

    //根据url获取网络json文件的内容。
    private StringBuilder getHotListContentForJSONObject(String url) {
        StringBuilder hotListContent = new StringBuilder();
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/119.0");
            connection.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) hotListContent.append(inputLine);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("网页".concat(url).concat("爬取失败!请联系开发人员!"));
        }
        return hotListContent;
    }

    //从网络下载图片到本地。
    private void downloadImage(String imageUrl, HotListData hotListData) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/119.0");
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10 * 1000);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 1024];
            int len;
            while ((len = connection.getInputStream().read(buffer)) != -1) outStream.write(buffer, 0, len);
            connection.getInputStream().close();
            byte[] data = outStream.toByteArray();
            FileOutputStream outputStream = new FileOutputStream(new File(hotListData.getHotListDataImageUrl()));
            outputStream.write(data);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("图片".concat(imageUrl).concat("下载失败!请联系开发人员!"));
        }
    }

    //对热榜数据进行排序。
    private void sortHotListData(List<HotListData> hotListDataList) {
        for (int i = hotListDataList.size() - 1; i > 0; i--)
            for (int j = 0; j < i; j++) {
                String heat1 = hotListDataList.get(j).getHotListDataHeat();
                String heat2 = hotListDataList.get(j + 1).getHotListDataHeat();
                heat1 = heat1.substring(0, heat1.indexOf("热"));
                heat2 = heat2.substring(0, heat2.indexOf("热"));
                if (heat1.trim().length() == 0) heat1 = "0";
                if (heat2.trim().length() == 0) heat2 = "0";
                if ((heat1.contains("万") ? (Float.parseFloat(heat1.substring(0, heat1.indexOf("万")).trim()) * 10000) : Float.parseFloat(heat1.trim())) < (heat2.contains("万") ? (Float.parseFloat(heat2.substring(0, heat2.indexOf("万")).trim()) * 10000) : Float.parseFloat(heat2.trim()))) {
                    HotListData hotListData = hotListDataList.get(j);
                    hotListDataList.set(j, hotListDataList.get(j + 1));
                    hotListDataList.set(j + 1, hotListData);
                }
            }
        for (int i = 0; i < hotListDataList.size(); i++) hotListDataList.get(i).setHotListDataId(String.valueOf(i + 1));
    }

    //从数据库中获取热榜数据。
    public List<HotListVo> selectHotListDataFromDatabase() {
        List<HotListVo> hotListVoList = new ArrayList<>();
        List<HotListDataVo> hotListDataVoList;
        List<HotList> hotListList = hotListMapper.selectHotList();
        for (HotList hotList : hotListList) {
            hotListVoList.add(new HotListVo(hotList.getId(), decrypt(hotList.getHotListName()), decrypt(hotList.getHotListUrl()), new ArrayList<>()));
            hotListDataVoList = new ArrayList<>();
            for (HotListData hotListData : hotList.getHotListDataList())
                if (hotListData.getHotListDataId() != null && hotListData.getHotListDataUrl() != null && hotListData.getHotListDataTitle() != null && hotListData.getHotListName() != null)
                    hotListDataVoList.add(new HotListDataVo(hotListData.getId(), decrypt(hotListData.getHotListDataId()), decrypt(hotListData.getHotListDataUrl()), decrypt(hotListData.getHotListDataTitle()), decrypt(hotListData.getHotListDataSubTitle()), decrypt(hotListData.getHotListDataHeat()), decrypt(hotListData.getHotListDataImageUrl()), decrypt(hotListData.getHotListName())));
            hotListVoList.get(hotListVoList.size() - 1).setHotListDataVoList(hotListDataVoList);
        }
        showAllHotListData(hotListVoList);
        return hotListVoList;
    }

    //展示全站热榜数据。
    private void showAllHotListData(List<HotListVo> hotListVoList) {
        List<String> hotListNameList = new ArrayList<>();
        hotListNameList.add("知乎");
        hotListNameList.add("微博");
        hotListNameList.add("虎扑");
        hotListNameList.add("贴吧");
        hotListNameList.add("百度");
        hotListNameList.add("豆瓣");
        hotListNameList.add("今日头条");
        hotListNameList.add("网易新闻");
        hotListNameList.add("澎湃新闻");
        hotListNameList.add("观风闻");
        hotListVoList.add(0, new HotListVo(0, "全站", "http://localhost:8080/", new ArrayList<>()));
        List<HotListDataVo> hotListDataVoList = hotListVoList.get(0).getHotListDataVoList();
        for (HotListVo hotListVo : hotListVoList)
            if (hotListNameList.contains(hotListVo.getHotListName()))
                for (int i = 0; i < 10 && i < hotListVo.getHotListDataVoList().size(); i++)
                    hotListDataVoList.add(new HotListDataVo(hotListVo.getHotListDataVoList().get(i).getId(), hotListVo.getHotListDataVoList().get(i).getHotListDataId(), hotListVo.getHotListDataVoList().get(i).getHotListDataUrl(), hotListVo.getHotListDataVoList().get(i).getHotListDataTitle(), hotListVo.getHotListDataVoList().get(i).getHotListDataSubTitle(), hotListVo.getHotListDataVoList().get(i).getHotListDataHeat(), hotListVo.getHotListDataVoList().get(i).getHotListDataImageUrl(), hotListVo.getHotListDataVoList().get(i).getHotListName()));
        for (int i = hotListDataVoList.size() - 1; i > 0; i--)
            for (int j = 0; j < i; j++) {
                String heat1 = hotListDataVoList.get(j).getHotListDataHeat();
                String heat2 = hotListDataVoList.get(j + 1).getHotListDataHeat();
                heat1 = heat1.substring(0, heat1.indexOf("热"));
                heat2 = heat2.substring(0, heat2.indexOf("热"));
                if (heat1.trim().length() == 0) heat1 = "0";
                if (heat2.trim().length() == 0) heat2 = "0";
                if ((heat1.contains("万") ? (Float.parseFloat(heat1.substring(0, heat1.indexOf("万")).trim()) * 10000) : Float.parseFloat(heat1.trim())) < (heat2.contains("万") ? (Float.parseFloat(heat2.substring(0, heat2.indexOf("万")).trim()) * 10000) : Float.parseFloat(heat2.trim()))) {
                    HotListDataVo hotListDataVo = hotListDataVoList.get(j);
                    hotListDataVoList.set(j, hotListDataVoList.get(j + 1));
                    hotListDataVoList.set(j + 1, hotListDataVo);
                }
            }
        for (int i = 0; i < hotListDataVoList.size(); i++) hotListDataVoList.get(i).setHotListDataId(String.valueOf(i + 1));
    }

    public List<HotListVo> selectAllHotList() {
        List<HotListVo> hotListVoList = new ArrayList<>();
        for (HotList hotList : hotListMapper.selectHotList())
            hotListVoList.add(new HotListVo(hotList.getId(), decrypt(hotList.getHotListName()), decrypt(hotList.getHotListUrl()), new ArrayList<>()));
        return hotListVoList;
    }

    public HotListVo selectOneHotList(String hotListName) {
        HotList hotList = hotListMapper.selectOneHotList(encode(hotListName));
        return hotList == null ? null : new HotListVo(hotList.getId(), decrypt(hotList.getHotListName()), decrypt(hotList.getHotListUrl()), new ArrayList<>());
    }

    public boolean addHotList(HotList hotList) {
        hotList.setHotListName(encode(hotList.getHotListName()));
        hotList.setHotListUrl(encode(hotList.getHotListUrl()));
        return hotListMapper.insertHotList(hotList) == 1;
    }

    public boolean updateHotList(HotList hotList) {
        hotList.setHotListName(encode(hotList.getHotListName()));
        hotList.setHotListUrl(encode(hotList.getHotListUrl()));
        return hotListMapper.updateHotList(hotList) == 1;
    }

    public boolean deleteHotList(int id) {
        hotListMapper.closeForeignKey();
        int count = hotListMapper.deleteOneHotList(id);
        hotListMapper.resetAutoIncrement();
        hotListMapper.openForeignKey();
        return count == 1;
    }

    public String encode(String origin) {
        return Base64.getEncoder().encodeToString(origin.getBytes());
    }

    public String decrypt(String encode) {
        return new String(Base64.getDecoder().decode(encode));
    }
}
