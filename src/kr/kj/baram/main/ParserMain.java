package kr.kj.baram.main;

import kr.kj.baram.dao.MysqlModule;
import kr.kj.baram.guild.GuildProperty;
import kr.kj.baram.parser.GuildListPageParser;
import kr.kj.baram.parser.GuildPageParser;
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


    public static void parseGuildInfo(int numOfWorker) {
        MysqlModule mysqlModule = Utils.getNewMysqlModule();
        mysqlModule.connect();

        ArrayList<String> resList;
        resList = mysqlModule.runQuery(
                "select guild_id, guild_server from target_guild_list where used = 0"
        );
        int totalCount = resList.size();

        System.out.println("TotalCount : " + totalCount);     // 20,966

        // 20개의 worker 생산
        long st = System.currentTimeMillis();
        System.out.println("작업 시작 : " + Utils.getCurrentTime());
        List<Thread> threadPool = new ArrayList<>();

        int listPerSize = 1 + totalCount / numOfWorker;
        for (int workerNum = 0; workerNum < numOfWorker; workerNum++) {
            int sidx = workerNum * listPerSize;
            int eidx = Math.min(sidx + listPerSize, totalCount);
            int curWorkerNum = workerNum;
            List<String> curList = resList.subList(sidx, eidx);

            Thread curWorkerThread = new Thread(
                    () -> generateGuildInfo(curList, curWorkerNum)
            );

            curWorkerThread.start();
            threadPool.add(curWorkerThread);
        }

        for (Thread curThread : threadPool) {
            try {
                curThread.join();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }
        System.out.println("작업 종료 : " + Utils.getCurrentTime());
        System.out.println("소요 시간 : " + (System.currentTimeMillis() - st) / 1000.0);

        mysqlModule.close();
    }

    public static void generateGuildInfo(List<String> targetGuildList, int workerNumber) {
        MysqlModule mysqlModule = Utils.getNewMysqlModule();
        mysqlModule.connect();

        GuildPageParser guildPageParser = new GuildPageParser();

        String updateTargetGuildQuery = """
                update target_guild_list
                set used = 1, succeed = ?
                where guild_id = ? and guild_server = ?
                """;

        String guildInfoQuery = mysqlModule.getBaseInsertQuery("guild_infos");
        String guildRelationQuery = mysqlModule.getBaseInsertQuery("guild_relation");
        String guildMemberQuery = mysqlModule.getBaseInsertQuery("guild_members");

        long st = System.currentTimeMillis();
        System.out.println("* Worker " + workerNumber + " starts with data = " + targetGuildList.size());
        System.out.println(" - at " + Utils.getCurrentTime());

        try {
            mysqlModule.getConn().setAutoCommit(false);

            PreparedStatement updatePs = mysqlModule.getConn().prepareStatement(updateTargetGuildQuery);
            PreparedStatement guildInfoPs = mysqlModule.getConn().prepareStatement(guildInfoQuery);
            PreparedStatement guildRelPs = mysqlModule.getConn().prepareStatement(guildRelationQuery);
            PreparedStatement guildMemberPs = mysqlModule.getConn().prepareStatement(guildMemberQuery);

            int loopCnt = 0;
            for (String guildInfoData : targetGuildList) {
                // guild_id, guild_server
                String[] guildInfo = guildInfoData.split(mysqlModule.getBaseSplitChar());
                int succeed = 0;

                GuildProperty guildProperty = guildPageParser.parse(guildInfo[0], guildInfo[1]);
                if (guildProperty != null) {
                    succeed = 1;

                    // guild info 데이터 설정
                    String curGuildNameServer = guildProperty.guildNameServer; // 낙원@호동
                    String curGuildName = curGuildNameServer.substring(0, curGuildNameServer.indexOf('@'));
                    String curGuildServer = curGuildNameServer.substring(1 + curGuildNameServer.indexOf('@'));

                    guildInfoPs.setString(1, curGuildName);
                    guildInfoPs.setString(2, curGuildServer);
                    guildInfoPs.setString(3, guildProperty.guildNameCode);
                    guildInfoPs.setString(4, guildProperty.guildServerCode);
                    guildInfoPs.setString(5, guildProperty.castleName);
                    guildInfoPs.setString(6, guildProperty.leaderNameServer);
                    guildInfoPs.setString(7, guildProperty.subLeaderNameServer);

                    guildInfoPs.addBatch();

                    // guild relation 데이터 설정
                    for (String agreeGuildNameServer : guildProperty.agreeGuildNameServerList) {
                        guildRelPs.setString(1, curGuildServer);
                        guildRelPs.setString(2, curGuildName);
                        guildRelPs.setString(3,
                                agreeGuildNameServer.substring(0, agreeGuildNameServer.indexOf('@')));
                        guildRelPs.setInt(4, 0); // 동맹:0

                        guildRelPs.addBatch();
                    }

                    // guild relation 데이터 설정
                    for (String disAgreeGuildNameServer : guildProperty.disAgreeGuildNameServerList) {
                        guildRelPs.setString(1, curGuildServer);
                        guildRelPs.setString(2, curGuildName);
                        guildRelPs.setString(3,
                                disAgreeGuildNameServer.substring(0, disAgreeGuildNameServer.indexOf('@')));
                        guildRelPs.setInt(4, 1); // 적문:1

                        guildRelPs.addBatch();
                    }

                    // guild member 데이터 설정
                    for (String guildMemberNameServer : guildProperty.memberNameServerList) {
                        guildMemberPs.setString(1, curGuildName);
                        guildMemberPs.setString(2, curGuildServer);
                        guildMemberPs.setString(3, guildMemberNameServer);

                        guildMemberPs.addBatch();
                    }
                }

                // 사용/성공 유무 처리
                updatePs.setInt(1, succeed);
                updatePs.setString(2, guildInfo[0]);
                updatePs.setString(3, guildInfo[1]);
                updatePs.addBatch();

                // batch 실행
                if (++loopCnt % 1000 == 0) {
                    updatePs.executeBatch();
                    guildInfoPs.executeBatch();
                    guildRelPs.executeBatch();
                    guildMemberPs.executeBatch();
                }
            }

            // 남은 batch 처리
            updatePs.executeBatch();
            guildInfoPs.executeBatch();
            guildRelPs.executeBatch();
            guildMemberPs.executeBatch();

            // 종료
            updatePs.close();
            guildInfoPs.close();
            guildRelPs.close();
            guildMemberPs.close();
            mysqlModule.getConn().setAutoCommit(true);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        mysqlModule.close();

        System.out.println("* Worker " + workerNumber + " ends - at " + Utils.getCurrentTime());
        System.out.println("\tElapsed Time : " + (System.currentTimeMillis() - st)/1000.0);
    }


    public static void main(String[] args) {
//        getAllUsers();

//        parseGuildInfo(10);

    }
}
