package rgr.sshApp.web;

import com.jcraft.jsch.Channel;
import lombok.Getter;

import java.util.LinkedList;

@Getter
public class ChannelData {

    private static volatile ChannelData sessionChannelData;
    private LinkedList<Channel> channelList;

    private ChannelData(){
        this.channelList = new LinkedList<>();
    }

    public static ChannelData getInstance() {
        ChannelData localsessionChannelData = sessionChannelData;
        if (localsessionChannelData == null){
            synchronized (ChannelData.class){
                localsessionChannelData = sessionChannelData;
                if (localsessionChannelData ==null){
                    sessionChannelData = localsessionChannelData = new ChannelData();
                }
            }
        }
        return localsessionChannelData;
    }

    public void addNewChannel(Channel channel) {
        channelList.add(channel);
    }

    public void getChannel(Class channelClass) {

    }

    public void clearData() {
        channelList.clear();
    }

}
