package com.github.mostroverkhov.firebase_rsocket;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface Logger {

    void log(Row row);

    class Row {
        private final String type;
        private final String uuid;
        private final Object payload;
        private final long timeStamp;

        public Row(String type,
                   String uuid,
                   Object payload,
                   long timeStamp) {
            this.type = type;
            this.payload = payload;
            this.uuid = uuid;
            this.timeStamp = timeStamp;
        }

        public String getType() {
            return type;
        }

        public String getUuid() {
            return uuid;
        }

        public Object payload() {
            return payload;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Row{");
            sb.append("type='").append(type).append('\'');
            sb.append(", uuid='").append(uuid).append('\'');
            sb.append(", payload=").append(payload);
            sb.append(", timeStamp=").append(timeStamp);
            sb.append('}');
            return sb.toString();
        }
    }
}
