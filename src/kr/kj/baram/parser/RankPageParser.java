package kr.kj.baram.parser;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 특정 서버, 직업을 지정 받으면,
 * 해당 서버-직업의 모든 캐릭터를 수집한다.
 */
public class RankPageParser {
    private String serverCode;
    private String classCode;


    public RankPageParser(String serverName, String className) {
        this.serverCode = ParserConstant.serverCodeMap
                .getOrDefault(serverName, "");
        this.classCode = ParserConstant.classCodeMap
                .getOrDefault(className, "");
    }

    /**
     * 2023.08.05 kjkim
     * 주어진 서버, 직업에 해당하는 Ranking page를 전부 방문, 아이디 목록 전체를 가져온다.
     * @return
     */
    private List<String> getAllId() {
        if (this.serverCode == null || this.serverCode.isEmpty())
            return null;
        if (this.classCode == null || this.classCode.isEmpty())
            return null;

        String baseURL = String.format(
                "https://baram.nexon.com/Rank/List?maskGameCode=%s&codeGameJob=%s&n4Rank_start=",
                serverCode, classCode
        );

        int startIdx = 1;
        Connection conn;
        Document curDoc;
        Random tRand = new Random();

        List<String> resultList = new ArrayList<>();

        // 20 단위로 parsing
        try {
            while (true) {
                conn = Jsoup.connect(baseURL + startIdx)
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .userAgent(ParserConstant.agent)
                        .method(Connection.Method.GET)
                        .ignoreContentType(true);

                curDoc = conn.get();

                // 이상한 페이지일 경우 종료
                if (curDoc.selectFirst("#ErrorMessageInfo") != null)
                    break;

                // 데이터 없을 경우 종료
                Element element = curDoc.selectFirst("div.border_rank_list > table > tbody");
                if (element == null)
                    break;
                if (element.selectFirst("tr.no_data") == null)
                    break;

                // 결과 저장
                for (Element trGameIdAelement : element.select("tr > td.gameid > a")) {
                    resultList.add(trGameIdAelement.text().trim());
                }

                // 다음 위치로 이동
                startIdx += 20;

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
