package games.aof.unitylib;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.binance.android.binancepay.api.BinancePay;
import com.binance.android.binancepay.api.BinancePayException;
import com.binance.android.binancepay.api.BinancePayFactory;
import com.binance.android.binancepay.api.BinancePayListener;
import com.binance.android.binancepay.api.BinancePayParam;
import com.tokenpocket.opensdk.base.TPListener;
import com.tokenpocket.opensdk.base.TPManager;
import com.tokenpocket.opensdk.simple.model.Blockchain;
import com.tokenpocket.opensdk.simple.model.Signature;

import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
     * @return
     */
    Activity getActivity(){
        if(null == _unityActivity) {
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
     * @param gameObjectName    调用的GameObject的名称
     * @param functionName      方法名
     * @param args              参数
     * @return                  调用是否成功
     */
    boolean callUnity(String gameObjectName, String functionName, String args){
        try {
            Class<?> classtype = Class.forName("com.unity3d.player.UnityPlayer");
            Method method =classtype.getMethod("UnitySendMessage", String.class,String.class,String.class);
            method.invoke(classtype,gameObjectName,functionName,args);
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
     * @param content           消息的内容
     * @return                  调用是否成功
     */
    public boolean showToast(String content){
        Toast.makeText(getActivity(),content,Toast.LENGTH_SHORT).show();
        //这里是主动调用Unity中的方法，该方法之后unity部分会讲到
        callUnity("Main Camera","FromAndroid", "hello unity i'm android");
        return true;
    }

    /**
     * @param chain          "ethereum"
     * @param chainId        "1"
     * @param signType       "ethPersonalSign"
     */
    public void sign(String signMessage, String chain, String chainId, String signType){
        Signature signature = new Signature();
        //标识链
        List<Blockchain> blockchains = new ArrayList<>();
        blockchains.add(new Blockchain(chain, chainId));
        signature.setBlockchains(blockchains);

        signature.setDappName("ArenaOfFaith");
        signature.setDappIcon("https://aof-project.s3-accelerate.amazonaws.com/front/common/favicon.svg");
        signature.setActionId("signAction");
        signature.setMemo("LoginSign");
        signature.setSignType(signType);
        signature.setMessage(signMessage);
        TPManager.getInstance().signature(getActivity(), signature, new TPListener() {
            @Override
            public void onSuccess(String s) {
                Log.i("unity", "onSuccess: "+s);
                callUnity("TPCallbackHelper","SignOnSuccess", s);
            }

            @Override
            public void onError(String s) {
                Log.i("unity", "onError: "+s);
                callUnity("TPCallbackHelper","SignOnError", s);
            }

            @Override
            public void onCancel(String s) {

                Log.i("unity", "onCancel: "+s);
                callUnity("TPCallbackHelper","SignOnCancel", s);
            }
        });
    }
    public void binPay(String merchantId, String prepayId, Long timeStamp, String nonceStr, String certSn, String sign) {

        BinancePayParam params = new BinancePayParam(merchantId, prepayId, timeStamp.toString(), nonceStr, certSn, sign);
        BinancePay binancePay = BinancePayFactory.Companion.getBinancePay(getActivity());
        binancePay.pay(params, new BinancePayListener() {
            @Override
            public void onSuccess() {
                Log.i("unity", "onSuccess: ");
                callUnity("TPCallbackHelper", "BPayOnSuccess", "");
            }

            @Override
            public void onCancel() {
                callUnity("TPCallbackHelper", "BPayOnError", "");
            }

            @Override
            public void onError(@NonNull BinancePayException e) {
                Log.i("unity", "onError: " + e.getMessage());
                callUnity("TPCallbackHelper", "BPayOnError", e.getMessage());
            }
        });
    }
}