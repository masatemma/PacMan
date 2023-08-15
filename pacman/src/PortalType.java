package src;

public enum PortalType {
    White,
    Yellow,
    DarkGold,
    DarkGray;

    public String getImageName() {
        switch (this) {
            case White: return "i_portalWhiteTile.png";
            case Yellow: return "j_portalYellowTile.png";
            case DarkGold: return "k_portalDarkGoldTile.png";
            case DarkGray: return "l_portalDarkGrayTile.png";
            default: {
                assert false;
            }
        }
        return null;
    }
}
