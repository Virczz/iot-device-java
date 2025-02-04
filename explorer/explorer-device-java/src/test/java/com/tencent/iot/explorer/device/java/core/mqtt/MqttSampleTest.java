package com.tencent.iot.explorer.device.java.core.mqtt;

import com.tencent.iot.explorer.device.java.data_template.TXDataTemplateDownStreamCallBack;
import com.tencent.iot.explorer.device.java.mqtt.TXMqttRequest;
import com.tencent.iot.explorer.device.java.core.data_template.DataTemplateSample;
import com.tencent.iot.hub.device.java.core.common.Status;
import com.tencent.iot.hub.device.java.core.mqtt.TXMqttActionCallBack;
import com.tencent.iot.hub.device.java.core.mqtt.TXMqttConstants;
import com.tencent.iot.hub.device.java.utils.Loggor;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import explorer.unit.test.BuildConfig;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MqttSampleTest {
    private static final Logger LOG = LoggerFactory.getLogger(MqttSampleTest.class);
    private static final String TAG = MqttSampleTest.class.getSimpleName();
    private static String mBrokerURL = null;  //传入null，即使用腾讯云物联网通信默认地址 "${ProductId}.iotcloud.tencentdevices.com:8883"  https://cloud.tencent.com/document/product/634/32546
    private static String mProductID = BuildConfig.TESTMQTTSAMPLE_PRODUCT_ID;
    private static String mDevName = BuildConfig.TESTMQTTSAMPLE_DEVICE_NAME;
    private static String mDevPSK  = BuildConfig.TESTMQTTSAMPLE_DEVICE_PSK; //若使用证书验证，设为null

    private static String mDevCert = "DEVICE_CERT_FILE_NAME";           // Device Cert File Name
    private static String mDevPriv = "DEVICE_PRIVATE_KEY_FILE_NAME";            // Device Private Key File Name
    private static AtomicInteger requestID = new AtomicInteger(0);
    private static String mJsonFileName = "struct.json";
    private static String mJsonFilePath = System.getProperty("user.dir") + "/src/test/resources/";

    private static DataTemplateSample mDataTemplateSample;

    static {
        Loggor.setLogger(LOG);
    }

    private static void connect() {
        // init connection
        mDataTemplateSample = new DataTemplateSample(mBrokerURL, mProductID, mDevName, mDevPSK, mDevCert, mDevPriv, new SelfMqttActionCallBack(),
                mJsonFileName, mJsonFilePath, new SelfDownStreamCallBack());
        mDataTemplateSample.connect();
    }

    private static void disconnect() {
        mDataTemplateSample.disconnect();
    }

    private static void subscribeTopic() {
        mDataTemplateSample.subscribeTopic();
    }

    private static void unSubscribeTopic() {
        mDataTemplateSample.unSubscribeTopic();
    }

    private static void propertyReport() {
        JSONObject property = new JSONObject();
        JSONObject structJson = new JSONObject();
        structJson.put("bool_param", 1);                    // 布尔类型
        structJson.put("int_param", 10);                    // 整数类型
        structJson.put("str_param", "testStrAndroid");      // 字符串类型
        structJson.put("float_param", 2.1001);              // 浮点类型
        structJson.put("enum_param", 1);                    // 枚举类型
        structJson.put("time_param", 1577871650);           // 时间戳类型
        property.put("struct", structJson);   // 自定义结构体属性

        property.put("power_switch",0);     // 创建产品时，选择产品品类为智能城市-公共事业-路灯照明，数据模板中系统推荐的标准功能属性
        property.put("color",0);            // 创建产品时，选择产品品类为智能城市-公共事业-路灯照明，数据模板中系统推荐的标准功能属性
        property.put("brightness",0);       // 创建产品时，选择产品品类为智能城市-公共事业-路灯照明，数据模板中系统推荐的标准功能属性
        property.put("name","test");        // 创建产品时，选择产品品类为智能城市-公共事业-路灯照明，数据模板中系统推荐的标准功能属性

        JSONArray arrInt = new JSONArray();  // 整数数组
        arrInt.put(1);
        arrInt.put(3);
        arrInt.put(5);
        arrInt.put(7);
        property.put("arrInt", arrInt);

        JSONArray arrStr = new JSONArray();  // 字符串数组
        arrStr.put("aaa");
        arrStr.put("bbb");
        arrStr.put("ccc");
        arrStr.put("");
        property.put("arrString", arrStr);

        JSONArray arrFloat = new JSONArray();  // 浮点数组
        arrFloat.put(5.001);
        arrFloat.put(0.003);
        arrFloat.put(0.004);
        arrFloat.put(0.007);
        property.put("arrFloat", arrFloat);

        JSONArray arrStruct = new JSONArray();  // 结构体数组
        for (int i = 0; i < 7; i++) {
            JSONObject structEleJson = new JSONObject();
            structEleJson.put("boolM", 0);      // 布尔型参数
            structEleJson.put("intM", 0);      // 整数型参数
            structEleJson.put("stringM", "string");  // 字符串参数
            structEleJson.put("floatM", 0.1); // 浮点型参数
            structEleJson.put("enumM", 0);      // 枚举型参数
            structEleJson.put("timeM", 1577871650);        // 时间型参数
            arrStruct.put(structEleJson);
        }

        property.put("arrStruct", arrStruct);


        if(Status.OK != mDataTemplateSample.propertyReport(property, null)) {
            Loggor.error(TAG, "property report failed!");
        }
    }

    private static void propertyGetStatus() {
        //get status
        if(Status.OK != mDataTemplateSample.propertyGetStatus("report", false)) {
            Loggor.error(TAG, "property get report status failed!");
        }

        if(Status.OK != mDataTemplateSample.propertyGetStatus("control", false)) {
            Loggor.error(TAG, "property get control status failed!");
        }
    }

    private static void propertyReportInfo() {
        //report info
        JSONObject params = new JSONObject();
        try {
            JSONObject label = new JSONObject();  //device label
            label.put("version", "v1.0.0");
            label.put("company", "tencent");

            params.put("module_hardinfo", "v1.0.0");
            params.put("module_softinfo", "v1.0.0");
            params.put("fw_ver", "v1.0.0");
            params.put("imei", "0");
            params.put("mac", "00:00:00:00");
            params.put("device_label", label);
        } catch (JSONException e) {
            Loggor.error(TAG, "Construct params failed!");
            return;
        }
        if(Status.OK != mDataTemplateSample.propertyReportInfo(params)) {
            Loggor.error(TAG, "property report failed!");
        }
    }

    private static void propertyClearControl() {
        //clear control
        if(Status.OK !=  mDataTemplateSample.propertyClearControl()){
            Loggor.error(TAG, "clear control failed!");
        }
    }

    private static void eventSinglePost() {
        String eventId = "status_report";
        String type = "info";
        JSONObject params = new JSONObject();
        try {
            params.put("status",0);
            params.put("message","");
        } catch (JSONException e) {
            Loggor.error(TAG, "Construct params failed!");
        }
        if(Status.OK != mDataTemplateSample.eventSinglePost(eventId, type, params)){
            Loggor.error(TAG, "single event post failed!");
        }
    }

    private static void eventsPost() {
        JSONArray events = new JSONArray();

        //event:status_report
        try {
            JSONObject event = new JSONObject();
            event.put("eventId","status_report");
            event.put("type", "info");
            event.put("timestamp", System.currentTimeMillis());

            JSONObject params = new JSONObject();
            params.put("status",0);
            params.put("message","");

            event.put("params", params);

            events.put(event);
        } catch (JSONException e) {
            Loggor.error(TAG, "Construct params failed!");
            return;
        }

        //event:low_voltage
        try {
            JSONObject event = new JSONObject();
            event.put("eventId","low_voltage");
            event.put("type", "alert");
            event.put("timestamp", System.currentTimeMillis());

            JSONObject params = new JSONObject();
            params.put("voltage",1.000000f);

            event.put("params", params);

            events.put(event);
        } catch (JSONException e) {
            Loggor.error(TAG, "Construct params failed!");
            return;
        }

        //event:hardware_fault
        try {
            JSONObject event = new JSONObject();
            event.put("eventId","hardware_fault");
            event.put("type", "fault");
            event.put("timestamp", System.currentTimeMillis());

            JSONObject params = new JSONObject();
            params.put("name","");
            params.put("error_code",1);

            event.put("params", params);

            events.put(event);
        } catch (JSONException e) {
            Loggor.error(TAG, "Construct params failed!");
            return;
        }

        if(Status.OK != mDataTemplateSample.eventsPost(events)){
            Loggor.error(TAG, "events post failed!");
        }
    }

    public static class SelfMqttActionCallBack extends TXMqttActionCallBack {

        @Override
        public void onConnectCompleted(Status status, boolean reconnect, Object userContext, String msg) {
            String userContextInfo = "";
            if (userContext instanceof TXMqttRequest) {
                userContextInfo = userContext.toString();
            }
            String logInfo = String.format("onConnectCompleted, status[%s], reconnect[%b], userContext[%s], msg[%s]",
                    status.name(), reconnect, userContextInfo, msg);
            Loggor.info(TAG, logInfo);
            if (!reconnect){unlock();}
        }

        @Override
        public void onConnectionLost(Throwable cause) {
            String logInfo = String.format("onConnectionLost, cause[%s]", cause.toString());
            Loggor.info(TAG, logInfo);
        }

        @Override
        public void onDisconnectCompleted(Status status, Object userContext, String msg) {
            String userContextInfo = "";
            if (userContext instanceof TXMqttRequest) {
                userContextInfo = userContext.toString();
            }
            String logInfo = String.format("onDisconnectCompleted, status[%s], userContext[%s], msg[%s]", status.name(), userContextInfo, msg);
            Loggor.info(TAG, logInfo);
            unlock();
        }

        @Override
        public void onPublishCompleted(Status status, IMqttToken token, Object userContext, String errMsg) {
            String userContextInfo = "";
            if (userContext instanceof TXMqttRequest) {
                userContextInfo = userContext.toString();
            }
            String logInfo = String.format("onPublishCompleted, status[%s], topics[%s],  userContext[%s], errMsg[%s]",
                    status.name(), Arrays.toString(token.getTopics()), userContextInfo, errMsg);
            Loggor.debug(TAG, logInfo);
        }

        @Override
        public void onSubscribeCompleted(Status status, IMqttToken asyncActionToken, Object userContext, String errMsg) {
            String userContextInfo = "";
            if (userContext instanceof TXMqttRequest) {
                userContextInfo = userContext.toString();
            }
            String logInfo = String.format("onSubscribeCompleted, status[%s], topics[%s], userContext[%s], errMsg[%s]",
                    status.name(), Arrays.toString(asyncActionToken.getTopics()), userContextInfo, errMsg);
            if (Status.ERROR == status) {
                Loggor.error(TAG, logInfo);
            } else {
                Loggor.debug(TAG, logInfo);
            }
            if (Arrays.toString(asyncActionToken.getTopics()).contains("thing/down/property") && userContextInfo.contains("subscribeTopic")) {
                subscribeTopicSuccess = true;
                unlock();
            }
        }

        @Override
        public void onUnSubscribeCompleted(Status status, IMqttToken asyncActionToken, Object userContext, String errMsg) {
            String userContextInfo = "";
            if (userContext instanceof TXMqttRequest) {
                userContextInfo = userContext.toString();
            }
            String logInfo = String.format("onUnSubscribeCompleted, status[%s], topics[%s], userContext[%s], errMsg[%s]",
                    status.name(), Arrays.toString(asyncActionToken.getTopics()), userContextInfo, errMsg);
            Loggor.debug(TAG, logInfo);
            if (Arrays.toString(asyncActionToken.getTopics()).contains("thing/down/property") && userContextInfo.contains("subscribeTopic")) {
                unSubscribeTopicSuccess = true;
                unlock();
            }
        }

        @Override
        public void onMessageReceived(final String topic, final MqttMessage message) {
            String logInfo = String.format("receive message, topic[%s], message[%s]", topic, message.toString());
            Loggor.debug(TAG, logInfo);
            if (topic.contains("thing/down/property") && message.toString().contains("report_reply") && message.toString().contains("success")) {//上报属性成功消息
                propertyReportSuccess = true;
                unlock();
            }  else if (topic.contains("thing/down/property") && message.toString().contains("get_status_reply") && message.toString().contains("success") && message.toString().contains("report")) { //获取report状态成功消息
                propertyGetStatusSuccess = true;
                unlock();
            }
        }
    }

    /**
     * 实现下行消息处理的回调接口
     */
    private static class SelfDownStreamCallBack extends TXDataTemplateDownStreamCallBack {
        @Override
        public void onReplyCallBack(String replyMsg) {
            //可根据自己需求进行处理属性上报以及事件的回复，根据需求填写
            Loggor.debug(TAG, "reply received : " + replyMsg);

            if (replyMsg.contains("report_info_reply") &&  replyMsg.contains("success")) { //获取report_info状态成功消息
                propertyReportInfoSuccess = true;
                unlock();
            } else if (replyMsg.contains("clear_control_reply") &&  replyMsg.contains("success")) { //获取clear_control状态成功消息
                propertyClearControlSuccess = true;
                unlock();
            } else if (replyMsg.contains("event_reply") &&  replyMsg.contains("\"code\":0")) { //获取event状态成功消息
                eventSinglePostSuccess = true;
                unlock();
            } else if (replyMsg.contains("events_reply") &&  replyMsg.contains("\"code\":0")) { //获取event状态成功消息
                eventsPostSuccess = true;
                unlock();
            }
        }

        @Override
        public void onGetStatusReplyCallBack(JSONObject data) {
            //可根据自己需求进行处理状态和控制信息的获取结果
            Loggor.debug(TAG, "event down stream message received : " + data);
        }

        @Override
        public JSONObject onControlCallBack(JSONObject msg) {
            Loggor.debug(TAG, "control down stream message received : " + msg);
            //do something

            //output
            try {
                JSONObject result = new JSONObject();
                result.put("code",0);
                result.put("status", "some message wher errorsome message when error");
                return result;
            } catch (JSONException e) {
                Loggor.error(TAG, "Construct params failed!");
                return null;
            }
        }

        @Override
        public  JSONObject onActionCallBack(String actionId, JSONObject params){
            Loggor.debug(TAG, String.format("action [%s] received, input:%s", actionId, params));
            //do something based action id and input
            if(actionId.equals("blink")) {
                try {
                    Iterator<String> it = params.keys();
                    while (it.hasNext()) {
                        String key = it.next();
                        Loggor.debug(TAG, String.format("Input parameter[%s]:%s", key, params.get(key)));
                    }
                    //construct result
                    JSONObject result = new JSONObject();
                    result.put("code",0);
                    result.put("status", "some message wher errorsome message when error");

                    // response based on output
                    JSONObject response = new JSONObject();
                    response.put("result", 0);

                    result.put("response", response);
                    return result;
                } catch (JSONException e) {
                    return null;
                }
            } else if (actionId.equals("YOUR ACTION")) {
                //do your action
            }
            return null;
        }

        @Override
        public void onUnbindDeviceCallBack(String msg) {
            //可根据自己需求进行用户删除设备的通知消息处理的回复，根据需求填写
            Loggor.debug(TAG, "unbind device received : " + msg);
        }

        @Override
        public void onBindDeviceCallBack(String msg) {
            //可根据自己需求进行用户绑定设备的通知消息处理的回复，根据需求填写
            Loggor.debug(TAG, "bind device received : " + msg);
        }
    }

    /** ============================================================================== Unit Test ============================================================================== **/

    private static final int COUNT = 1;
    private static final int TIMEOUT = 3000;
    private static CountDownLatch latch = new CountDownLatch(COUNT);

    private static boolean subscribeTopicSuccess = false;
    private static boolean propertyReportSuccess = false;
    private static boolean propertyGetStatusSuccess = false;
    private static boolean propertyReportInfoSuccess = false;
    private static boolean propertyClearControlSuccess = false;
    private static boolean eventSinglePostSuccess = false;
    private static boolean eventsPostSuccess = false;
    private static boolean unSubscribeTopicSuccess = false;

    private static void lock() {
        latch = new CountDownLatch(COUNT);
        try {
            latch.await(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void unlock() {
        latch.countDown();// 回调执行完毕，唤醒主线程
    }

    @Test
    public void testMqttConnect() {
        // Loggor.saveLogs("explorer/explorer-device-java.log"); //保存日志到文件
        connect();
        lock();
        assertSame(mDataTemplateSample.getConnectStatus(), TXMqttConstants.ConnectStatus.kConnected);
        Loggor.debug(TAG, "after connect");

        subscribeTopic();
        lock();
        assertTrue(subscribeTopicSuccess);
        Loggor.debug(TAG, "after subscribe");

        propertyReport();
        lock();
        assertTrue(propertyReportSuccess);
        Loggor.debug(TAG, "after propertyReport");

        propertyGetStatus();
        lock();
        assertTrue(propertyGetStatusSuccess);
        Loggor.debug(TAG, "after propertyGetStatus");

        propertyReportInfo();
        lock();
        assertTrue(propertyReportInfoSuccess);
        Loggor.debug(TAG, "after propertyReportInfo");

        propertyClearControl();
        lock();
        assertTrue(propertyClearControlSuccess);
        Loggor.debug(TAG, "after propertyClearControl");

        eventSinglePost();
        lock();
        assertTrue(eventSinglePostSuccess);
        Loggor.debug(TAG, "after eventSinglePost");

        eventsPost();
        lock();
        assertTrue(eventsPostSuccess);
        Loggor.debug(TAG, "after eventsPost");

        unSubscribeTopic();
        lock();
        assertTrue(unSubscribeTopicSuccess);
        Loggor.debug(TAG, "after unSubscribe");

        disconnect();
        lock();
        Loggor.debug(TAG, "after disconnect");
        assertSame(mDataTemplateSample.getConnectStatus(), TXMqttConstants.ConnectStatus.kDisconnected);
    }
}