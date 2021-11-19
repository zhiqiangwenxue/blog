package com.iszhouhua.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iszhouhua.blog.model.pojo.Log;

import java.util.List;
import java.util.Map;

/**
 * 访客日志表 服务类
 *
 * @author ZhouHua
 * @since 2018-12-01
 */
public interface LogService extends IService<Log> {

    /**
     * 获取最新的n条日志
     *
     * @param number 需要获取的条数
     * @return
     */
    List<Log> findLatestLog(int number);

    /**
     * 统计访客的浏览器类型
     *
     * @return
     */
    Map<String, Integer> statBrowser();

    /**
     * 统计访客的操作系统
     *
     * @return
     */
    Map<String, Integer> statOperatingSystem();

    /**
     * 统计访客的所在城市
     *
     * @return
     */
    List<Map<String, Integer>> statCity();

    /**
     * 添加日志
     *
     * @param log 日志
     */
    void addLog(Log log);

    List<Log> findListLog();
}
