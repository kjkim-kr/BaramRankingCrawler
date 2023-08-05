package kr.kj.baram.main;

import kr.kj.baram.dao.MysqlModule;

public class Utils {
    public static MysqlModule getNewMysqlModule() {
        MysqlModule mysqlModule = new MysqlModule.MysqlBuilder(
                "baram_root", "Baram!@#$1234",
                "127.0.0.1", "winbaram")
                .build();

        return mysqlModule;
    }
}
