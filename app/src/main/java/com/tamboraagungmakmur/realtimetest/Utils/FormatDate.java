package com.tamboraagungmakmur.realtimetest.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Tambora on 30/11/2016.
 */
public class FormatDate {

    public static String format(String waktu, String formatAwal, String formatAkhir) {
        SimpleDateFormat fromdate = new SimpleDateFormat(formatAwal);
        SimpleDateFormat todate = new SimpleDateFormat(formatAkhir);
        String reformatdate = null;
        try {
            reformatdate = todate.format(fromdate.parse(waktu));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return reformatdate;
    }

    public static String currentDateTime() {
        String[] days = new String[]{"Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"};
        String[] month = new String[]{"Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        Calendar calendar = Calendar.getInstance();
        return days[calendar.get(Calendar.DAY_OF_WEEK) - 1] + ", " + (calendar.get(Calendar.DAY_OF_MONTH) + " " + month[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR)) + "\n" + new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    public static String currentTime() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

}
