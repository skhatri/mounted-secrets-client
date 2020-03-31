package com.github.skhatri.mounted.model;

public enum ErrorDecision {
    FAIL() {
        @Override
        public String handleErrorForKey(String key) {
            throw new IllegalArgumentException(String.format("no value found for key %s", key));
        }
    }, EMPTY() {
        @Override
        public String handleErrorForKey(String key) {
            return "";
        }
    }, IDENTITY() {
        @Override
        public String handleErrorForKey(String key) {
            return key;
        }
    };

    public abstract String handleErrorForKey(String key);


}
