package com.frostwire.jlibtorrent.demo;

import com.frostwire.jlibtorrent.LibTorrent;
import com.frostwire.jlibtorrent.Sha1Hash;
import com.frostwire.jlibtorrent.Vectors;
import com.frostwire.jlibtorrent.swig.*;

import java.io.File;

/**
 * @author gubatron
 * @author aldenml
 */
public final class LazyRead {

    public static void main(String[] args) throws Throwable {

        // comment this line for a real application
        args = new String[]{"/Users/aldenml/Downloads/Kellee_Maize_The_5th_Element_FrostClick_FrostWire_MP3_April_14_2014.torrent"};

        File torrentFile = new File(args[0]);

        System.out.println("Using libtorrent version: " + LibTorrent.version());

        byte[] data = Utils.readFileToByteArray(torrentFile);

        byte_vector buffer = Vectors.bytes2byte_vector(data);
        bdecode_node e = new bdecode_node();
        error_code ec = new error_code();
        int ret = bdecode_node.bdecode(buffer, e, ec);

        if (ret != 0) {
            System.out.println("failed to decode torrent: " + ec.message());
            return;
        }

        ec.clear();
        torrent_info ti = new torrent_info(e, ec);

        System.out.println(new Sha1Hash(ti.info_hash()));
        System.out.println(bdecode_node.to_string(e, false, 0));
    }
}
