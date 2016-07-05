package com.parrot.fpvtoolbox;

/**
 * Created by fred on 02/06/16.
 */
public class FpvScene {

    enum SceneType
    {
        WEB,
        IMAGE,
        VIDEO,
    }

    private String mName;
    private String mSubtitle;
    String mUrl;
    SceneType mType;

    public FpvScene(String name,String url, SceneType type,  String subtitle) {
        mName = name;
        mUrl = url;
        mType = type;
        mSubtitle = subtitle;
    }

    public String getUrl() {
        return mUrl;
    }

    public SceneType getType() {
        return mType;
    }

    public String getName() {
        return mName;
    }

    public String getSubtitle() {
        return mSubtitle;
    }
}
