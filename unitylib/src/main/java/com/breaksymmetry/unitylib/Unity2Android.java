package com.breaksymmetry.unitylib;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;


import com.alipay.sdk.app.EnvUtils;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.alipay.sdk.app.PayTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by Jing on 2018-1-18.
 */
public class Unity2Android {

    /**
     * unity项目启动时的的上下文
     */
    private Activity _unityActivity;

    /**
     * 获取unity项目的上下文
     *
     * @return
     */
    Activity getActivity() {
        if (null == _unityActivity) {
            try {
                Class<?> classtype = Class.forName("com.unity3d.player.UnityPlayer");
                Activity activity = (Activity) classtype.getDeclaredField("currentActivity").get(classtype);
                _unityActivity = activity;
            } catch (ClassNotFoundException e) {

            } catch (IllegalAccessException e) {

            } catch (NoSuchFieldException e) {

            }
        }
        return _unityActivity;
    }

    /**
     * 调用Unity的方法
     *
     * @param gameObjectName 调用的GameObject的名称
     * @param functionName   方法名
     * @param args           参数
     * @return 调用是否成功
     */
    boolean callUnity(String gameObjectName, String functionName, String args) {
        try {
            Class<?> classtype = Class.forName("com.unity3d.player.UnityPlayer");
            Method method = classtype.getMethod("UnitySendMessage", String.class, String.class, String.class);
            method.invoke(classtype, gameObjectName, functionName, args);
            return true;
        } catch (ClassNotFoundException e) {

        } catch (NoSuchMethodException e) {

        } catch (IllegalAccessException e) {

        } catch (InvocationTargetException e) {

        }
        return false;
    }

    /**
     * Toast显示unity发送过来的内容
     *
     * @param content 消息的内容
     * @return 调用是否成功
     */
    public boolean showToast(String content) {
        Toast.makeText(getActivity(), content, Toast.LENGTH_SHORT).show();
        //这里是主动调用Unity中的方法，该方法之后unity部分会讲到
        callUnity("BootScope", "FromAndroid", "hello unity i'm android");
        return true;
    }

    public void InitAliPay(boolean useSandbox) {
        Log.i("Unity", "InitAliPay, useSandbox = " + useSandbox);
        if (useSandbox) {
            EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX);
        } else {
            EnvUtils.setEnv(EnvUtils.EnvEnum.ONLINE);
        }
    }
    /**
     * 支付宝App支付
     */
    public void AliPayByApp(final String orderInfo) {
        Log.i("Unity", "启动线程");
        if (orderInfo == null || orderInfo.trim().isEmpty()) {
            Log.e("Unity", "支付宝支付失败，orderInfo为空");
            callUnity("BootScope", "onALIPayFinish", "{'resultStatus':'6001','result':'orderInfo is null','memo':'Failed'}");
            return;
        }
        Log.d("AliPay", "Received orderInfo: " + orderInfo);

        final Runnable payRun = new Runnable() {
            @Override
            public void run() {
                try {
                    Activity activity = getActivity();
                    if (activity == null) {
                        Log.e("Unity", "支付宝支付失败，activity为空");
                        return;
                    }
                    PayTask paytask = new PayTask(activity);
                    Map<String, String> result = paytask.payV2(orderInfo, true);

                    Log.i("Unity", "onALIPayFinish, result = " + result.toString());
                    // 解析具体的返回码
                    String resultStatus = result.get("resultStatus");
                    String resultMsg = result.get("memo");
                    Log.d("AliPay", "Status: " + resultStatus + ", Message: " + resultMsg);
                    JSONObject jsonResult = new JSONObject(result);
                    //支付回调
                    callUnity("BootScope", "onALIPayFinish", jsonResult.toString());

                    //UnityPlayer.UnitySendMessage("GamePaySDK", "ALiPayResult", result);
                } catch (Exception e) {
                    Log.e("AliPay", "Payment error: " + e.getMessage());
                    e.printStackTrace();
                    JSONObject errorResult = new JSONObject();
                    try {
                        errorResult.put("resultStatus", "-1");
                        errorResult.put("memo", "Payment error: " + e.getMessage());
                        callUnity("BootScope", "onALIPayFinish", errorResult.toString());
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }


                }
            }
        };
        Thread payThread = new Thread(payRun);
        payThread.start();
    }

    private IWXAPI msgApi;

    /**
     * 微信App支付
     */
    public void WxPayByApp(String appId, String partnerId, String prepayId, String nonceStr, String timeStamp, String sign) {
        Activity activity = getActivity();
        if (activity != null) {
            msgApi = WXAPIFactory.createWXAPI(activity, appId);
            PayReq request = new PayReq();
            request.appId = appId;
            request.partnerId = partnerId;
            request.prepayId = prepayId;
            request.packageValue = "Sign=WXPay";
            request.nonceStr = nonceStr;
            request.timeStamp = timeStamp;
            request.sign = sign;
            Log.d("Unity", request.checkArgs() + "");//输出验签是否正确
            msgApi.sendReq(request);
        } else {
            Log.e("Unity", "微信支付失败，activity为空");
        }
    }
}