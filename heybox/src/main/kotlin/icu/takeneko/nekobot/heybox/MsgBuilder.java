package icu.takeneko.nekobot.heybox;

public class MsgBuilder {
    private final String uuid;
    private final String roomId;
    private final String channelId;
    private final String msg;
    private final List<String> atUsers;
    private final List<String> atRoles;
    private final List<String> atChannels;
    private String replay;

    public MsgBuilder(String roomId, String channelId, String msg) {
        this.uuid = IdUtil.fastSimpleUUID();
        this.roomId = roomId;
        this.channelId = channelId;
        this.msg = msg;
        this.atUsers = new ArrayList<>();
        this.atRoles = new ArrayList<>();
        this.atChannels = new ArrayList<>();
        this.replay = "";
    }

    public MsgBuilder atUser(long at) {
        this.atUsers.add(String.valueOf(at));
        return this;
    }

    public MsgBuilder atRole(String at) {
        this.atRoles.add(at);
        return this;
    }

    public MsgBuilder atChannel(String at) {
        this.atChannels.add(at);
        return this;
    }

    public MsgBuilder replay(String replay) {
        this.replay = replay;
        return this;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getRoomId() {
        return this.roomId;
    }

    public String getChannelId() {
        return this.channelId;
    }

    public String getMsg() {
        return this.msg;
    }

    public String getAtUsers() {
        return String.join(",", this.atUsers);
    }

    public String getAtRoles() {
        return String.join(",", this.atRoles);
    }

    public String getAtChannels() {
        return String.join(",", this.atChannels);
    }

    public String getReplay() {
        return this.replay;
    }

    @Override
    public String toString() {
        return "MsgBuilder{" +
                "uuid='" + uuid + '\'' +
                ", roomId='" + roomId + '\'' +
                ", channelId='" + channelId + '\'' +
                ", msg='" + msg + '\'' +
                ", atUsers=" + atUsers +
                ", atRoles=" + atRoles +
                ", atChannels=" + atChannels +
                ", replay='" + replay + '\'' +
                '}';
    }
}
