public enum Direction {
    E(0,1), W(0,-1), N(-1,0), S(1,0), NE(-1,1), SW(1,-1), SE(1,1), NW(-1,-1);
    public final int dr; public final int dc;
    Direction(int dr, int dc){
        this.dr = dr;
        this.dc = dc;
    }
    public static Direction opposite(Direction d){
        switch(d){
            case E:
                return W;
            case W:
                return E;
            case N:
                return S;
            case S:
                return N;
            case NE:
                return SW;
            case SW:
                return NE;
            case SE:
                return NW;
            default:
                return SE;
        }
    }
}
