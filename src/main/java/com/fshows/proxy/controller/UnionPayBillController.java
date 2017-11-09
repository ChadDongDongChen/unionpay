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
import java.util.List;
import java.util.Map;

@Controller
public class UnionPayBillController {

    private static final Logger logger = LoggerFactory.getLogger(UnionPayBillController.class);

    @RequestMapping("/bill")
    public void test1(HttpServletRequest req, HttpServletResponse response) throws Exception {

        System.out.println("==========bill========");
        long start = System.currentTimeMillis();
        String num = req.getParameter("num");
        String merId = req.getParameter("merId");
        String txnAmt = req.getParameter("txnAmt");
        String orderId = req.getParameter("orderId");
        String txnTime = req.getParameter("txnTime");

        String settleDate = req.getParameter("settleDate");


        merId="WZPA0000FS0000"+num;
        orderId="cfyme20171105000001"+System.currentTimeMillis();

        System.out.println("===============");
        System.out.println("========merId======="+merId);

        //订单创建时间
        txnTime = DateUtil.getStringByMillis(System.currentTimeMillis(), "yyyyMMddHHmmss");

        System.out.println("txtTime=" + txnTime);

        Map<String, String> contentData = new HashMap<String, String>();



        /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
        contentData.put("version", DemoBase.version);            //版本号 全渠道默认值
        contentData.put("encoding", DemoBase.encoding);     //字符集编码 可以使用UTF-8,GBK两种方式
        contentData.put("signMethod", SDKConfig.getConfig().getSignMethod()); //签名方法
        contentData.put("txnType", "76");                        //交易类型 76-对账文件下载
        contentData.put("txnSubType", "01");                    //交易子类 01-对账文件下载
        contentData.put("bizType", "000000");                    //填写000000
        contentData.put("accessType", "0");                    //渠道类型 08手机

        /***商户接入参数***/
        contentData.put("merId", merId);                        //商户号码，请改成自己申请的商户号或者open上注册得来的777商户号测试
        contentData.put("settleDate", settleDate);                  //清算日期，如果使用正式商户号测试则要修改成自己想要获取对账文件的日期， 测试环境如果使用700000000000001商户号则固定填写0119
        contentData.put("txnTime",DemoBase.getCurrentTime());       //订单发送时间，取系统时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效

        contentData.put("fileType", "00");                          //文件类型，一般商户填写00即可


        /**对请求参数进行签名并发送http post请求，接收同步应答报文**/
        Map<String, String> reqData = AcpService.sign(contentData,DemoBase.encoding);			 //报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
        String url = SDKConfig.getConfig().getFileTransUrl();
        System.out.println("url="+url);
        //交易请求url从配置文件读取对应属性文件acp_sdk.properties中的 acpsdk.backTransUrl
        Map<String, String> rspData = AcpService.post(reqData,url,DemoBase.encoding);  //发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）;这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过



        /**对应答码的处理，请根据您的业务逻辑来编写程序,以下应答码处理逻辑仅供参考------------->**/

        //应答码规范参考open.unionpay.com帮助中心 下载  产品接口规范  《平台接入接口规范-第5部分-附录》
        String fileContentDispaly = "";
        if(!rspData.isEmpty()){
            if(AcpService.validate(rspData, DemoBase.encoding)){
                LogUtil.writeLog("验证签名成功");
                String respCode = rspData.get("respCode");
                if("00".equals(respCode)){
                    String outPutDirectory ="/Users/caofy/other";
                    // 交易成功，解析返回报文中的fileContent并落地
                    String zipFilePath = AcpService.deCodeFileContent(rspData,outPutDirectory,DemoBase.encoding);
                    //对落地的zip文件解压缩并解析
                    List<String> fileList = DemoBase.unzip(zipFilePath, outPutDirectory);
                    //解析ZM，ZME文件
                    fileContentDispaly ="<br>获取到商户对账文件，并落地到"+outPutDirectory+",并解压缩 <br>";
                    for(String file : fileList){
                        if(file.indexOf("ZM_")!=-1){
                            List<Map> ZmDataList = DemoBase.parseZMFile(file);
                            fileContentDispaly = fileContentDispaly+DemoBase.getFileContentTable(ZmDataList,file);
                        }else if(file.indexOf("ZME_")!=-1){
                            DemoBase.parseZMEFile(file);
                        }
                    }
                    //TODO
                }else{
                    //其他应答码为失败请排查原因
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


        long costTime = System.currentTimeMillis()-start;
        String reqMessage = DemoBase.genHtmlResult(reqData);
        String rspMessage = DemoBase.genHtmlResult(rspData);
        response.getWriter().write("request msg:<br/>"+reqMessage+"<br/>" + "responsemsg:</br>"+rspMessage+""
                +"<br/>" + "costTime:</br>"+costTime+"");
    }




}
