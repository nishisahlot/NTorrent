package com.frostwire.jlibtorrent;

import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.DhtStatsAlert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @author gubatron
 * @author aldenml
 */
public class FetchMagnetTest {

    @Test
    public void testRemoveAfterFetch() {

        String sha1 = "a83cc13bf4a07e85b938dcf06aa707955687ca7c";
        String uri = "magnet:?xt=urn:btih:" + sha1;

        final Session s = new Session();

        final CountDownLatch signal = new CountDownLatch(1);

        // the session stats are posted about once per second.
        AlertListener l = new AlertListener() {
            @Override
            public int[] types() {
                return new int[]{AlertType.SESSION_STATS.getSwig(), AlertType.DHT_STATS.getSwig()};
            }

            @Override
            public void alert(Alert<?> alert) {
                if (alert.getType().equals(AlertType.SESSION_STATS)) {
                    s.postDHTStats();
                }

                if (alert.getType().equals(AlertType.DHT_STATS)) {

                    long nodes = ((DhtStatsAlert) alert).totalNodes();
                    // wait for at least 10 nodes in the DHT.
                    if (nodes >= 10) {
                        signal.countDown();
                    }
                }
            }

        };

        s.addListener(l);
        s.postDHTStats();

        Downloader d = new Downloader(s);

        // waiting for nodes in DHT (10 seconds)
        boolean r = false;
        try {
            r = signal.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // ignore
        }
        assertTrue("DHT bootstrap timeout", r);

        // no more trigger of DHT stats
        s.removeListener(l);


        // Fetching the magnet uri, waiting 30 seconds max
        byte[] data = d.fetchMagnet(uri, 30);
        assertNotNull("Failed to retrieve the magnet", data);

        TorrentHandle th = s.findTorrent(new Sha1Hash(sha1));
        assertNull(th);

        s.abort();
    }
}
