/*
 * Copyright (C) 2014 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nikki.torrents.utils;

import java.io.File;
import java.util.HashMap;

public class Icons {
    private static HashMap<String, Integer> sMimeIcons = new HashMap<String, Integer>();
    final static int ic_doc_apk_white = 1;
    final static int ic_doc_audio_am = 2;
    final static int ic_doc_certificate = 3;
    final static int ic_doc_codes = 4;
    final static int ic_doc_compressed = 5;
    final static int ic_doc_contact_am = 6;
    final static int ic_doc_event_am = 7;
    final static int ic_doc_font = 8;
    final static int ic_doc_image = 9;
    final static int ic_doc_pdf = 10;
    final static int ic_doc_presentation = 11;
    final static int ic_doc_spreadsheet_am = 12;
    final static int ic_doc_doc_am = 13;
    final static int ic_doc_video_am = 14;
    final static int ic_doc_text_am = 15;



    private static void add(String mimeType, int resId) {
        if (sMimeIcons.put(mimeType, resId) != null) {
            throw new RuntimeException(mimeType + " already registered!");
        }
    }

    static {
        int icon;

        // Package
        icon = ic_doc_apk_white;
        add("application/vnd.android.package-archive", icon);

        // Audio
        icon = ic_doc_audio_am;
        add("application/ogg", icon);
        add("application/x-flac", icon);

        // Certificate
        icon = ic_doc_certificate;
        add("application/pgp-keys", icon);
        add("application/pgp-signature", icon);
        add("application/x-pkcs12", icon);
        add("application/x-pkcs7-certreqresp", icon);
        add("application/x-pkcs7-crl", icon);
        add("application/x-x509-ca-cert", icon);
        add("application/x-x509-user-cert", icon);
        add("application/x-pkcs7-certificates", icon);
        add("application/x-pkcs7-mime", icon);
        add("application/x-pkcs7-signature", icon);

        // Source code
        icon = ic_doc_codes;
        add("application/rdf+xml", icon);
        add("application/rss+xml", icon);
        add("application/x-object", icon);
        add("application/xhtml+xml", icon);
        add("text/css", icon);
        add("text/html", icon);
        add("text/xml", icon);
        add("text/x-c++hdr", icon);
        add("text/x-c++src", icon);
        add("text/x-chdr", icon);
        add("text/x-csrc", icon);
        add("text/x-dsrc", icon);
        add("text/x-csh", icon);
        add("text/x-haskell", icon);
        add("text/x-java", icon);
        add("text/x-literate-haskell", icon);
        add("text/x-pascal", icon);
        add("text/x-tcl", icon);
        add("text/x-tex", icon);
        add("application/x-latex", icon);
        add("application/x-texinfo", icon);
        add("application/atom+xml", icon);
        add("application/ecmascript", icon);
        add("application/json", icon);
        add("application/javascript", icon);
        add("application/xml", icon);
        add("text/javascript", icon);
        add("application/x-javascript", icon);

        // Compressed
        icon = ic_doc_compressed;
        add("application/mac-binhex40", icon);
        add("application/rar", icon);
        add("application/zip", icon);
        add("application/java-archive", icon);
        add("application/x-apple-diskimage", icon);
        add("application/x-debian-package", icon);
        add("application/x-gtar", icon);
        add("application/x-iso9660-image", icon);
        add("application/x-lha", icon);
        add("application/x-lzh", icon);
        add("application/x-lzx", icon);
        add("application/x-stuffit", icon);
        add("application/x-tar", icon);
        add("application/x-webarchive", icon);
        add("application/x-webarchive-xml", icon);
        add("application/gzip", icon);
        add("application/x-7z-compressed", icon);
        add("application/x-deb", icon);
        add("application/x-rar-compressed", icon);

        // Contact
        icon = ic_doc_contact_am;
        add("text/x-vcard", icon);
        add("text/vcard", icon);

        // Event
        icon = ic_doc_event_am;
        add("text/calendar", icon);
        add("text/x-vcalendar", icon);

        // Font
        icon = ic_doc_font;
        add("application/x-font", icon);
        add("application/font-woff", icon);
        add("application/x-font-woff", icon);
        add("application/x-font-ttf", icon);

        // Image
        icon = ic_doc_image;
        add("application/vnd.oasis.opendocument.graphics", icon);
        add("application/vnd.oasis.opendocument.graphics-template", icon);
        add("application/vnd.oasis.opendocument.image", icon);
        add("application/vnd.stardivision.draw", icon);
        add("application/vnd.sun.xml.draw", icon);
        add("application/vnd.sun.xml.draw.template", icon);
        add("image/jpeg", icon);
        add("image/png", icon);
        // PDF
        icon = ic_doc_pdf;
        add("application/pdf", icon);

        // Presentation
        icon = ic_doc_presentation;
        add("application/vnd.ms-powerpoint", icon);
        add("application/vnd.openxmlformats-officedocument.presentationml.presentation", icon);
        add("application/vnd.openxmlformats-officedocument.presentationml.template", icon);
        add("application/vnd.openxmlformats-officedocument.presentationml.slideshow", icon);
        add("application/vnd.stardivision.impress", icon);
        add("application/vnd.sun.xml.impress", icon);
        add("application/vnd.sun.xml.impress.template", icon);
        add("application/x-kpresenter", icon);
        add("application/vnd.oasis.opendocument.presentation", icon);

        // Spreadsheet
        icon = ic_doc_spreadsheet_am;
        add("application/vnd.oasis.opendocument.spreadsheet", icon);
        add("application/vnd.oasis.opendocument.spreadsheet-template", icon);
        add("application/vnd.ms-excel", icon);
        add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", icon);
        add("application/vnd.openxmlformats-officedocument.spreadsheetml.template", icon);
        add("application/vnd.stardivision.calc", icon);
        add("application/vnd.sun.xml.calc", icon);
        add("application/vnd.sun.xml.calc.template", icon);
        add("application/x-kspread", icon);

        // Doc
        icon = ic_doc_doc_am;
        add("application/msword", icon);
        add("application/vnd.openxmlformats-officedocument.wordprocessingml.document", icon);
        add("application/vnd.openxmlformats-officedocument.wordprocessingml.template", icon);
        add("application/vnd.oasis.opendocument.text", icon);
        add("application/vnd.oasis.opendocument.text-master", icon);
        add("application/vnd.oasis.opendocument.text-template", icon);
        add("application/vnd.oasis.opendocument.text-web", icon);
        add("application/vnd.stardivision.writer", icon);
        add("application/vnd.stardivision.writer-global", icon);
        add("application/vnd.sun.xml.writer", icon);
        add("application/vnd.sun.xml.writer.global", icon);
        add("application/vnd.sun.xml.writer.template", icon);
        add("application/x-abiword", icon);
        add("application/x-kword", icon);

        // Text
        icon = ic_doc_text_am;
        add("text/plain", icon);

        // Video
        icon = ic_doc_video_am;
        add("application/x-quicktimeplayer", icon);
        add("application/x-shockwave-flash", icon);
    }

    public static boolean isText(String name) {
        String mimeType = MimeTypes.getMimeType(new File(name));

        Integer res = sMimeIcons.get(mimeType);
        if (res != null && res == ic_doc_text_am) return true;
        if (mimeType != null && mimeType.contains("/")) {
            final String typeOnly = mimeType.split("/")[0];
            if ("text".equals(typeOnly)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isVideo(String name) {
        String mimeType = MimeTypes.getMimeType(new File(name));
        Integer res = sMimeIcons.get(mimeType);
        if (res != null && res == ic_doc_video_am) return true;
        if (mimeType != null && mimeType.contains("/")) {
            final String typeOnly = mimeType.split("/")[0];
            if ("video".equals(typeOnly)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAudio(String name) {
        String mimeType = MimeTypes.getMimeType(new File(name));
        Integer res = sMimeIcons.get(mimeType);
        if (res != null && res == ic_doc_audio_am) return true;
        if (mimeType != null && mimeType.contains("/")) {
            final String typeOnly = mimeType.split("/")[0];
            if ("audio".equals(typeOnly)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCode(String name) {
        Integer res = sMimeIcons.get(MimeTypes.getMimeType(new File(name)));
        if (res != null && res == ic_doc_codes) return true;
        return false;
    }

    public static boolean isArchive(String name) {
        Integer res = sMimeIcons.get(MimeTypes.getMimeType(new File(name)));
        if (res != null && res == ic_doc_compressed) return true;
        return false;
    }

    public static boolean isApk(String name) {
        Integer res = sMimeIcons.get(MimeTypes.getMimeType(new File(name)));
        if (res != null && res == ic_doc_apk_white) return true;
        return false;
    }

    public static boolean isPdf(String name) {
        Integer res = sMimeIcons.get(MimeTypes.getMimeType(new File(name)));
        if (res != null && res == ic_doc_pdf) return true;
        return false;
    }

    public static boolean isPicture(String name) {
        Integer res = sMimeIcons.get(MimeTypes.getMimeType(new File(name)));
        if (res != null && res == ic_doc_image) return true;
        return false;
    }

    public static boolean isgeneric(String name) {
        String mimeType = MimeTypes.getMimeType(new File(name));
        if (mimeType == null) {
            return true;
        }
        Integer resId = sMimeIcons.get(mimeType);
        if (resId == null) {
            return true;
        }


        return false;
    }

}
