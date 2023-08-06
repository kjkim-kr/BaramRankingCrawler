package kr.kj.baram.main;

import kr.kj.baram.dao.MysqlModule;
import kr.kj.baram.parser.GuildListPageParser;
import kr.kj.baram.parser.ParserConstant;
import kr.kj.baram.parser.RankPageParser;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParserMain {
    // user id 수집기
    public static List<String> getUserList(String serverName, String className) {
        long st = System.currentTimeMillis();

        System.out.println("(Main-getUserList) " + serverName + " / " + className + " 수집 시작... ");
        RankPageParser rankPageParser = new RankPageParser(serverName, className);
        List<String> resultList = rankPageParser.getAllId();

        System.out.println("(Main-getUserList) " + serverName + " / " + className + " 수집 완료 -> "
                + " total " + resultList.size() + " items with elapsed : "
                + (System.currentTimeMillis() - st) / 1000.0);

        return resultList;
    }

    public static void getAllUsers() {
        // 60개의 Thread 보다는 10 * 6으로 해야할 듯.
        // "호동_전사": {검귀, ...}, "호동_도적": {리신장인, ... }
        MysqlModule mysqlModule = Utils.getNewMysqlModule();
        mysqlModule.connect();
        String userInsertQuery = "insert ignore into target_user_list (user_id, user_server) values (?, ?)";

        for (String serverName : ParserConstant.serverCodeMap.keySet()) {
            List<Thread> threadPool = new ArrayList<>();
            HashMap<String, List<String>> totalUserMap = new HashMap<>();

            long threadSt = System.currentTimeMillis();
            System.out.println(serverName + " 유저 목록 수집 시작... " + Utils.getCurrentTime());
            // 모든 서버에 대하여 각각의 Thread 사용

            for (String className : ParserConstant.classCodeMap.keySet()) {
                Thread curServerThread = new Thread(
                        () -> totalUserMap.put(
                                serverName + "_" + className,
                                getUserList(serverName, className)
                        )
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

            System.out.println(serverName + " 유저 목록 전체 수집 완료 " + Utils.getCurrentTime());
            System.out.println(" -> elapsed: " + (System.currentTimeMillis() - threadSt) / 1000.0);


            // 수집 완료한 list를 mysql에 넣고 정리
            System.out.println("Insert into target_user_list... ");

            try (PreparedStatement ps = mysqlModule.getConn().prepareStatement(userInsertQuery)) {
                mysqlModule.getConn().setAutoCommit(false);

                int loopCount = 0;
                long st = System.currentTimeMillis();

                for (String curServerName : ParserConstant.serverCodeMap.keySet()) {
                    for (String curClassName : ParserConstant.classCodeMap.keySet()) {
                        // server name : "호동"
                        String totalUserMapKey = curServerName + "_" + curClassName;

                        if (totalUserMap.containsKey(totalUserMapKey)) {
                            List<String> curServerUserIdList = totalUserMap.get(totalUserMapKey);

                            for (String curUserId : curServerUserIdList) {
                                ps.setString(1, curUserId);
                                ps.setString(2, curServerName);
                                ps.addBatch();

                                if (++loopCount % 10000 == 0) {
                                    ps.executeBatch();
                                    System.out.println("\tloop : " + loopCount
                                            + " / ela : " + (System.currentTimeMillis() - st) / 1000.0);
                                    st = System.currentTimeMillis();
                                }
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
        }

        mysqlModule.close();
    }


    // 문파 수집기
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

    public static void getAllGuilds() {
        // 문파 목록 전부 수집하기
        List<Thread> threadPool = new ArrayList<>();
        // "연" : {1234, 5678}, "무휼" : {123, 12, 3}, ...
        HashMap<String, List<String>> totalGuildMap = new HashMap<>();

        long threadSt = System.currentTimeMillis();
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
        System.out.println("-> elapsed: " + (System.currentTimeMillis() - threadSt) / 1000.0);

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

    public static void main(String[] args) {
//        getAllUsers();
    }
}
