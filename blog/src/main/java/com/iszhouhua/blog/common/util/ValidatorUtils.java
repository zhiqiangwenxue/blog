package com.iszhouhua.blog.common.util;

import com.iszhouhua.blog.common.constant.CodeEnum;
import com.iszhouhua.blog.common.exception.BlogException;
import com.iszhouhua.blog.common.storage.StorageConfig;
import com.iszhouhua.blog.common.storage.group.AliyunGroup;
import com.iszhouhua.blog.common.storage.group.LocalGroup;
import com.iszhouhua.blog.common.storage.group.QcloudGroup;
import com.iszhouhua.blog.common.storage.group.QiniuGroup;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static com.iszhouhua.blog.common.constant.StorageType.*;

/**
 * hibernate-validator校验工具类
 */
public class ValidatorUtils {
    private static Validator validator;

    static {
        validator = SpringUtils.getBean(Validator.class);
    }


    public static void isBlank(String str, String message) {
        if (StringUtils.isBlank(str)) {
            throw new BlogException(CodeEnum.VALIDATION_ERROR.getValue(), message);
        }
    }

    public static void isNull(Object obj, String message) {
        if (obj == null) {
            throw new BlogException(CodeEnum.VALIDATION_ERROR.getValue(), message);
        }
    }

    /**
     * 校验对象
     *
     * @param object 待校验对象
     * @param groups 待校验的组
     * @throws BlogException 校验不通过，则报BlogException异常
     */
    public static void validate(Object object, Class<?>... groups) throws BlogException {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
        if (!constraintViolations.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            for (ConstraintViolation<Object> constraint : constraintViolations) {
                msg.append(constraint.getMessage());
            }
            throw new BlogException(CodeEnum.VALIDATION_ERROR.getValue(), msg.toString());
        }
    }

    /**
     * 校验存储配置信息
     *
     * @param value 待校验的值
     * @throws BlogException 校验不通过，则报BlogException异常
     */
    public static void validateStorageConfig(String value) throws BlogException {
        StorageConfig storageConfig = JsonUtils.fromJson(value, StorageConfig.class);
        switch (storageConfig.getType()) {
            case QINIU:
                //校验七牛数据
                validate(storageConfig, QiniuGroup.class);
                break;
            case ALIYUN:
                //校验阿里云数据
                validate(storageConfig, AliyunGroup.class);
                break;
            case QCLOUD:
                //校验腾讯云数据
                validate(storageConfig, QcloudGroup.class);
                break;
            case LOCAL:
                //校验本地数据
                validate(storageConfig, LocalGroup.class);
                break;
            default:
                throw new BlogException(CodeEnum.VALIDATION_ERROR.getValue(), "无效的存储类型");
        }
    }
}
