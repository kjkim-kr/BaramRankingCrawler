package kr.kj.baram.main;

import kr.kj.baram.dao.MysqlModule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {
    public static MysqlModule getNewMysqlModule() {
        MysqlModule mysqlModule = new MysqlModule.MysqlBuilder(
                "baram_root", "Baram!@#$1234",
                "127.0.0.1", "winbaram")
                .build();

        return mysqlModule;
    }

    public static String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();

        // 원하는 형식의 문자열로 변환합니다.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return now.format(formatter);
    }
}
