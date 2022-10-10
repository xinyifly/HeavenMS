package server.gachapon;

public class Global extends GachaponItems {
    @Override
    public int[] getCommonItems() {
        return new int[] {
            2000004, 2000005
        };
    }

    @Override
    public int[] getUncommonItems() {
        return new int[] {
            2022179, 2022273, 2022282
        };
    }

    @Override
    public int[] getRareItems() {
        return new int[] {
            2049100
        };
    }
}
