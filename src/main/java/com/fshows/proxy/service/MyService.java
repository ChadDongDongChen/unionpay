package com.fshows.proxy.service;

import com.fshows.proxy.sdk.CertUtil;
import com.fshows.proxy.sdk.SDKConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;


@Service
public class MyService implements InitializingBean {


    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("-------afterPropertiesSet init -------");
        SDKConfig.getConfig().loadPropertiesFromSrc(); //从classpath加载acp_sdk.properties文件
        System.out.println("---backUrl-"+SDKConfig.getConfig().getBackUrl());
        System.out.println("---transUrl-"+SDKConfig.getConfig().getFileTransUrl());

        CertUtil certUtil = new CertUtil();
        System.out.println("-------afterPropertiesSet end-------");
    }



}
