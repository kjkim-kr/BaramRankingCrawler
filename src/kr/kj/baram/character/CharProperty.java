package kr.kj.baram.character;

import java.util.List;
import java.util.StringJoiner;

/**
 * 2023.08.04 kjkim
 * Character Class
 */
public class CharProperty {
    // 기본 정보
    public String myName;
    public String myServer;

    public int level;
    public int ranking;
    public int promotion;
    public String job;
    public String country;

    public String coupleNameServer;   // 부부의 서버는 나와 같다

    public String guildName;    // 길드 정보. 서버는 나와 같다.

    // 아이템 장착 정보
    /*
    일반
    목(27)    투구(4)  얼굴(22)
    무기(1)   갑옷(2)  방패(3)
    왼손(7)   망토(24) 오른손(8)
    보조1(20) 신발(26) 보조2(21)
    노리개(9) 분신(34)

    캐시
    목(33) 투구(23) 얼굴(30)
    무기(28) 갑옷(25) 방패(29)
    X 망토(31) X
    X 신발(32) X
    장신구(10) 올레이어(35)
     */
    public List<ItemProperty> equipItem;

    public CharProperty setMyName(String myName) {
        this.myName = myName;
        return this;
    }

    public CharProperty setMyServer(String myServer) {
        this.myServer = myServer;
        return this;
    }

    public CharProperty setLevel(int level) {
        this.level = level;
        return this;
    }

    public CharProperty setRanking(int ranking) {
        this.ranking = ranking;
        return this;
    }

    public CharProperty setPromotion(int promotion) {
        this.promotion = promotion;
        return this;
    }

    public CharProperty setJob(String job) {
        this.job = job;
        return this;
    }

    public CharProperty setCountry(String country) {
        this.country = country;
        return this;
    }

    public CharProperty setCoupleNameServer(String coupleNameServer) {
        this.coupleNameServer = coupleNameServer;
        return this;
    }

    public CharProperty setGuildName(String guildName) {
        this.guildName = guildName;
        return this;
    }

    public CharProperty setEquipItem(List<ItemProperty> equipItem) {
        this.equipItem = equipItem;
        return this;
    }

    public CharProperty() {

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CharProperty{");
        sb.append("myName='").append(myName).append('\'');
        sb.append(", myServer='").append(myServer).append('\'');
        sb.append(", level=").append(level);
        sb.append(", ranking=").append(ranking);
        sb.append(", promotion=").append(promotion);
        sb.append(", job='").append(job).append('\'');
        sb.append(", country='").append(country).append('\'');
        sb.append(", coupleNameServer='").append(coupleNameServer).append('\'');
        sb.append(", guildName='").append(guildName).append('\'');
        sb.append(", equipItem=").append(equipItem);
        sb.append('}');
        return sb.toString();
    }
}
