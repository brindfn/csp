package com.intrasoft.csp.client;

public enum DateMath {
    NOW("now"),
    ONE_DAY("1d"),
    ONE_WEEK("1w"),
    ONE_MONTH("1m"),
    ONE_YEAR("1y"),
    THREE_YEARS("3y");  // To be removed (exists for testing purposes)

    private final String value;

    DateMath(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
