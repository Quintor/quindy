package nl.quintor.studybits.enums;

public enum ExchangeApplicationState {
    APPLIED(0),
    REJECTED(1),
    ACCEPTED(2);

    private final int value;

    ExchangeApplicationState(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public ExchangeApplicationState getState(int value) {
        switch (value) {
            case 0:
                return APPLIED;
            case 1:
                return REJECTED;
            case 2:
                return ACCEPTED;
            default:
                return APPLIED;
        }
    }
}