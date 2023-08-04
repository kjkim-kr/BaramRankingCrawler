package kr.kj.baram.guild;

import java.util.List;

public class GuildProperty {
    public String guildNameServer;

    public String guildNameCode;
    public String guildServerCode;

    public String castleName;
    public String leaderNameServer;
    public String subLeaderNameServer;

    public List<String> memberNameServerList;

    public List<String> agreeGuildNameServerList;
    public List<String> disAgreeGuildNameServerList;

    public GuildProperty() {

    }

    public GuildProperty setGuildNameServer(String guildNameServer) {
        this.guildNameServer = guildNameServer;
        return this;
    }

    public GuildProperty setGuildNameCode(String guildNameCode) {
        this.guildNameCode = guildNameCode;
        return this;
    }

    public GuildProperty setGuildServerCode(String guildServerCode) {
        this.guildServerCode = guildServerCode;
        return this;
    }

    public GuildProperty setCastleName(String castleName) {
        this.castleName = castleName;
        return this;
    }

    public GuildProperty setLeaderNameServer(String leaderNameServer) {
        this.leaderNameServer = leaderNameServer;
        return this;
    }

    public GuildProperty setSubLeaderNameServer(String subLeaderNameServer) {
        this.subLeaderNameServer = subLeaderNameServer;
        return this;
    }

    public GuildProperty setMemberNameServerList(List<String> memberNameServerList) {
        this.memberNameServerList = memberNameServerList;
        return this;
    }

    public GuildProperty setAgreeGuildNameServerList(List<String> agreeGuildNameServerList) {
        this.agreeGuildNameServerList = agreeGuildNameServerList;
        return this;
    }

    public GuildProperty setDisAgreeGuildNameServerList(List<String> disAgreeGuildNameServerList) {
        this.disAgreeGuildNameServerList = disAgreeGuildNameServerList;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GuildProperty{");
        sb.append("guildNameServer='").append(guildNameServer).append('\'');
        sb.append(", guildNameCode='").append(guildNameCode).append('\'');
        sb.append(", guildServerCode='").append(guildServerCode).append('\'');
        sb.append(", castleName='").append(castleName).append('\'');
        sb.append(", leaderNameServer='").append(leaderNameServer).append('\'');
        sb.append(", subLeaderNameServer='").append(subLeaderNameServer).append('\'');
        sb.append(", agreeGuildNameServerList=").append(agreeGuildNameServerList);
        sb.append(", disAgreeGuildNameServerList=").append(disAgreeGuildNameServerList);
        sb.append(", memberNameServerList=").append(memberNameServerList);
        sb.append('}');
        return sb.toString();
    }
}
