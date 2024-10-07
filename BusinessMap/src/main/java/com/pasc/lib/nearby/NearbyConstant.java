package com.pasc.lib.nearby;


import android.text.TextUtils;

public class NearbyConstant {


    // 调用方传递搜索类型
    public static final String NEAR_DELIVER_SEARCH_TYPE_KEY = "near_deliver_search_type";

    // 保存当前位置信息
    public static final String NEAR_CURRENT_LOCATION_KEY = "near_current_location";
    // 搜索类型
    public static final String NEAR_HOME_SEARCH_TYPE_KEY = "near_home_search_type";

    // 跳转到附近的生活首页路由
    public static final String JUMP_NEARBY_MAP_MAIN_KEY = "/nearby/map/main";

    // 默认搜索项
    public static final String NEAR_DEFAULT_SEARCH_TYPE_LIST = "24H便利店,公共卫生间,银行,ATM/自助银行,充电桩,照相馆,加油站,机动车维修点,周边药店,医保网店,设备网点,火车票代售点,社会福利院,邮局,博物馆,代驾";


    /**
     * getSearchTypeText
     * @param typeCode
     * @return
     */
    public static String getSearchTypeTxt(String typeCode) {
        String typeString = "";

        if (!TextUtils.isEmpty(typeCode)) {
            switch (typeCode) {
                case "24hour_shop":
                    typeString = "24H便利店";
                    break;
                case "parking":
                    typeString = "停车场";
                    break;
                case "bank":
                    typeString = "银行";
                    break;
                case "petrol_station":
                    typeString = "加油站";
                    break;
                case "supermarket":
                    typeString = "超市";
                    break;
                case "public_toilets":
                    typeString = "厕所";
                    break;
                case "post_office":
                    typeString = "邮局";
                    break;
                case "chemists_shop":
                    typeString = "药店";
                    break;
                case "atm_self_bank":
                    typeString = "ATM/自助银行";
                    break;
                case "hospital_clinic":
                    typeString = "医院/诊所";
                    break;
                case "ticket_outlets":
                    typeString = "火车票代售点";
                    break;
                case "driver_service":
                    typeString = "代驾";
                    break;
                default:
                    break;
            }

        }

        return typeString;
    }
}
