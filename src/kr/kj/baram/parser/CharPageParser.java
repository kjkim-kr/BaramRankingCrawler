package kr.kj.baram.parser;

import kr.kj.baram.character.CharConstant;
import kr.kj.baram.character.CharProperty;
import kr.kj.baram.character.ItemProperty;
import kr.kj.baram.main.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 2023.08.04 kjkim
 * page : https://baram.nexon.com/Profile/Info?character=주애%40호동 을 주면
 * char property 객체를 return 해줌
 */
public class CharPageParser {
    private JSONParser ps;

    public CharPageParser() {
        ps = new JSONParser();
    }

    public CharProperty parse(String myId, String myServer) {
        CharProperty charProperty;

        if (myId == null || myId.isEmpty()) return null;
        if (myServer == null || myServer.isEmpty()) return null;

        try {
            try {
                String encodedIdServer = URLEncoder.encode(myId + "@" + myServer, StandardCharsets.UTF_8);

                Connection conn;
                Document curDoc;
                charProperty = new CharProperty();

                // 기본 정보 parsing
                // 페이지 정보가 없을 수도 있음.
                conn = Jsoup.connect(ParserConstant.charInfoUrl + encodedIdServer)
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .userAgent(ParserConstant.agent)
                        .method(Connection.Method.GET)
                        .ignoreContentType(true);

                curDoc = conn.get();
                if (isErrorPage(curDoc)) return null;

                Element charArea = curDoc.selectFirst("div.chr_area ul");
                if (charArea == null)
                    return null;

                HashMap<String, String> baseMap = getCharacterInfo(charArea);
                List<ItemProperty> itemPropertyList = getItemList(
                        curDoc.select("div.contents > script[type=text/javascript]")
                );

                // 0.5초 휴식
                Thread.sleep(500);
                // 문파 정보 parsing
                // 페이지 정보가 없을 수도 있음.

                conn = Jsoup.connect(ParserConstant.charGuildInfoUrl + encodedIdServer)
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .userAgent(ParserConstant.agent)
                        .method(Connection.Method.GET)
                        .ignoreContentType(true);

                curDoc = conn.get();

                String guildName = "";
                if (!isErrorPage(curDoc)) {
                    Elements ddElems = curDoc.select("dl.guild_info > dd");
                    if (ddElems.size() > 1) {
                        String guildFullName = ddElems.get(1).text();   // XXYYZZ@호동
                        int atIndex = guildFullName.indexOf('@');
                        // If '@' is not present, take the whole text.
                        guildName = (atIndex != -1) ? guildFullName.substring(0, atIndex) : guildFullName;
                    }
                }

                // 데이터 설정
                charProperty.setMyName(myId)
                        .setMyServer(myServer)
                        .setLevel(Integer.parseInt(baseMap.get("레벨")))
                        .setRanking(Integer.parseInt(baseMap.get("랭킹")))
                        .setPromotion(Integer.parseInt(baseMap.get("승급차수")))
                        .setJob(baseMap.get("직업"))
                        .setCountry(baseMap.get("국가"))
                        .setCoupleNameServer(baseMap.get("부부"))
                        .setEquipItem(itemPropertyList)
                        .setGuildName(guildName)
                ;
            }
            catch (SocketTimeoutException socketTimeoutException) {
                System.out.println("Socket TimeOut : " + Utils.getCurrentTime());
                return null;
            }
            catch(IOException socketTimeoutException) {
                socketTimeoutException.printStackTrace();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return charProperty;
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


    /**
     * 주어진 객체에서 캐릭터 기본 정보를 가져온다.
     * 가져오는 정보는 (레벨, 랭킹, 승급차수, 직업, 국가, 부부) 정보이다.
     *
     * @param charAreaElem
     * @return (레벨, 랭킹, 승급차수, 직업, 국가, 부부)
     */
    private HashMap<String, String> getCharacterInfo(Element charAreaElem) {
        return charAreaElem.select("li").stream()
                .filter(li -> li.selectFirst("strong") != null)
                .collect(Collectors.toMap(
                                li -> li.selectFirst("strong").text(),
                                li -> li.selectFirst("span.system").text(),
                                (existingValue, newValue) -> existingValue, // (value, value) 중복 발생 시 기존 값을 유지
                                HashMap::new
                        )
                );
    }

    /**
     * 주어진 scriptTags에서 _equipItem을 찾아서 해당 객체를 파싱하여 리턴한다
     *
     * @param scriptTags "div.contents > script[type=text/javascript]"로 찾은 객체
     * @return itemList
     */
    private List<ItemProperty> getItemList(Elements scriptTags) {
        List<ItemProperty> resultList = new ArrayList<>();

        // 2일 때만 parsing
        if (scriptTags.size() == 2) {
            String scriptCode = scriptTags.get(1).html();
            String pattern = "_equipItem\\s*=\\s*(.*?);";

            // JavaScript 변수 A의 값을 가져오기
            String varAValue = scriptCode.replaceAll("(?s).*?" + pattern + ".*", "$1").trim();
            if (!varAValue.isEmpty()) {
                // item이 없을 경우 아예 object가 없다.
                /*
                {
                    "slot": 1,
                    "partname": "weapon",
                    "isCash": 0,
                    "name": "고대마령의적혈봉",
                    "tile": -1,
                    "Itemcode": -1,
                    "renderOpt": -1,
                    "attr": ""
                }
                 */
                try {
                    JSONArray jsonArray = (JSONArray) ps.parse(varAValue);

                    for (Object o : jsonArray) {
                        JSONObject curObj = (JSONObject) o;
                        int itemCodeValue = ((Long) curObj.get("slot")).intValue();
                        String itemName = (String) curObj.get("name");

                        CharConstant.ITEMCODE curItemCode = CharConstant.getItemCodeByValue(itemCodeValue);
                        if (curItemCode != CharConstant.ITEMCODE.NONE) {
                            ItemProperty itemProperty = new ItemProperty();
                            itemProperty.itemCode = curItemCode;
                            itemProperty.itemName = itemName;

                            resultList.add(itemProperty);
                        }
                    }
                } catch (ParseException parseException) {
                    parseException.printStackTrace();
                }
            }
        }

        return resultList;
    }
}
