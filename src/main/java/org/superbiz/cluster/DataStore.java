package org.superbiz.cluster;

public interface DataStore {

    void set(final String key, final String value);

    String get(final String key);

}
