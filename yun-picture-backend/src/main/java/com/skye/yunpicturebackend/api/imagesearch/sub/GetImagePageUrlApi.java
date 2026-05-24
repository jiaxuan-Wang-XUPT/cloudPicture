package com.skye.yunpicturebackend.api.imagesearch.sub;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.skye.yunpicturebackend.exception.BusinessException;
import com.skye.yunpicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GetImagePageUrlApi {

    //获取以图搜图的页面地址

    public static String getImagePageUrl(String imageUrl) {
        //1.准备请求参数(观察调用的api接收的是哪些参数)（F12中载荷记录）
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        //获取当前时间戳
        long uptime = System.currentTimeMillis();
        //请求地址
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;
        //2.发送post请求到百度接口
        try {
            HttpResponse response = HttpRequest.post(url)
                    // 这里需要指定acs-token 不然会响应系统异常
                    .form(formData)
                    .header("Acs-Token", RandomUtil.randomString(1))
                    .timeout(5000)
                    .execute();

            if (response.getStatus() != HttpStatus.HTTP_OK) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "调用接口失败");
            }
            //解析响应
            String responseBody = response.body();
            Map<String, Object> result = JSONUtil.toBean(responseBody, Map.class);

            //3.处理响应结果
            if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "调用接口失败");
            }

            Map<String, Object> data = (Map<String, Object>) result.get("data");
            String rawUrl = (String) data.get("url");
            //对url进行解码
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            if (searchResultUrl == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "未返回有效结果");
            }
            return searchResultUrl;
        } catch (Exception e) {
            log.error("搜索失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }

    public static void main(String[] args) {
        //测试以图搜图
        String imageUrl = "https://skye-1429152456.cos.ap-shanghai.myqcloud.com/space/2055257831873634306/2026-05-15_glGhhGKp2TlslHYP.webp";
        String result = getImagePageUrl(imageUrl);
        System.out.println("搜索结果成功，结果url：" + result);
    }

}
