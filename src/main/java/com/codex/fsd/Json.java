package com.codex.fsd;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Json {
    private Json() {
    }

    public static Map<String, String> object(String json) {
        Parser parser = new Parser(json);
        return parser.parseObject();
    }

    public static String arrayMenu(List<MenuItem> menu) {
        List<String> items = new ArrayList<>();
        for (MenuItem item : menu) {
            items.add(item.toJson());
        }
        return arrayValues(items);
    }

    public static String arrayOrders(List<Order> orders) {
        List<String> items = new ArrayList<>();
        for (Order order : orders) {
            items.add(order.toJson());
        }
        return arrayValues(items);
    }

    public static String arrayValues(List<String> jsonValues) {
        return "[" + String.join(",", jsonValues) + "]";
    }

    public static String pair(String key, String value) {
        return quote(key) + ":" + quote(value);
    }

    public static String pair(String key, int value) {
        return quote(key) + ":" + value;
    }

    public static String pair(String key, boolean value) {
        return quote(key) + ":" + value;
    }

    public static String rawPair(String key, String jsonValue) {
        return quote(key) + ":" + jsonValue;
    }

    public static String error(String message) {
        return "{" + pair("error", message) + "}";
    }

    public static String quote(String value) {
        StringBuilder output = new StringBuilder("\"");
        for (char character : value.toCharArray()) {
            switch (character) {
                case '"' -> output.append("\\\"");
                case '\\' -> output.append("\\\\");
                case '\n' -> output.append("\\n");
                case '\r' -> output.append("\\r");
                case '\t' -> output.append("\\t");
                default -> output.append(character);
            }
        }
        return output.append("\"").toString();
    }

    private static final class Parser {
        private final String json;
        private int index;

        private Parser(String json) {
            this.json = json == null ? "" : json.trim();
        }

        private Map<String, String> parseObject() {
            Map<String, String> values = new LinkedHashMap<>();
            skipWhitespace();
            expect('{');
            skipWhitespace();
            if (peek() == '}') {
                index++;
                return values;
            }

            while (index < json.length()) {
                String key = parseString();
                skipWhitespace();
                expect(':');
                skipWhitespace();
                String value = parseValue();
                values.put(key, value);
                skipWhitespace();
                char separator = peek();
                if (separator == ',') {
                    index++;
                    skipWhitespace();
                    continue;
                }
                if (separator == '}') {
                    index++;
                    return values;
                }
                throw new IllegalArgumentException("Invalid JSON object");
            }

            throw new IllegalArgumentException("Invalid JSON object");
        }

        private String parseValue() {
            if (peek() == '"') {
                return parseString();
            }
            int start = index;
            while (index < json.length() && ",}".indexOf(json.charAt(index)) == -1) {
                index++;
            }
            return json.substring(start, index).trim();
        }

        private String parseString() {
            expect('"');
            StringBuilder output = new StringBuilder();
            while (index < json.length()) {
                char character = json.charAt(index++);
                if (character == '"') {
                    return output.toString();
                }
                if (character == '\\') {
                    if (index >= json.length()) {
                        throw new IllegalArgumentException("Invalid JSON escape");
                    }
                    char escaped = json.charAt(index++);
                    switch (escaped) {
                        case '"' -> output.append('"');
                        case '\\' -> output.append('\\');
                        case 'n' -> output.append('\n');
                        case 'r' -> output.append('\r');
                        case 't' -> output.append('\t');
                        default -> output.append(escaped);
                    }
                    continue;
                }
                output.append(character);
            }
            throw new IllegalArgumentException("Unterminated JSON string");
        }

        private void expect(char expected) {
            if (peek() != expected) {
                throw new IllegalArgumentException("Expected '" + expected + "'");
            }
            index++;
        }

        private char peek() {
            if (index >= json.length()) {
                throw new IllegalArgumentException("Unexpected end of JSON");
            }
            return json.charAt(index);
        }

        private void skipWhitespace() {
            while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
                index++;
            }
        }
    }
}
