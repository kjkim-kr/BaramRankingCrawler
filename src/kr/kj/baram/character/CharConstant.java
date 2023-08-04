package kr.kj.baram.character;

/**
 * 2023.08.04 kjkim
 * character 관련 상수
 */
public class CharConstant {


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
    public enum ITEMCODE {
        NORMAL_NECK(27), NORMAL_HELMET(4), NORMAL_FACE(22),
        NORMAL_WEAPON(1), NORMAL_ARMOR(2), NORMAL_SHIELD(3),
        NORMAL_LEFT(7), NORMAL_CAPE(24), NORMAL_RIGHT(8),
        NORMAL_LSUB(20), NORMAL_SHOES(26), NORMAL_RSUB(21),
        NORMAL_JEWEL(9), NORMAL_AVATAR(34),

        CASH_NECK(33), CASH_HELMET(23), CASH_FACE(30),
        CASH_WEAPON(28), CASH_ARMOR(25), CASH_SHIELD(29),
        CASH_CAPE(31),
        CASH_SHOES(32),
        CASH_JEWEL(10), CASH_AVATAR(35),

        NONE(-1)
        ;

        private int value;

        ITEMCODE(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static ITEMCODE getItemCodeByValue(int value) {
        for(ITEMCODE itemcode : ITEMCODE.values()) {
            if (itemcode.getValue() == value) {
                return itemcode;
            }
        }

        return ITEMCODE.NONE;
    }
}
