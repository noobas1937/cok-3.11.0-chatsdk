package com.elex.chatservice.model.kurento;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by xupengzhan on 2016/12/5.
 */

public class WebRtcRoom {
    private String name;
    private String url;
    private PeerUserInfo localPeer;
    private List<PeerUserInfo> peers;
    private boolean dataChannels;

    public WebRtcRoom(String name,String userName,String url,boolean dataChannels)
    {
        this.name = name;
        localPeer = new PeerUserInfo(userName);
        peers = new ArrayList<PeerUserInfo>();
        this.url = url;
        this.dataChannels = dataChannels;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public PeerUserInfo getLocalPeer() {
        return localPeer;
    }

    public void setLocalPeer(PeerUserInfo localPeer) {
        this.localPeer = localPeer;
    }

    public List<PeerUserInfo> getPeers() {
        return peers;
    }

    public void setPeers(List<PeerUserInfo> peers) {
        this.peers = peers;
    }

    public boolean isDataChannels() {
        return dataChannels;
    }

    public void setDataChannels(boolean dataChannels) {
        this.dataChannels = dataChannels;
    }
}
