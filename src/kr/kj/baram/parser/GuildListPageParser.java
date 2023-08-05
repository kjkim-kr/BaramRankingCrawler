package kr.kj.baram.parser;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 특정 서버를 받으면, 해당 서버의 모든 길드를 가져온다.
 */
public class GuildListPageParser {
    private String serverCode;


    public GuildListPageParser(String serverName) {
        this.serverCode = ParserConstant.serverCodeMap
                .getOrDefault(serverName, "");
    }

    /**
     * 2023.08.05 kjkim
     * 주어진 서버의 Guild List page를 전부 방문, 길드 목록 전체를 가져온다.
     * @return 모든 길드의 id (고유 id)
     */
    private List<String> getAllId() {
        if (this.serverCode == null || this.serverCode.isEmpty())
            return null;

        // https://baram.nexon.com/Guild/List/2?maskGameCode=131074
        String urlFormat = "https://baram.nexon.com/Guild/List/%d?maskGameCode=" + serverCode;

        int startIdx = 1;
        Connection conn;
        Document curDoc;
        Random tRand = new Random();

        List<String> resultList = new ArrayList<>();
        // 1 단위로 parsing
        try {
            while (true) {
                conn = Jsoup.connect(String.format(urlFormat, startIdx))
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .userAgent(ParserConstant.agent)
                        .method(Connection.Method.GET)
                        .ignoreContentType(true);

                curDoc = conn.get();

                // 이상한 페이지일 경우 종료
                if (curDoc.selectFirst("#ErrorMessageInfo") != null)
                    break;

                // 데이터 없을 경우 종료
                Element element = curDoc.selectFirst("div.guild_section > div.border_rank_list > table > tbody");
                if (element == null)
                    break;
                if (element.selectFirst("tr.no_data") == null)
                    break;

                // 결과 저장
                for (Element trNameAElement : element.select("tr > td.name > a")) {
                    // javascript:baram.goGuildInfo('1734@131073') -> 청아
                    String hrefInfo = trNameAElement.attr("href").trim();

                    if(hrefInfo.length() > 30 && hrefInfo.contains("@")) {
                        resultList.add(hrefInfo.substring(30, hrefInfo.indexOf('@')));
                    }
                }

                // 다음 위치로 이동
                startIdx ++;

                // 1 ~ 2초 랜덤 휴식
                try {
                    Thread.sleep(1000 + 10 * tRand.nextInt(100));
                }catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }
}
