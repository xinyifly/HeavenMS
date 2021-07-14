package server.gachapon;

public class Sleepywood extends GachaponItems {
    @Override
    public int[] getCommonItems() {
        return new int[] {};
    }

    @Override
    public int[] getUncommonItems() {
        return new int[] {
            2040915, 2040920,
            2040816
        };
    }

    @Override
    public int[] getRareItems() {
        return new int[] {
            2040917, 2040922,
            2040815
        };
    }
}
