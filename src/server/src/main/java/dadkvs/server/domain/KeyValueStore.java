package dadkvs.server.domain;

import dadkvs.server.domain.classes.TransactionRecord;

public class KeyValueStore {
    private int size;
    private VersionedValue[] values;

    public KeyValueStore(int n_entries) {
        this.size = n_entries;
        this.values = new VersionedValue[n_entries];
        for (int i = 0; i < n_entries; i++) {
            this.values[i] = new VersionedValue(0, 0);
        }
    }

    public synchronized int getConfig() {
        return this.values[0].getValue();
    }

    public synchronized VersionedValue read(int k) {
        if (k < size) {
            return values[k];
        } else {
            return null;
        }
    }

    public synchronized boolean write(int k, VersionedValue v) {
        if (k < size) {
            values[k] = v;
            return true;
        } else return false;
    }

    public synchronized boolean commit(TransactionRecord tr) {
        // System.out.println(
        //         "store commit read first key = "
        //                 + tr.getRead1Key()
        //                 + " with version = "
        //                 + tr.getRead1Version()
        //                 + "  and current version = "
        //                 + this.read(tr.getRead1Key()).getVersion());
        // System.out.println(
        //         "store commit read second key = "
        //                 + tr.getRead2Key()
        //                 + " with version = "
        //                 + tr.getRead2Version()
        //                 + " and current version = "
        //                 + this.read(tr.getRead2Key()).getVersion());
        // System.out.println(
        //         "store commit write key  "
        //                 + tr.getPrepareKey()
        //                 + " with value = "
        //                 + tr.getPrepareValue()
        //                 + " and version "
        //                 + tr.getTimestamp());

        if (
            this.read(tr.getRead1Key()).getVersion() == tr.getRead1Version()
            && this.read(tr.getRead2Key()).getVersion() == tr.getRead2Version()
        ) {
            VersionedValue vv = new VersionedValue(tr.getPrepareValue(), tr.getTimestamp());
            this.write(tr.getPrepareKey(), vv);

            // If key = 0, it means that it is a configuration change
            if (tr.getPrepareKey() == 0) {
                System.out.println("[REPLICA] Config change to " + tr.getPrepareValue());
            }

            return true;
        } else {
            return false;
        }
    }
}
