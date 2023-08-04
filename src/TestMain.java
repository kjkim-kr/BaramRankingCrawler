
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TestMain {
    public static void main(String[] args) throws Exception {
//        String agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36";
//        String curURL = "https://baram.nexon.com/Profile/Info?character=" + URLEncoder.encode("검귀" + "@호동", StandardCharsets.UTF_8);
//        System.out.println("cur:" +curURL);
//        Connection conn = Jsoup.connect(curURL)
//                .header("Content-Type", "application/json;charset=UTF-8")
//                .userAgent(agent)
//                .method(Connection.Method.GET)
//                .ignoreContentType(true);
//
//        Document doc = conn.get();

        JSONParser ps = new JSONParser();

        Document docFromFile = Jsoup.parse(new File("test_character.html"));
        Elements scriptTags = docFromFile.select("div.contents > script[type=text/javascript]");

        if (scriptTags.size() == 2) {
            String scriptCode = scriptTags.get(1).html();

            // JavaScript 변수 A의 값을 가져오기
            String varAValue = getJavaScriptVariableValue(scriptCode, "_equipItem");
            if(!varAValue.isEmpty()) {
                // item이 없을 경우 아예 object가 없다.
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
                JSONArray jsonArray = (JSONArray) ps.parse(varAValue);
                System.out.println("size : " + jsonArray.size());
            }
        }

        Element contInfo = docFromFile.selectFirst("div.cont_box > div.inner");
        if(contInfo != null) {
            Element findGuild = contInfo.selectFirst("div.tab_menu4 > ul");
            if(findGuild != null) {
                Elements liElements = findGuild.select("li > a");
                // 문파가 있는 경우 => baram.goCharacterInfo('/Profile/GuildInfo','검귀@호동');
                // 문파가 없는 경우 => alert('가입한 문파가 없습니다.')
                // 문파

                liElements.get(1).attr("onclick");

            }
        }

//        Element charArea = docFromFile.selectFirst("div.chr_area ul");
//        if(charArea != null) {
//            for(Element li : charArea.select("li")) {
//                System.out.println(li.selectFirst("strong").text() + " -> " + li.selectFirst("span.system").text());
//            }
//        }
    }

    // JavaScript 변수의 값을 추출하는 메서드
    public static String getJavaScriptVariableValue(String scriptCode, String variableName) {
        String pattern = variableName + "\\s*=\\s*(.*?);";
        return scriptCode.replaceAll("(?s).*?" + pattern + ".*", "$1").trim();
    }
}

/*
https://baram.nexon.com/Profile/Info?character=%EA%B2%80%EA%B7%80%40%ED%98%B8%EB%8F%99

랭킹

1) 서버
2) 서버 - 직업
3) 서버 - 직업 - 캐릭터

- 등수

등수가 없는 경우
: https://baram.nexon.com/Rank/List?maskGameCode=131088&n4Rank_start=100001&codeGameJob=1

4) 상세정보
캐릭터
https://baram.nexon.com/Profile/Info?character=검귀@호동
 레벨 / 랭킹 / 승급차수 / 직업 / 국가 / 부부

캐릭의 문파 정보
https://baram.nexon.com/Profile/GuildInfo?character=검귀@호동

 바람 성 / 문파명 / 문주 / 부문주 / ... / 동맹 문파 / 적대 문파

- 문파가 없는 경우
=> https://baram.nexon.com/Profile/Info?character=벽쿵@호동



문파 자체의 정보
https://baram.nexon.com/Guild/List/2?maskGameCode=131073 (*연/2페이지)
-> 450이 마지막 페이지
(20개씩 보여주는데, 20개가 되지 않으면 마무리)
딱 20개인 경우?

-> https://baram.nexon.com/Guild/List/451?maskGameCode=131073
(넘어갈 경우)


- 문파 상세

(문파 상세)
https://baram.nexon.com/GuildInfo/Contents?clanName=8000755@131088

바람 성 / 문파명 / 문주 / 부문주 / 문파원 수
동맹 문파
적대 문파

(문파 구성원)
https://baram.nexon.com/GuildInfo/Member?clanName=8000755@131088

 */