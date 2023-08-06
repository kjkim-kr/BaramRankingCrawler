package kr.kj.baram.parser;

import kr.kj.baram.main.Utils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.SocketTimeoutException;
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

    private String serverName, className;


    public RankPageParser(String serverName, String className) {
        this.serverCode = ParserConstant.serverCodeMap
                .getOrDefault(serverName, "");
        this.classCode = ParserConstant.classCodeMap
                .getOrDefault(className, "");

        this.serverName = serverName;
        this.className = className;
    }

    /**
     * 2023.08.05 kjkim
     * 주어진 서버, 직업에 해당하는 Ranking page를 전부 방문, 아이디 목록 전체를 가져온다.
     * @return
     */
    public List<String> getAllId() {
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
        long st = System.currentTimeMillis();

        // 20 단위로 parsing
        try {
            int prevListSize = 0;
            int currentRetryCount = 0, totalRetryCount = 10;
            int loopCount = 0;

            // 최대 100,000 위 까지 존재한다.
            // 6차 이상만 수집하는 걸로 변경

            endWhile:
            while (startIdx <= 100000) {
                conn = Jsoup.connect(baseURL + startIdx)
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .userAgent(ParserConstant.agent)
                        .method(Connection.Method.GET)
                        .ignoreContentType(true);

                if (loopCount % 50 == 0) {
                    System.out.printf("\t(%s/%s) idx : %d (ela:%f) at %s\n",
                            serverName, className, startIdx,
                            (System.currentTimeMillis() - st) / 1000.0, Utils.getCurrentTime());
                    st = System.currentTimeMillis();
                }

                try {
                    curDoc = conn.get();

                    // 이상한 페이지일 경우 종료
                    if (curDoc.selectFirst("#ErrorMessageInfo") != null)
                        break;

                    // 데이터 없을 경우 종료
                    Element element = curDoc.selectFirst("div.border_rank_list > table > tbody");
                    if (element == null)
                        break;
                    if (element.selectFirst("tr.no_data") != null)
                        break;

                    // 결과 저장
//                    for (Element trGameIdAelement : element.select("tr > td.gameid > a")) {
//                        resultList.add(trGameIdAelement.text().trim());
//                    }
                    for(Element trElement : element.select("tr")) {
                        Element tdPromotionElement = trElement.selectFirst("td.promote");

                        if (tdPromotionElement != null) {
                            // 5차 이면 바로 종료
                            if (tdPromotionElement.text().equals("5"))
                                break endWhile;
                        }

                        Element tdGameIdAElement = trElement.selectFirst("td.gameid > a");
                        if(tdGameIdAElement != null) {
                            resultList.add(tdGameIdAElement.text().trim());
                        }
                    }

                    // 리스트 사이즈 변동 없으면 바로 종료
                    if (prevListSize == resultList.size())
                        break;

                    prevListSize = resultList.size();

                    // 다음 위치로 이동
                    startIdx += 20;
                    loopCount += 1;

                    // 1 ~ 2초 랜덤 휴식
                    try {
                        Thread.sleep(1000 + 10 * tRand.nextInt(100));
                    }catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                } catch(SocketTimeoutException socketTimeoutException) {
                    System.out.println("\tTimeOut 발생 : " + baseURL + startIdx);
                    currentRetryCount += 1;

                    if (currentRetryCount > totalRetryCount) {
                        System.out.println("\t\tRetry 횟수 초과로 종료");
                        break;
                    }

                    // 60초 대기
                    try {
                        Thread.sleep(60000 + 10 * tRand.nextInt(100));
                    }catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();

                    System.out.println("\tIoException 발생 : " + baseURL + startIdx);
                    currentRetryCount += 1;

                    if (currentRetryCount > totalRetryCount) {
                        System.out.println("\t\tRetry 횟수 초과로 종료");
                        break;
                    }

                    // 60초 대기
                    try {
                        Thread.sleep(60000 + 10 * tRand.nextInt(100));
                    }catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }
}
