package com.cleverpush.plugins.capacitor;

import com.cleverpush.ChannelTopic;
import com.cleverpush.CleverPush;
import com.cleverpush.Notification;
import com.cleverpush.listener.NotificationOpenedListener;
import com.cleverpush.listener.NotificationReceivedListener;
import com.cleverpush.listener.SubscribedCallbackListener;
import com.cleverpush.listener.SubscribedListener;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;

@CapacitorPlugin(name = "CleverPush")
public class CleverPushCapacitorPlugin extends Plugin {
    private JSObject coldStartOpenObject = null;

    @PluginMethod
    public void init(PluginCall call) {
        CleverPush.getInstance(this.getActivity()).init(call.getString("channelId"),
                (NotificationReceivedListener) result -> {
                    Gson gson = new Gson();
                    JSObject obj = new JSObject();
                    try {
                        obj.put("notification", new JSObject(gson.toJson(result.getNotification())));
                        obj.put("subscription", new JSObject(gson.toJson(result.getSubscription())));
                        notifyListeners("notificationReceived", obj);
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                },
                (NotificationOpenedListener) result -> {
                    Gson gson = new Gson();
                    JSObject obj = new JSObject();
                    try {
                        obj.put("notification", new JSObject(gson.toJson(result.getNotification())));
                        obj.put("subscription", new JSObject(gson.toJson(result.getSubscription())));
                        if (hasListeners("notificationOpened")) {
                            notifyListeners("notificationOpened", obj);
                        } else {
                            coldStartOpenObject = obj;
                        }
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                },
                (SubscribedListener) subscriptionId -> {
                    JSObject obj = new JSObject();
                    obj.put("subscriptionId", subscriptionId);
                    notifyListeners("subscribed", obj);
                },
            Boolean.TRUE.equals(call.getBoolean("autoRegister", true))
        );

        CleverPush.getInstance(this.getActivity()).setAppBannerOpenedListener(action -> {
            JSObject obj = new JSObject();
            JSObject actionObj = new JSObject();
            if (action.getUrl() != null) {
                actionObj.put("url", action.getUrl());
            }
            if (action.getUrlType() != null) {
                actionObj.put("urlType", action.getUrlType());
            }
            if (action.getName() != null) {
                actionObj.put("name", action.getName());
            }
            if (action.getType() != null) {
                actionObj.put("type", action.getType());
            }
            if (action.getAttributeId() != null) {
                actionObj.put("attributeId", action.getAttributeId());
            }
            if (action.getAttributeId() != null) {
                actionObj.put("attributeValue", action.getAttributeId());
            }

            actionObj.put("dismiss", action.getDismiss());
            actionObj.put("openInWebview", action.isOpenInWebView());
            actionObj.put("openBySystem", action.isOpenBySystem());
            notifyListeners("appBannerOpened", actionObj);
        });
    }

    @PluginMethod(returnType = PluginMethod.RETURN_NONE)
    public void addListener(PluginCall call) {
        super.addListener(call);
        String eventName = call.getString("eventName");
        if (coldStartOpenObject != null && eventName != null && eventName.equals("notificationOpened")) {
            notifyListeners("notificationOpened", coldStartOpenObject);
            coldStartOpenObject = null;
        }
    }

    @PluginMethod
    public void getSubscriptionId(PluginCall call) {
        CleverPush.getInstance(this.getActivity()).getSubscriptionId(subscriptionId -> {
            JSObject obj = new JSObject();
            obj.put("subscriptionId", subscriptionId);
            call.resolve(obj);
        });
    }

    @PluginMethod
    public void trackPageView(PluginCall call) {
        String value = call.getString("url");
        CleverPush.getInstance(this.getActivity()).trackPageView(value);
    }

    @PluginMethod
    public void trackEvent(PluginCall call) {
        String eventName = call.getString("eventName");
        JSObject propertiesObject = call.getObject("properties");
        if (propertiesObject != null) {
            Iterator<String> keysIter = propertiesObject.keys();
            List<String> keys = new ArrayList<>();
            Map<String, Object> properties = new HashMap<>();
            while (keysIter.hasNext()) {
                String key = keysIter.next();
                String value = propertiesObject.getString(key);
                if (value != null) {
                    properties.put(key, value);
                }
            }
            CleverPush.getInstance(this.getActivity()).trackEvent(eventName, properties);
        } else {
            CleverPush.getInstance(this.getActivity()).trackEvent(eventName);
        }
    }

    @PluginMethod
    public void addSubscriptionTag(PluginCall call) {
        String value = call.getString("tagId");
        CleverPush.getInstance(this.getActivity()).addSubscriptionTag(value);
    }

    @PluginMethod
    public void removeSubscriptionTag(PluginCall call) {
        String value = call.getString("tagId");
        CleverPush.getInstance(this.getActivity()).removeSubscriptionTag(value);
    }

    @PluginMethod
    public void hasSubscriptionTag(PluginCall call) {
        String tag = call.getString("tagId");
        boolean value = CleverPush.getInstance(this.getActivity()).hasSubscriptionTag(tag);
        JSObject obj = new JSObject();
        obj.put("hasTag", value);
        call.resolve(obj);
    }

    @PluginMethod
    public void setSubscriptionTopics(PluginCall call) throws JSONException {
        JSArray topicsArray = call.getArray("topics");

        if (topicsArray != null) {
            String[] value = new String[topicsArray.length()];
            for (int i = 0; i < topicsArray.length(); i++) {
                value[i] = topicsArray.getString(i);
            }
            CleverPush.getInstance(this.getActivity()).setSubscriptionTopics(value);
        } 
    }

    @PluginMethod
    public void getSubscriptionTags(PluginCall call) {
        Set<String> subscriptionTags = CleverPush.getInstance(this.getActivity()).getSubscriptionTags();
        JSObject result = new JSObject();
        result.put("tagIds", subscriptionTags);
        call.resolve(result);
    }

    @PluginMethod
    public void getSubscriptionTopics(PluginCall call) {
        Set<String> subscriptionTopics = CleverPush.getInstance(this.getActivity()).getSubscriptionTopics();
        JSObject obj = new JSObject();
        obj.put("topicIds", subscriptionTopics);
        call.resolve(obj);
    }

    @PluginMethod
    public void getAvailableTopics(PluginCall call) {
        CleverPush.getInstance(this.getActivity()).getAvailableTopics(topics -> {
            Set<ChannelTopic> channelTopic = topics;
            JSObject obj = new JSObject();
            obj.put("topics", channelTopic);
            call.resolve(obj);
        });
    }

    @PluginMethod
    public void isSubscribed(PluginCall call) {
        boolean value = CleverPush.getInstance(this.getActivity()).isSubscribed();
        JSObject obj = new JSObject();
        obj.put("isSubscribed", value);
        call.resolve(obj);
    }

    @PluginMethod
    public void unsubscribe(PluginCall call) {
        CleverPush.getInstance(this.getActivity()).unsubscribe();
        JSObject obj = new JSObject();
        call.resolve(obj);
    }

    @PluginMethod
    public void subscribe(PluginCall call) {
        CleverPush.getInstance(this.getActivity()).subscribe(new SubscribedCallbackListener() {
            @Override
            public void onSuccess(String newSubscriptionId) {
                JSObject obj = new JSObject();
                obj.put("subscriptionId", newSubscriptionId);
                call.resolve(obj);
            }
            
            @Override
            public void onFailure(Throwable exception) {
                call.reject(exception.getMessage());
            }
        });
    }

    @PluginMethod
    public void showTopicsDialog(PluginCall call) {
        CleverPush.getInstance(this.getActivity()).showTopicsDialog();
    }

    @PluginMethod
    public void enableDevelopmentMode(PluginCall call) {
        CleverPush.getInstance(this.getActivity()).enableDevelopmentMode();
    }

    @PluginMethod
    public void setAuthorizerToken(PluginCall call) {
        String token = call.getString("token");
        CleverPush.getInstance(this.getActivity()).setAuthorizerToken(token);
    }

    @PluginMethod
    public void getNotifications(PluginCall call) {
        Gson gson = new Gson();
        Set<Notification> notifications = CleverPush.getInstance(this.getActivity()).getNotifications();
        JSArray notificationsArray = new JSArray();
        for (Notification notification : notifications) {
            try {
                JSObject notificationObj = new JSObject(gson.toJson(notification));
                notificationsArray.put(notificationObj);
            } catch (Exception ex) {
                System.out.println("Exception while getting notifications: " + ex.getMessage());
            }
        }
        JSObject result = new JSObject();
        result.put("notifications", notificationsArray);
        call.resolve(result);
    }
}
