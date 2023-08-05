package kr.kj.baram.main;

import kr.kj.baram.dao.MysqlModule;
import kr.kj.baram.parser.GuildListPageParser;
import kr.kj.baram.parser.ParserConstant;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParserMain {
    public static List<String> getGuildList(String serverName) {
        long st = System.currentTimeMillis();
        System.out.println("(Main-getGuildList) " + serverName + " 수집 시작... ");
        GuildListPageParser guildListPageParser = new GuildListPageParser(serverName);
        List<String> resultList = guildListPageParser.getAllId();

        System.out.println("(Main-getGuildList) " + serverName + " 수집 완료 -> "
                + " total " + resultList.size() + " items with elapsed : "
                + (System.currentTimeMillis() - st) / 1000.0);

        return resultList;
    }

    public static void main(String[] args) {
        // 문파 목록 전부 수집하기
        List<Thread> threadPool = new ArrayList<>();
        // "연" : {1234, 5678}, "무휼" : {123, 12, 3}, ...
        HashMap<String, List<String>> totalGuildMap = new HashMap<>();

        System.out.println("길드 목록 전체 수집 시작...");
        // 모든 서버에 대하여 각각의 Thread 사용
        for (String serverName : ParserConstant.serverCodeMap.keySet()) {
            Thread curServerThread = new Thread(
                    () -> totalGuildMap.put(serverName, getGuildList(serverName))
            );

            curServerThread.start();
            threadPool.add(curServerThread);
        }

        for (Thread curThread : threadPool) {
            try {
                curThread.join();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }

        System.out.println("길드 목록 전체 수집 완료");

        // 수집 완료한 list를 mysql에 넣고 정리
        System.out.println("Insert into target_guild_list... ");
        MysqlModule mysqlModule = Utils.getNewMysqlModule();
        mysqlModule.connect();

        String guildInsertQuery = "insert ignore into target_guild_list (guild_id, guild_server) values (?, ?)";
        try (PreparedStatement ps = mysqlModule.getConn().prepareStatement(guildInsertQuery)) {
            mysqlModule.getConn().setAutoCommit(false);

            int loopCount = 0;
            long st = System.currentTimeMillis();

            for (Map.Entry<String, String> serverEntry : ParserConstant.serverCodeMap.entrySet()) {
                // server name : "호동"
                if (totalGuildMap.containsKey(serverEntry.getKey())) {
                    String curServerCode = serverEntry.getValue();
                    List<String> curServerGuildIdList = totalGuildMap.get(serverEntry.getKey());

                    for (String curGuildId : curServerGuildIdList) {
                        ps.setString(1, curGuildId);
                        ps.setString(2, curServerCode);
                        ps.addBatch();

                        if (++loopCount % 1000 == 0) {
                            ps.executeBatch();
                            System.out.println("\tloop : " + loopCount
                                    + " / ela : " + (System.currentTimeMillis() - st) / 1000.0);
                            st = System.currentTimeMillis();
                        }
                    }
                }
            }
            // 마지막 남은 batch 실행
            ps.executeBatch();

            mysqlModule.getConn().setAutoCommit(true);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        mysqlModule.close();
    }
}
