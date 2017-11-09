package com.fshows.proxy.controller;

import com.fshows.proxy.sdk.*;
import com.fshows.proxy.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Controller
public class CallBackController {

    private static final Logger logger = LoggerFactory.getLogger(CallBackController.class);

    @RequestMapping("/callback")
    public void test1(HttpServletRequest request, HttpServletResponse response) throws Exception {

        System.out.println("==========callback========" + request.getQueryString());

        request.setCharacterEncoding("UTF-8");
        //获得参数的编码 没有则为UTF-8
        String encoding = request.getParameter(SDKConstants.param_encoding);
        encoding = encoding == null ? "UTF-8" : encoding;
        // 获取银联通知服务器发送的后台通知参数
        Map<String, String> reqParam = SDKUtil.getAllRequestParam(request);

        if (reqParam == null || reqParam.isEmpty()) {
            logger.info("{}回调接口 >> 无参数");
        }
        
        System.out.println(222);

        Iterator<Map.Entry<String, String>> it = reqParam.entrySet().iterator();
        //验签map
        HashMap<String, String> valideData = new HashMap<>(reqParam.size());
        while (it.hasNext()) {
            Map.Entry<String, String> e = it.next();
            String key = e.getKey();
            String value = e.getValue();
            value = new String(value.getBytes(encoding), encoding);
            valideData.put(key, value);
        }

        System.out.println(333);
        logger.info("{}回调接口 >> 获得验签参数, valideData = {}");

        //重要！验证签名前不要修改reqParam中的键值对的内容，否则会验签不过
        if (!AcpService.validate(valideData, encoding)) {

            System.out.println("999999999999");
            //验签失败
        }

    }


}
