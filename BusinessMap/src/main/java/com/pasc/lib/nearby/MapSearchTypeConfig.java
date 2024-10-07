package com.pasc.lib.nearby;

import java.io.Serializable;

/**
 * 传入附近生活搜索类型
 */
public class MapSearchTypeConfig implements Serializable{

    private String[] poiSearchType;

    public String[] getPoiSearchType() {
        return poiSearchType;
    }

    public void setPoiSearchType(String[] poiSearchType) {
        this.poiSearchType = poiSearchType;
    }

    public static class Builder implements Serializable{

        private String[] searchType;
        public Builder setNearLifePoiSearchType(String[] PoiSearchType){
            searchType = PoiSearchType;
            return this;
        }

        public MapSearchTypeConfig create(){
            MapSearchTypeConfig config = new MapSearchTypeConfig();
            config.setPoiSearchType(searchType);

            return config;
        }

    }


}
