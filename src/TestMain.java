import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TestMain {
    public static void main(String[] args) throws Exception{
        String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36";

        String baseRankURL = "https://baram.nexon.com/Profile/Info?character=";

        String encodedCharInfo = URLEncoder.encode("검귀@호동", StandardCharsets.UTF_8);

        System.out.println("https://baram.nexon.com");
        Connection conn = Jsoup.connect(baseRankURL + encodedCharInfo)
                .header("Content-Type", "application/json;charset=UTF-8")
                .userAgent(USER_AGENT)
                .method(Connection.Method.GET)
                .ignoreContentType(true);


        Document document = conn.get();

        System.out.println(document.html());
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