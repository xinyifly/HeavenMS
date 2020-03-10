package server.gachapon;

public class MushroomShrine extends GachaponItems {
    @Override
    public int[] getCommonItems() {
        return new int[] {};
    }

    @Override
    public int[] getUncommonItems() {
        return new int[] {};
    }

    @Override
    public int[] getRareItems() {
        return new int[] {
            1102041, 1102042, 1102084, 1102086
        };
    }
}
