package com.genesys.x.statdnregister;

import com.genesyslab.platform.applicationblocks.com.objects.CfgConnInfo;
import com.genesyslab.platform.commons.protocol.ChannelClosedEvent;
import com.genesyslab.platform.commons.protocol.ChannelErrorEvent;
import com.genesyslab.platform.commons.protocol.ChannelListener;
import com.google.inject.Inject;

import java.util.EventObject;

/**
 * Created by dburdick on 12/3/2015.
 */
public class StatCfgListener implements ChannelListener {
    public StatCfgListener() {
        System.out.println("StatCfgListener creator");
    }

    @Override
    public void onChannelOpened(EventObject eventObject) {
        System.out.format("%s opened\n", eventObject.getSource().getClass().getName());
    }

    @Override
    public void onChannelClosed(ChannelClosedEvent channelClosedEvent) {
        System.out.format("%s closed\n", channelClosedEvent.getSource().getClass().getName());

    }

    @Override
    public void onChannelError(ChannelErrorEvent channelErrorEvent) {
        System.out.format("%s error\n", channelErrorEvent.getSource().getClass().getName());
    }
}
