package nl.quintor.studybits.university.enums;

public enum ExchangePositionState {
    OPEN(0),
    CLOSED(1);

    private final int value;

    ExchangePositionState(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public ExchangePositionState getState(int value) {
        switch (value) {
            case 0:
                return OPEN;
            case 1:
                return CLOSED;
            default:
                return OPEN;
        }
    }
}