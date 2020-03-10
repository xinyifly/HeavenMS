package server.gachapon;

public class Ellinia extends GachaponItems {
    @Override
    public int[] getCommonItems() {
        return new int[] {};
    }

    @Override
    public int[] getUncommonItems() {
        return new int[] {
            1372035, 1372036, 1372037, 1372038, 1372039, 1372040, 1372041, 1372042,
            1382045, 1382046, 1382047, 1382048, 1382049, 1382050, 1382051, 1382052
        };
    }

    @Override
    public int[] getRareItems() {
        return new int[] {
            1372032, 1382036, 1382037
        };
    }
}
