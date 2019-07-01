

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**

 */
public class DateUtils {

	private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

	public static String ymdhms = "yyyy-MM-dd HH:mm:ss";

	private static String ymdh = "yyyy-MM-dd HH";

	private static String ymdhs = "yyyy-MM-dd HH:mm";

	private static String ymdhms2 = "yyyyMMddHHmmss";

	private static String ymd = "yyyy-MM-dd";

	private static String ym = "yyyy-MM";

	public static SimpleDateFormat ymdSDF = new SimpleDateFormat(ymd);

	private static String year = "yyyy";

	private static String month = "MM";

	private static String day = "dd";

	private static String hour = "HH";

	private static String minute = "mm";

	private static String second = "ss";

	public static SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat(ymdhms);

	public static SimpleDateFormat yyyyMMddHHmm = new SimpleDateFormat(ymdhs);

	public static SimpleDateFormat yyyyMMddHH = new SimpleDateFormat(ymdh);
	public static SimpleDateFormat yyyyMM = new SimpleDateFormat(ym);

	public static SimpleDateFormat yyyyMMddHHmmss2 = new SimpleDateFormat(
			ymdhms2);

	public static SimpleDateFormat yearSDF = new SimpleDateFormat(year);

	public static SimpleDateFormat monthSDF = new SimpleDateFormat(month);

	public static SimpleDateFormat daySDF = new SimpleDateFormat(day);

	public static SimpleDateFormat hourSDF = new SimpleDateFormat(hour);

	public static SimpleDateFormat minuteSDF = new SimpleDateFormat(minute);

	public static SimpleDateFormat secondSDF = new SimpleDateFormat(second);

	public static SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");

	public static SimpleDateFormat yyyyMMddHH_NOT_ = new SimpleDateFormat(
			"yyyyMMdd");

	public static long DATEMM = 86400L;

	/**
	 * 获得当前时间 格式：20151214171555
	 *
	 * @return String
	 */
	public static String getCurrentTime2() {
		return yyyyMMddHHmmss2.format(new Date());
	}

	/**
	 * 获得当前时间 格式：2014-12-02 10:38:53
	 *
	 * @return String
	 */
	public static String getCurrentTime() {
		//多线程会有问题，修改
//		return yyyyMMddHHmmss.format(new Date());
		SimpleDateFormat ymdhmsFormat = new SimpleDateFormat(ymdhms);
		return ymdhmsFormat.format(new Date());
	}

	public static Timestamp getCurrentTimestamp(){
		return turnString2TimeStamp(getCurrentTime());
	}

	/**
	 * 可以获取昨天的日期 格式：2014-12-01
	 *
	 * @return String
	 */
	public static String getYesterdayYYYYMMDD() {
		Date date = new Date(System.currentTimeMillis() - DATEMM * 1000L);
		String str = yyyyMMdd.format(date);
		try {
			date = yyyyMMddHHmmss.parse(str + " 00:00:00");
			return yyyyMMdd.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 可以获取后退N天的日期 格式：传入2 得到2014-11-30
	 *
	 * @param backDay
	 * @return String
	 */
	public String getStrDate(String backDay) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, Integer.parseInt("-" + backDay));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String back = sdf.format(calendar.getTime());
		return back;
	}

	/**
	 * 可以获取后退N天的日期 格式：传入2 得到 前两天 日期格式：2014-11-30 00:00:00
	 */
	public static String getStrDateByN1(String backDay) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, Integer.parseInt("-" + backDay));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String back = sdf.format(calendar.getTime());
		return back;
	}

	/**
	 * 可以获取后退N天的日期 格式：传入2 得到2014-11-30
	 *
	 * @param backDay
	 * @return String
	 */
	public static String getStrDateByN(String backDay) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, Integer.parseInt("-" + backDay));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String back = sdf.format(calendar.getTime());
		return back;
	}

	/**
	 * 可以获取后退N小时的日期
	 *
	 * @param backHour
	 * @return String
	 */
	public static String getLastNHour(int backHour) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, Integer.parseInt("-" + backHour));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH");
		String back = sdf.format(calendar.getTime());
		return back;
	}

	/**
	 * 获取当前的年、月、日
	 *
	 * @return String
	 */
	public static String getCurrentYear() {
		return yearSDF.format(new Date());
	}

	public static String getCurrentMonth() {
		return monthSDF.format(new Date());
	}

	public static String getCurrentDay() {
		return daySDF.format(new Date());
	}

	/**
	 * 获取当前的时、分、秒
	 *
	 * @return
	 */
	public static String getCurrentHour() {
		return hourSDF.format(new Date());
	}

	public static String getCurrentMinute() {
		return minuteSDF.format(new Date());
	}

	public static String getCurrentSecond() {
		return secondSDF.format(new Date());
	}

	/**
	 * 获取年月日 也就是当前时间 格式：2014-12-02
	 *
	 * @return String
	 */
	public static String getCurrentymd() {
		return ymdSDF.format(new Date());
	}

	public static String getyyyyMM(String time){
		try {
			return yyyyMM.format(yyyyMMddHHmmss.parse(time));
		} catch (ParseException e) {
//			e.printStackTrace();
			return null;
		}
	}

	public static String getyyyyMMdd(String time){
		try {
			return yyyyMMdd.format(yyyyMMddHHmmss.parse(time));
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 获取今天0点开始的秒数
	 *
	 * @return long
	 */
	public static long getTimeNumberToday() {
		Date date = new Date();
		String str = yyyyMMdd.format(date);
		try {
			date = yyyyMMdd.parse(str);
			return date.getTime() / 1000L;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0L;
	}

	/**
	 * 获取今天的日期 格式：20141202
	 *
	 * @return String
	 */
	public static String getTodayString() {
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		String str = format.format(date);
		return str;
	}

	/**
	 * 获取昨天的日期 格式：20141201
	 *
	 * @return String
	 */
	public static String getYesterdayString() {
		Date date = new Date(System.currentTimeMillis() - DATEMM * 1000L);
		String str = yyyyMMddHH_NOT_.format(date);
		return str;
	}

	/**
	 * 获得昨天零点
	 *
	 * @return Date
	 */
	public static Date getYesterDayZeroHour() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR, 0);
		return cal.getTime();
	}

	/**
	 * 把long型日期转String ；---OK
	 *
	 * @param date 毫秒数；
	 * @param format
	 *            日期格式；
	 * @return
	 */
	public static String longToString(Long date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date dt2 = new Date(date);
		String sDateTime = sdf.format(dt2); // 得到精确到秒的表示：08/31/2006 21:08:00
		return sDateTime;
	}

	/**
	 * 获得今天零点
	 *
	 * @return Date
	 */
	public static Date getTodayZeroHour() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR, 0);
		return cal.getTime();
	}

	/**
	 * 获得昨天23时59分59秒
	 *
	 * @return
	 */
	public static Date getYesterDay24Hour() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.HOUR, 23);
		return cal.getTime();
	}

	/**
	 * String To Date ---OK
	 *
	 * @param date
	 *            待转换的字符串型日期；
	 * @param format
	 *            转化的日期格式
	 * @return 返回该字符串的日期型数据；
	 */
	public static Date stringToDate(String date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			return sdf.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * 获得指定日期所在的自然周的第一天，即周日
	 *
	 * @param date
	 *            日期
	 * @return 自然周的第一天
	 */
	public static Date getStartDayOfWeek(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_WEEK, 1);
		date = c.getTime();
		return date;
	}

	/**
	 * 获得指定日期所在的自然周的最后一天，即周六
	 *
	 * @param date
	 * @return
	 */
	public static Date getLastDayOfWeek(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_WEEK, 7);
		date = c.getTime();
		return date;
	}

	/**
	 * 获得指定日期所在当月第一天
	 *
	 * @param date
	 * @return
	 */
	public static Date getStartDayOfMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_MONTH, 1);
		date = c.getTime();
		return date;
	}

	/**
	 * 获得指定日期所在当月最后一天
	 *
	 * @param date
	 * @return
	 */
	public static Date getLastDayOfMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DATE, 1);
		c.add(Calendar.MONTH, 1);
		c.add(Calendar.DATE, -1);
		date = c.getTime();
		return date;
	}

	/**
	 * 获得指定日期的下一个月的第一天
	 *
	 * @param date
	 * @return
	 */
	public static Date getStartDayOfNextMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, 1);
		c.set(Calendar.DAY_OF_MONTH, 1);
		date = c.getTime();
		return date;
	}

	/**
	 * 获得指定日期的下一个月的最后一天
	 *
	 * @param date
	 * @return
	 */
	public static Date getLastDayOfNextMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DATE, 1);
		c.add(Calendar.MONTH, 2);
		c.add(Calendar.DATE, -1);
		date = c.getTime();
		return date;
	}

	/**
	 *
	 * 求某一个时间向前多少秒的时间(currentTimeToBefer)---OK
	 *
	 * @param givedTime
	 *            给定的时间
	 * @param interval
	 *            间隔时间的毫秒数；计算方式 ：n(天)*24(小时)*60(分钟)*60(秒)(类型)
	 * @param format_Date_Sign
	 *            输出日期的格式；如yyyy-MM-dd、yyyyMMdd等；
	 */
	public static String givedTimeToBefer(String givedTime, long interval,
			String format_Date_Sign) {
		String tomorrow = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format_Date_Sign);
			Date gDate = sdf.parse(givedTime);
			long current = gDate.getTime(); // 将Calendar表示的时间转换成毫秒
			long beforeOrAfter = current - interval * 1000L; // 将Calendar表示的时间转换成毫秒
			Date date = new Date(beforeOrAfter); // 用timeTwo作参数构造date2
			tomorrow = new SimpleDateFormat(format_Date_Sign).format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return tomorrow;
	}

	/**
	 * 把String 日期转换成long型日期(毫秒)；---OK
	 *
	 * @param date
	 *            String 型日期；
	 * @param format
	 *            日期格式；
	 * @return
	 */
	public static long stringToLong(String date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date dt2 = null;
		long lTime = 0;
		try {
			dt2 = sdf.parse(date);
			// 继续转换得到秒数的long型
			lTime = dt2.getTime() ;
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return lTime;
	}

	/**
	 * 得到二个日期间的间隔日期；
	 *
	 * @param endTime
	 *            结束时间
	 * @param beginTime
	 *            开始时间
	 * @param isEndTime
	 *            是否包含结束日期；
	 * @return
	 */
	public static Map<String, String> getTwoDay(String endTime,
			String beginTime, boolean isEndTime) {
		Map<String, String> result = new HashMap<String, String>();
		if ((endTime == null || endTime.equals("") || (beginTime == null || beginTime
				.equals(""))))
			return null;
		try {
			Date date = ymdSDF.parse(endTime);
			endTime = ymdSDF.format(date);
			Date mydate = ymdSDF.parse(beginTime);
			long day = (date.getTime() - mydate.getTime())
					/ (24 * 60 * 60 * 1000);
			result = getDate(endTime, Integer.parseInt(day + ""), isEndTime);
		} catch (Exception e) {
		}
		return result;
	}

	/**
	 * 得到二个日期间的间隔日期（单位是天）；
	 *
	 * @param endTime
	 *            结束时间  支持多种格式
	 * @param beginTime
	 *            开始时间  支持多种格式
	 * @return
	 */
	public static Integer getTwoDayInterval(String endTime, String beginTime) {
		if ((endTime == null || endTime.equals("") || (beginTime == null || beginTime
				.equals(""))))
			return 0;
		long day = 0l;

		try {
	        long beginLong = DateUtils.turnStrTime2Date(beginTime);
	        long endLong = DateUtils.turnStrTime2Date(endTime);

			try {
				day = (endLong - beginLong) / (24 * 60 * 60 * 1000);
			} catch (Exception e) {
				return 0;
			}
			return Integer.parseInt(day + "");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    	return 0;
	}

	/**
	 * 得到二个日期间的间隔日期的秒数(单位是秒)；
	 *
	 * @param endTime
	 *            结束时间
	 * @param beginTime
	 *            开始时间
	 * @param isEndTime
	 *            是否包含结束日期；
	 * @return
	 */
	public static Integer getTwoDayIntervalS(String endTime, String beginTime,
			boolean isEndTime) {
		if ((endTime == null || endTime.equals("") || (beginTime == null || beginTime
				.equals(""))))
			return 0;
		long s = 0l;
		try {
			Date date = yyyyMMddHHmmss.parse(endTime);
			endTime = yyyyMMddHHmmss.format(date);
			Date mydate = yyyyMMddHHmmss.parse(beginTime);
			s = (date.getTime() - mydate.getTime())/1000;
		} catch (Exception e) {
			return 0;
		}
		return Integer.parseInt(s + "");
	}
	/**
	 * 根据结束时间以及间隔差值，求符合要求的日期集合；
	 *
	 * @param endTime
	 * @param interval
	 * @param isEndTime
	 * @return
	 */
	public static Map<String, String> getDate(String endTime, Integer interval,
			boolean isEndTime) {
		Map<String, String> result = new HashMap<String, String>();
		if (interval == 0 || isEndTime) {
			if (isEndTime)
				result.put(endTime, endTime);
		}
		if (interval > 0) {
			int begin = 0;
			for (int i = begin; i < interval; i++) {
				endTime = givedTimeToBefer(endTime, DATEMM, ymd);
				result.put(endTime, endTime);
			}
		}
		return result;
	}


	/**
	 * 判断一个时间是否在另一个时间之前
	 * @author bianfulin
	 * @param time1 第一个时间
	 * @param time2 第二个时间
	 * @return 判断结果
	 */
	public static boolean before(String time1, String time2) {
		try {
			Date dateTime1 = yyyyMMddHHmmss.parse(time1);
			Date dateTime2 = yyyyMMddHHmmss.parse(time2);

			if(dateTime1.before(dateTime2)) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 判断一个时间是否在另一个时间之后
	 * @param time1 第一个时间
	 * @param time2 第二个时间
	 * @return 判断结果
	 */
	public static boolean after(String time1, String time2) {
		try {
			Date dateTime1 = yyyyMMddHHmmss.parse(time1);
			Date dateTime2 = yyyyMMddHHmmss.parse(time2);

			dateTime1.setHours(0);
			dateTime1.setMinutes(0);
			dateTime1.setSeconds(0);
			dateTime2.setHours(0);
			dateTime2.setMinutes(0);
			dateTime2.setSeconds(0);

			if(dateTime1.after(dateTime2)) {
				return true;
			}
		} catch (Exception e) {
			logger.error("DateUtils after method error");
//			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 计算时间差值（单位为秒）
	 * @param time1 时间1
	 * @param time2 时间2
	 * @return 差值
	 */
	public static int minus(String time1, String time2) {
		try {
			Date datetime1 = yyyyMMddHHmmss.parse(time1);
			Date datetime2 = yyyyMMddHHmmss.parse(time2);

			long millisecond = datetime1.getTime() - datetime2.getTime();

			return Integer.valueOf(String.valueOf(millisecond / 1000));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 可以获取后退N天的日期 格式：传入2 得到 前两天 日期格式：2014-11-30 00:00:00
	 */
	/**
	 * 可获得前进或后退N天的日期时间，日期格式:yyyy-MM-dd HH:mm:ss
	 * @param timestamp 日期时间
	 * @param backDay n天，正为前进，负为后退
	 * @return
	 */
	public static String getStrDateByN1(String timestamp ,Integer backDay) {
		try {
			Date date = yyyyMMddHHmmss.parse(timestamp);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.DATE,  backDay);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String back = sdf.format(calendar.getTime());
			return back;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
     * 返回指定时间段的特定日期List.比如所有周一(周二、周三....)
     *
     * @param fromYmd 起始日期
     * @param toYmd 中止日期
     * @param type 日期类型:1 周日 2 周一 3 周二  4 周三 5 周四 6 周五 7 周六
     * @param flag 是否包括起始日前面的最近一个指定特殊日期 0:不包括 1:包括
     *
     * @return 日期集合
     */
    public static List<String> getSpecificDayList(String fromYmd, String toYmd,
        int type, int flag) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        List<String> resultList = new ArrayList<String>();
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Integer.parseInt(fromYmd.substring(0, 4)), Integer.parseInt(fromYmd.substring(4, 6)) - 1, Integer.parseInt(fromYmd.substring(6)));

        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Integer.parseInt(toYmd.substring(0, 4)), Integer.parseInt(toYmd.substring(4, 6)) - 1, Integer.parseInt(toYmd.substring(6)));

        if ((flag == 1) && (calendar1.get(Calendar.DAY_OF_WEEK) != type)) {
            while (calendar1.get(Calendar.DAY_OF_WEEK) != type) {
                calendar1.add(Calendar.DATE, -1);
            }

            resultList.add(sdf.format(calendar1.getTime()));
            calendar1.add(Calendar.DATE, 1);
        }

        while (!calendar1.after(calendar2)) {
            if (calendar1.get(Calendar.DAY_OF_WEEK) == type) {
                resultList.add(sdf.format(calendar1.getTime()));
            }

            calendar1.add(Calendar.DATE, 1);
        }

        return resultList;
    }

    /**
     * 判断是否是节假日
     * @param date，格式：2018-4-20 22:22:22
     * @return
     */
    public static boolean isHoliday(String date){
//        SimpleDateFormat sdw = new SimpleDateFormat("E"); 
    	if(date==null)return false;
        Date d = null;
        try {
            d = yyyyMMddHHmmss.parse(date);
        } catch (ParseException e) {
           return false;
        }
//        String weedDay = sdw.format(d);
//        logger.error("weedDay="+weedDay);
//        logger.error("星期六");
//        if("星期六".equals(weedDay)||"星期日".equals(weedDay)){
//        	return true;
//        }
//        return false;  
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(d);
		if(cal.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY||cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY){
    	    return true;
    	}
		return false;
    }

    /**
     * 获取小时数
     * @param time ，格式：2018-40-22 22:22:22
     * @return
     */
    public static Integer getHour(String time){
    	try {
			int hour = yyyyMMddHHmmss.parse(time).getHours();
			return hour;
		} catch (ParseException e) {
			logger.error(e.getMessage());
		}
    	return null;
    }

    /**
     * 获取小时数
     * @param time ，格式：2018-40-22 22:22:22
     * @return
     */
    public static int getMonth(String time){
		int hour = new Date(turnTime2Long(time)).getMonth();
		// int hour = yyyyMMddHHmmss.parse(time).getMonth();
		return hour;
    }



    /**
     * 获取当前月份前n个月的list
     * @param nMonth
     * @param pattern 展示格式,例如yyyy-MM
     * @return
     */
    public static List<String> getMonthListBeforeCurrMonth(Integer nMonth ,String pattern){
    	List<String> dateList = new ArrayList<>();
    	Calendar calendar = Calendar.getInstance();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
    	for(int i=1 ; i<=nMonth ; i++){
    		calendar.add(Calendar.MONTH, -1);
    		dateList.add(sdf.format(calendar.getTime()));
    	}
		return dateList;
    }

    /**
     * 判断time是否在距今n个月内
     * @param time 时间，格式：yyyy-MM-dd HH:mm:ss或yyyy-MM-dd
     * @param nMonth 几个月内
     * @return
     */
    public static boolean withinMonth(String time , Integer nMonth){
    	if(StringUtil.isBlankStr(time))return false;
    	Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -nMonth);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long nMonthBefore = calendar.getTimeInMillis();
        SimpleDateFormat sdf = null;
    	if(time.length()==19&&time.contains(":")){
    		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		try {
				Date date = sdf.parse(time);
				calendar.setTime(date);
		        calendar.set(Calendar.HOUR_OF_DAY, 0);
		        calendar.set(Calendar.MINUTE, 0);
		        calendar.set(Calendar.SECOND, 0);
		        long timeLong = calendar.getTimeInMillis();
		        if(timeLong > nMonthBefore){
		        	return true;
		        }
			} catch (ParseException e) {
				e.printStackTrace();
			}
    	}else if(time.length()==7&&time.contains("-")&&!time.contains(":")){
    		sdf = new SimpleDateFormat("yyyy-MM");
    		try {
				Date date = sdf.parse(time);
				calendar.setTime(date);
				calendar.set(Calendar.DATE,01);
		        calendar.set(Calendar.HOUR_OF_DAY, 0);
		        calendar.set(Calendar.MINUTE, 0);
		        calendar.set(Calendar.SECOND, 0);
		        long timeLong = calendar.getTimeInMillis();
		        if(timeLong > nMonthBefore){
		        	return true;
		        }
			} catch (ParseException e) {
				e.printStackTrace();
			}
    	} else if(RegUtil.isMatch(time, "\\d{13,}")){
    		//判断是不是时间戳，例如："1478329992827"
    		try {
				Long timeLong = new Long(time);

				calendar.setTimeInMillis(timeLong);
				calendar.set(Calendar.HOUR_OF_DAY, 0);
		        calendar.set(Calendar.MINUTE, 0);
		        calendar.set(Calendar.SECOND, 0);
		        timeLong = calendar.getTimeInMillis();

				if(timeLong > nMonthBefore){
					return true;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
    	} else {
    		try {
    			sdf = new SimpleDateFormat("yyyy-MM-dd");
    			time = time.replace("年", "-").replace("月", "-");
				Date date = sdf.parse(time);
				calendar.setTime(date);
		        calendar.set(Calendar.HOUR_OF_DAY, 0);
		        calendar.set(Calendar.MINUTE, 0);
		        calendar.set(Calendar.SECOND, 0);
		        long timeLong = calendar.getTimeInMillis();
		        if(timeLong > nMonthBefore){
		        	return true;
		        }
			} catch (ParseException e) {
				logger.warn("time pattern is wrong ===>" +time);
			}
    	}
    	return false;
    }

	/**
	 * 输入时间是否在比较时间n个月内
	 * @param inputTime
	 * @param compareTime
	 * @param nMonth
	 * @return
	 */
	public static boolean withinMonthByTime(String inputTime, String compareTime, Integer nMonth){
		if(StringUtil.isBlankStr(inputTime) || StringUtil.isBlankStr(compareTime))return false;
        Long time1 = timeToMillis(inputTime, 0);
        Long time2 = timeToMillis(compareTime, nMonth);
        if(time1 ==null || time2 ==null)return false;
		return time1 > time2;
	}

	/**
	 * 时间转毫秒
	 * @param time
	 * @return
	 */
	private static Long timeToMillis(String time,Integer nMonth){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		if(time.length()==19&&time.contains(":")){
			try {
				Date date = yyyyMMddHHmmss.parse(time);
				calendar.setTime(date);
				if(nMonth != null) calendar.add(Calendar.MONTH, -nMonth);
				setCalendar(calendar,0,0,0);
				return calendar.getTimeInMillis();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}else if(time.length()==7&&time.contains("-")&&!time.contains(":")){
			try {
				Date date = yyyyMM.parse(time);
				calendar.setTime(date);
				if(nMonth != null) calendar.add(Calendar.MONTH, -nMonth);
				calendar.set(Calendar.DATE,01);
				setCalendar(calendar,0,0,0);
				return calendar.getTimeInMillis();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else if(RegUtil.isMatch(time, "\\d{13,}")){
			//判断是不是时间戳，例如："1478329992827"
			try {
				Long timeLong = new Long(time);
				calendar.setTimeInMillis(timeLong);
				if(nMonth != null) calendar.add(Calendar.MONTH, -nMonth);
				setCalendar(calendar,0,0,0);
				return calendar.getTimeInMillis();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		} else {
			try {
				time = time.replace("年", "-").replace("月", "-");
				Date date = yyyyMMdd.parse(time);
				calendar.setTime(date);
				if(nMonth != null) calendar.add(Calendar.MONTH, -nMonth);
				setCalendar(calendar,0,0,0);
				return calendar.getTimeInMillis();
			} catch (ParseException e) {
				logger.warn("time pattern is wrong ===>" +time);
			}
		}
		return null;
	}

	/**
	 * 设置calendar
	 * @param calendar
	 * @param hour
	 * @param minute
	 * @param second
	 */
	private static void setCalendar(Calendar calendar, Integer hour, Integer minute, Integer second){
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
	};

    /**
     * 将各种形式的字符串时间转为Long（时分秒都转为零）
     * @param time
     * @return
     */
    public static Long turnStrTime2Date(String time){
    	Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = null;
        Long timeLong  = 0L;
    	if(time.length()==19&&time.contains(":")){
    		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		try {
				Date date = sdf.parse(time);
				calendar.setTime(date);
		        calendar.set(Calendar.HOUR_OF_DAY, 0);
		        calendar.set(Calendar.MINUTE, 0);
		        calendar.set(Calendar.SECOND, 0);
		       timeLong = calendar.getTimeInMillis();
			} catch (ParseException e) {
				e.printStackTrace();
			}
    	}else if(time.length()==7&&time.contains("-")&&!time.contains(":")){
    		sdf = new SimpleDateFormat("yyyy-MM");
    		try {
				Date date = sdf.parse(time);
				calendar.setTime(date);
				calendar.set(Calendar.DATE,01);
		        calendar.set(Calendar.HOUR_OF_DAY, 0);
		        calendar.set(Calendar.MINUTE, 0);
		        calendar.set(Calendar.SECOND, 0);
		        timeLong = calendar.getTimeInMillis();
			} catch (ParseException e) {
				e.printStackTrace();
			}
    	} else if(RegUtil.isMatch(time, "\\d{13,}")){
    		//判断是不是时间戳，例如："1478329992827"
    		try {
				timeLong = new Long(time);
				calendar.setTimeInMillis(timeLong);
				calendar.set(Calendar.HOUR_OF_DAY, 0);
		        calendar.set(Calendar.MINUTE, 0);
		        calendar.set(Calendar.SECOND, 0);
		        timeLong = calendar.getTimeInMillis();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
    	} else {
    		try {
    			sdf = new SimpleDateFormat("yyyy-MM-dd");
    			time = time.replace("年", "-").replace("月", "-");
				Date date = sdf.parse(time);
				calendar.setTime(date);
		        calendar.set(Calendar.HOUR_OF_DAY, 0);
		        calendar.set(Calendar.MINUTE, 0);
		        calendar.set(Calendar.SECOND, 0);
		        timeLong = calendar.getTimeInMillis();
			} catch (ParseException e) {
				logger.warn("time pattern is wrong ===>" +time);
			}
    	}
    	return timeLong;
    }

    /**
     * 将time转为时间戳格式
     * @param time 时间，格式：yyyy-MM-dd HH:mm:ss或yyyy-MM-dd或时间戳
     * @return
     */
    public static Long turnTime2Long(String time){
    	Calendar calendar = Calendar.getInstance();
    	SimpleDateFormat sdf = null;
    	 Long timeLong = null;
    	if(time.length()==19&&time.contains(":")){
    		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		try {
				Date date = sdf.parse(time);
				calendar.setTime(date);
		        timeLong = calendar.getTimeInMillis();
			} catch (ParseException e) {
				e.printStackTrace();
			}
    	}else if(time.length()==7&&time.contains("-")&&!time.contains(":")){
    		sdf = new SimpleDateFormat("yyyy-MM");
    		try {
				Date date = sdf.parse(time);
				calendar.setTime(date);
				calendar.set(Calendar.DATE,01);
		        calendar.set(Calendar.HOUR_OF_DAY, 0);
		        calendar.set(Calendar.MINUTE, 0);
		        calendar.set(Calendar.SECOND, 0);
		        timeLong = calendar.getTimeInMillis();
			} catch (ParseException e) {
				e.printStackTrace();
			}
    	} else if(RegUtil.isMatch(time, "\\d{13,}")){
    		//判断是不是时间戳，例如："1478329992827"
    		try {
				timeLong = new Long(time);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
    	} else {
    		try {
    			sdf = new SimpleDateFormat("yyyy-MM-dd");
    			time = time.replace("年", "-").replace("月", "-");
				Date date = sdf.parse(time);
				calendar.setTime(date);
		        calendar.set(Calendar.HOUR_OF_DAY, 0);
		        calendar.set(Calendar.MINUTE, 0);
		        calendar.set(Calendar.SECOND, 0);
		        timeLong = calendar.getTimeInMillis();
			} catch (ParseException e) {
				logger.warn("time pattern is wrong ===>" +time);
			}
    	}
    	return timeLong;
    }
    /**
     * 返回在当前时间n天之前的日期
     *  i为正数 向后推迟i天，负数时向前提前i天
     * @param i
     * @return
     */
    public Date getdate(int i)
    {
    Date dat = null;
    Calendar cd = Calendar.getInstance();
    cd.add(Calendar.DATE, i);
    dat = cd.getTime();
    SimpleDateFormat dformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Timestamp date = Timestamp.valueOf(dformat.format(dat));
    return date;
    }

    /**
     * 日期字符串根据老格式转为新格式
     * @param dateStr
     * @param oldPattern
     * @param newPattern
     * @return
     */
    public static String changeDateFormat(String dateStr , String oldPattern ,String newPattern){
    	if(dateStr==null)return null;
    	SimpleDateFormat sdf = new SimpleDateFormat(oldPattern);
    	try {
			Date date = sdf.parse(dateStr);
			sdf = new SimpleDateFormat(newPattern);
			return sdf.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	return null;
    }

    /**
     *  将时间戳转换为时间
     * @param s
     * @return
     */
    public static String stampToDate(String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ymdhms);
        long lt = new Long(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }

	/**
	 *  将时间戳转换为1970年到现在的天数
	 * @param stamp
	 * @return
	 */
	public static Long stampToDay(Object stamp){
		if(stamp == null) return null;
		Long timeLong = NumberUtil.toLong(String.valueOf(stamp));
		Long day = timeLong / (1000*60*60*24);
		return day;
	}

	/**
	 * 获取从开始时间到现在的秒数
	 * @param startTime
	 * @return
	 */
	public  static Double getTimeInterval(Long startTime){
		return MathUtil.div((System.currentTimeMillis() - startTime),1000d);
	}

    /**
     *  将时间戳转换为时间
     * @param lt
     * @return
     */
    public static String stampToDate(Long lt){
    	if(lt==null || lt == 0 || lt == 1) return null;
        String res = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ymdhms);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }

    public static Timestamp turnString2TimeStamp(String time){
    	if(StringUtils.isBlank(time)) return null;
    	return new Timestamp(turnTime2Long(time));
	}

	/**
	 * 判断time是否在距今n天内
	 * @param time 时间，格式：yyyy-MM-dd HH:mm:ss或yyyy-MM-dd
	 * @param nDay 几个月内
	 * @param timePoint 从哪个时间段往前推
	 * @return
	 */
	public static boolean withinDay(String time , Integer nDay,Date timePoint){
		if(StringUtil.isBlankStr(time))return false;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(timePoint);
//		calendar.set(Calendar.DATE, calendar.get(Calendar.DATE)-nDay-1);
		calendar.set(Calendar.DATE, calendar.get(Calendar.DATE)-nDay);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		long nMonthBefore = calendar.getTimeInMillis();
		SimpleDateFormat sdf = null;
		if(time.length()==19&&time.contains(":")){
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				Date date = sdf.parse(time);
				calendar.setTime(date);
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				long timeLong = calendar.getTimeInMillis();
				if(timeLong > nMonthBefore){
					return true;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}else if(time.length()==7&&time.contains("-")&&!time.contains(":")){
			sdf = new SimpleDateFormat("yyyy-MM");
			try {
				Date date = sdf.parse(time);
				calendar.setTime(date);
				calendar.set(Calendar.DATE,01);
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				long timeLong = calendar.getTimeInMillis();
				if(timeLong > nMonthBefore){
					return true;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else if(RegUtil.isMatch(time, "\\d{13,}")){
			//判断是不是时间戳，例如："1478329992827"
			try {
				Long timeLong = new Long(time);

				calendar.setTimeInMillis(timeLong);
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				timeLong = calendar.getTimeInMillis();

				if(timeLong > nMonthBefore){
					return true;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		} else {
			try {
				sdf = new SimpleDateFormat("yyyy-MM-dd");
				time = time.replace("年", "-").replace("月", "-");
				Date date = sdf.parse(time);
				calendar.setTime(date);
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				long timeLong = calendar.getTimeInMillis();
				if(timeLong > nMonthBefore){
					return true;
				}
			} catch (ParseException e) {
				logger.warn("time pattern is wrong ===>" +time);
			}
		}
		return false;
	}

	public static void main(String[] args) {
//		List<String> monthListBeforeCurrMonth = DateUtils.getMonthListBeforeCurrMonth(3, "yyyy-MM");
//		System.out.println(monthListBeforeCurrMonth);
//		System.out.println("2018-02-07 19:18:09".length());
//		System.out.println("yyyy-MM-dd HH:mm:ss".length());
//		System.out.println("yyyy-MM-dd".length());

		String time =  "2018-08-1 04:22:11";
		String time2 = "2018-09-13 04:22:11";
//		String time =  "2018-02-17";
//		System.out.println(DateUtils.isHoliday(time));
//		System.out.println(DateUtils.withinMonth(time, 1));
//		System.out.println(DateUtils.turnTime2Long(time));
//		System.out.println(DateUtils.longToString(DateUtils.turnTime2Long(time), DateUtils.ymdhms));
//		System.out.println(DateUtils.getTwoDayInterval(time, time2));
//		System.out.println(DateUtils.getMonth("2018-05-18"));

//		System.out.println(withinMonthByTime(time,time2,1));
		System.out.println(new Timestamp(turnTime2Long("2018-01-01 10:10:10")));
	}
}
