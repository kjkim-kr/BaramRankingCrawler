package kr.kj.baram.parser;

import java.util.Map;

/**
 * 2024.08.04 kjkim
 * parser 관련 상수
 */
public class ParserConstant {
    public static final String agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36";

    public static final String charInfoUrl = "https://baram.nexon.com/Profile/Info?character=";
    public static final String charGuildInfoUrl = "https://baram.nexon.com/Profile/GuildInfo?character=";


    public static final String guildInfoUrl = "https://baram.nexon.com/GuildInfo/Contents?clanName=";
    public static final String guildMemberUrl = "https://baram.nexon.com/GuildInfo/Member?clanName=";

    public static final Map<String, String> serverCodeMap = Map.ofEntries(
            Map.entry("진", "131089"),
            Map.entry("유리", "131086"),
            Map.entry("하자", "131087"),
            Map.entry("호동", "131088"),
            Map.entry("무휼", "131074"),
            Map.entry("연", "131073")
    );

    //전사 도적 주술사 도사 궁사 천인 마도사 영술사 차사 살수
    public static final Map<String, String> classCodeMap = Map.ofEntries(
            Map.entry("전사", "1"),
            Map.entry("도적", "2"),
            Map.entry("주술사", "3"),
            Map.entry("도사", "4"),
            Map.entry("궁사", "5"),
            Map.entry("천인", "6"),
            Map.entry("마도사", "7"),
            Map.entry("영술사", "8"),
            Map.entry("차사", "9"),
            Map.entry("살수", "10")
    );
}
