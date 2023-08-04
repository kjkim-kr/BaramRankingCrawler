package kr.kj.baram.character;

/**
 * 2023.08.04 kjkim
 * Item Class
 */
public class ItemProperty {
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

    public CharConstant.ITEMCODE itemCode;
    public String itemName;

    public ItemProperty() {

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ItemProperty{");
        sb.append("itemCode=").append(itemCode);
        sb.append(", itemName='").append(itemName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
