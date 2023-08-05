package kr.kj.baram.parser;

import kr.kj.baram.guild.GuildProperty;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class GuildPageParser {
    public GuildPageParser() {

    }

    public GuildProperty parse(String guildNameCode, String guildServerCode) {
        // https://baram.nexon.com/GuildInfo/Contents?clanName=2000237@131088
        // https://baram.nexon.com/GuildInfo/Member?clanName=2000237@131088
        if (guildNameCode == null || guildNameCode.isEmpty()) return null;
        if (guildServerCode == null || guildServerCode.isEmpty()) return null;

        GuildProperty guildProperty;

        try {
            Connection conn;
            Document curDoc;

            // 기본 정보 parsing
            // 페이지 정보가 없을 수도 있음.
            conn = Jsoup.connect(ParserConstant.guildInfoUrl + guildNameCode + "@" + guildServerCode)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .userAgent(ParserConstant.agent)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true);

            curDoc = conn.get();
            if (isErrorPage(curDoc)) return null;

            Element baseElem = curDoc.selectFirst("div.guild_section.first > dl.guild_info");
            if (baseElem == null) return null;

            HashMap<String, String> guildInfoMap = getGuildInfo(baseElem);

            Elements guildListElems = curDoc.select("div.guild_section > div.guild_list");
            // 2개가 발견되며, 처음은 동맹, 2번째는 적대 문파
            if (guildListElems.size() != 2) return null;

            List<String> agreeGuildList = getGuildList(guildListElems.get(0));
            List<String> disAgreeGuildList = getGuildList(guildListElems.get(1));

            // 0.5초 휴식
            Thread.sleep(500);

            // member list
            // 페이지 정보가 없을 수도 있음.
            conn = Jsoup.connect(ParserConstant.guildMemberUrl + guildNameCode + "@" + guildServerCode)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .userAgent(ParserConstant.agent)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true);

            curDoc = conn.get();
            if (isErrorPage(curDoc)) return null;

            // member parsing
            List<String> memberList = curDoc.select("ul.member_list > li").stream()
                    .map(elem -> elem.selectFirst("em.name > a"))
                    .filter(Objects::nonNull)
                    .map(Element::text)
                    .map(String::trim)
                    .toList();

            // 결과 데이터 작성
            guildProperty = new GuildProperty();

            guildProperty
                    .setGuildNameCode(guildNameCode)
                    .setGuildServerCode(guildServerCode)
                    .setCastleName(guildInfoMap.get("바람성"))
                    .setGuildNameServer(guildInfoMap.get("문파명"))
                    .setLeaderNameServer(guildInfoMap.get("문주"))
                    .setSubLeaderNameServer(guildInfoMap.get("부문주"))
                    .setAgreeGuildNameServerList(agreeGuildList)
                    .setDisAgreeGuildNameServerList(disAgreeGuildList)
                    .setMemberNameServerList(memberList)
            ;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return guildProperty;
    }

    private List<String> getGuildList(Element guildListElem) {
        return (guildListElem.selectFirst("div.search_not_found.no_data_line") == null)?
                guildListElem.select("a").stream()
                        .map(elem -> elem.text().trim())
                        .toList()
                : new ArrayList<>();
    }

    private HashMap<String, String> getGuildInfo(Element guildElem) {
        HashMap<String, String> resMap = new HashMap<>();

        Elements ddElems = guildElem.select("dd");
        if (ddElems.size() > 3) {
            resMap.put("바람성", ddElems.get(0).text().trim());
            resMap.put("문파명", ddElems.get(1).text().trim());
            resMap.put("문주", ddElems.get(2).text().trim());
            resMap.put("부문주", ddElems.get(3).text().trim());
        }

        return resMap;
    }


    /**
     * Error Page 인지 유무를 확인한다.
     * 찾는 코드 -> <p id="ErrorMessageInfo">
     *
     * @param curDoc
     * @return
     */
    private boolean isErrorPage(Element curDoc) {
        return curDoc.selectFirst("#ErrorMessageInfo") != null;
    }
}
