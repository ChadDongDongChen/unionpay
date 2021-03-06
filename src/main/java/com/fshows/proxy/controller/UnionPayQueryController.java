package com.fshows.proxy.controller;

import com.fshows.proxy.sdk.AcpService;
import com.fshows.proxy.sdk.DemoBase;
import com.fshows.proxy.sdk.LogUtil;
import com.fshows.proxy.sdk.SDKConfig;
import com.fshows.proxy.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
public class UnionPayQueryController {

    private static final Logger logger = LoggerFactory.getLogger(UnionPayQueryController.class);

    @RequestMapping("/query")
    public void test1(HttpServletRequest req, HttpServletResponse response) throws Exception {

        System.out.println("==========test1========");

        String merId = req.getParameter("merId");
        String orderId = req.getParameter("orderId");
        String txnTime = req.getParameter("txnTime");

        merId="WZPA0000FS00001";


        Map<String, String> contentData = new HashMap<String, String>();

        SDKConfig.getConfig().loadPropertiesFromSrc(); //从classpath加载acp_sdk.properties文件

        /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
        contentData.put("version", DemoBase.version);            //版本号 全渠道默认值
        contentData.put("encoding", DemoBase.encoding);     //字符集编码 可以使用UTF-8,GBK两种方式
        contentData.put("signMethod", SDKConfig.getConfig().getSignMethod()); //签名方法
        contentData.put("txnType", "00");                        //交易类型 01:消费
        contentData.put("txnSubType", "00");                    //交易子类 07：申请消费二维码
        contentData.put("accessType", "0");                    //渠道类型 08手机

        /***商户接入参数***/
        contentData.put("merId", merId);                        //商户号码，请改成自己申请的商户号或者open上注册得来的777商户号测试
        contentData.put("accessType", "0");                        //接入类型，商户接入填0 ，不需修改（0：直连商户， 1： 收单机构 2：平台商户）
        contentData.put("orderId", orderId);                    //商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则


        contentData.put("bizType", "000000");                         //业务类型

        if(txnTime!=null &&txnTime.length()>0){
            contentData.put("txnTime", txnTime);
        }



        /**对请求参数进行签名并发送http post请求，接收同步应答报文**/
        Map<String, String> reqData = AcpService.sign(contentData,DemoBase.encoding);			 //报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
        String requestAppUrl = SDKConfig.getConfig().getSingleQueryUrl();								 //交易请求url从配置文件读取对应属性文件acp_sdk.properties中的 acpsdk.backTransUrl
        Map<String, String> rspData = AcpService.post(reqData,requestAppUrl,DemoBase.encoding);  //发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）;这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过

        /**对应答码的处理，请根据您的业务逻辑来编写程序,以下应答码处理逻辑仅供参考------------->**/
        //应答码规范参考open.unionpay.com帮助中心 下载  产品接口规范  《平台接入接口规范-第5部分-附录》
        if(!rspData.isEmpty()){
            if(AcpService.validate(rspData, DemoBase.encoding)){
                LogUtil.writeLog("验证签名成功");
                String respCode = rspData.get("respCode") ;
                if(("00").equals(respCode)){
                    //成功,获取tn号
                    //String tn = resmap.get("tn");
                    //TODO
                }else{
                    //其他应答码为失败请排查原因或做失败处理
                    //TODO
                }
            }else{
                LogUtil.writeErrorLog("验证签名失败");
                //TODO 检查验证签名失败的原因
            }
        }else{
            //未返回正确的http状态
            LogUtil.writeErrorLog("未获取到返回报文或返回http状态码非200");
        }
        String reqMessage = DemoBase.genHtmlResult(reqData);
        String rspMessage = DemoBase.genHtmlResult(rspData);
        response.getWriter().write("请求报文:<br/>"+reqMessage+"<br/>" + "应答报文:</br>"+rspMessage+"");

    }




}
