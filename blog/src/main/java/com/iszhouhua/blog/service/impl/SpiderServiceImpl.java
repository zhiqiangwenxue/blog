package com.iszhouhua.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iszhouhua.blog.common.constant.Const;
import com.iszhouhua.blog.common.storage.OSSFactory;
import com.iszhouhua.blog.mapper.SpiderMapper;
import com.iszhouhua.blog.model.pojo.Article;
import com.iszhouhua.blog.model.pojo.Spider;
import com.iszhouhua.blog.service.ConfigService;
import com.iszhouhua.blog.service.SpiderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

/**
 * 爬虫规则服务实现类
 * @author ZhouHua
 * @date 2019/04/03
 */
@Slf4j
@Service
public class SpiderServiceImpl extends ServiceImpl<SpiderMapper, Spider> implements SpiderService {
    @Autowired
    private ConfigService configService;

    @Override
    public Article spiderArticle(Spider spider, String url) throws IOException {
        Article article = new Article();
        Document doc = Jsoup.connect(url).userAgent(Const.USER_AGENT).timeout(Const.SPIDER_TIMEOUT).get();
        // 获得标题
        String title = doc.select(spider.getTitleRule()).text();
        if (StringUtils.isEmpty(title)){
            return null;
        }
        article.setTitle(title);
        // 获得描述
        String description=doc.head().select("meta[name=description]").attr("content");
        article.setDescription(description);
        // 获得文章内容
        Element detail = doc.select(spider.getContentRule()).first();
        if (detail == null){
            return null;
        }
        downImage(detail);
        //去掉文章中所有的超链接，保留文字。
        String content=detail.html().replaceAll("<a(.*?)>", "").replaceAll("</a>", "");
        article.setContent(content);
        article.setContentMd(content);
        return article;
    }

    /**
     * 下载图片
     * @param content 文章
     * @return 下载图片之后的文章
     */
    private Element downImage(Element content) {
        // 获得所有图片
        Iterator<Element> iterator = content.getElementsByTag("img").iterator();
        while (iterator.hasNext()) {
            Element image=iterator.next();
            String url = image.attr("abs:src");
            InputStream inputStream = null;
            // 获得后缀
            String suffix ="."+StringUtils.substringAfterLast(url,".");
            // 开始下载图片
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestProperty("User-Agent", Const.USER_AGENT);
                connection.setConnectTimeout(Const.SPIDER_TIMEOUT);
                inputStream = connection.getInputStream();
                String imageUrl = OSSFactory.build().uploadSuffix(inputStream, suffix);
                image.attr("src", imageUrl);
            } catch (Exception e) {
                log.error("图片下载失败："+url,e);
                //删除失败的图片
                image.remove();
            } finally {
                try {
                    if (inputStream != null){
                        inputStream.close();
                    }
                } catch (IOException e) {
                    log.error("关闭输入流时出现异常",e);
                }
            }
        }
        return content;
    }
}
