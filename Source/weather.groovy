//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// 项目名    ：电脑信息桌面显示
//
// 文件描述  ：天气预报解析程序，从中国气象局获得数据html格式，解析出
//             对应的天气情况
//
// 修改记录  ：
//
// 创建日期  ：2015年03月12日
//
// 项目作者  ：沈阳工业大学 老井 laojing@139.com
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

import java.text.SimpleDateFormat

updatetime = 'fc_3h_internal_update_time'
home = '/home/laojing/.conky/'
def update

//--------------------------------------------------------------------
//
// 函数功能  ：解析天气信息更新时间
//
// 函数参数  ：line : 包含更新时间的html字符串
//
// 返回值    ：直接生成包含更新时间的图片
//
// 用法示例  ：
//
// 修改记录  ：
//
//--------------------------------------------------------------------
def UpdateTime ( line ) {
	line = line[line.indexOf(updatetime)..-1]
	dt = line[(line.indexOf('value')+7)..-1]
	cmd = sprintf ( '%s %s %s %s %s %s %s %s',
			"convert -pointsize 12 -font lihei.ttf ",
			"-stroke white -fill white",
			"-annotate 0x0+10+30", 
			dt[0..9], 
			"-annotate 0x0+85+30",
			dt[11..15],
			home + "updatetimeblank.png",
			home + "updatetime.png" )
	def sdf = new SimpleDateFormat( "yyyy-MM-dd hh" );
	update = sdf.parse( dt[0..10] + dt[11..12] )
	cmd.execute()
}

//--------------------------------------------------------------------
//
// 函数功能  ：检查天气指示图片是否存在，存在返回，否则返回空图片
//
// 函数参数  ：wea : 天气名称
//
// 返回值    ：天气图片文件的名称
//
// 用法示例  ：
//
// 修改记录  ：
//
//--------------------------------------------------------------------
def GetWeatherName ( wea ) {
	blank = new File ( home + 'weather/' + wea + '.png' )
	if ( !blank.exists() ) {
		cmd = sprintf ( '%s %s %s %s %s %s',
				"convert -pointsize 30 -font " + home + "lihei.ttf ",
				"-stroke white -fill white",
				"-annotate 0x0+50+80", wea, 
				home + "weather/blank.png",
				home + "weather/error.png" )
		cmd.execute()
		wea = 'error'
	}
	return wea
}

//--------------------------------------------------------------------
//
// 函数功能  ：根据天气情况，生成对应的示意图像
//
// 函数参数  ：day   : 星期字符串
//             num   : 日期字符串
//             high  : 最高温度
//             low   : 最低温度
//             wea   : 天气状况
//             index : 第几天
//
// 返回值    ：直接生成天气图片
//
// 用法示例  ：
//
// 修改记录  ：
//
//--------------------------------------------------------------------
def GenPicture ( day, num, high, low, wea, index ) {
	if ( wea.indexOf('转')>0 ) {
		weas = wea.split ( '转' )
		one = GetWeatherName ( weas[0] )
		two = GetWeatherName ( weas[1] )
		cmd = sprintf ( '%s %s %s %s',
				"montage -background none",
				"-geometry +0+90 " + home + "weather/" + one + ".png",
				home + "weather/" + two + ".png",
				home + "weather"+index+".png" )
		cmd.execute()
	} else {
		wea = GetWeatherName ( wea )
		cmd = sprintf ( '%s %s %s',
				"montage -background none",
				"-geometry +88+90 " + home + "weather/" + wea + ".png",
				home + "weather"+index+".png" )
		cmd.execute()
	}

	temp = sprintf ( '%s°C/%s°C', high, low )
	cmd = sprintf ( '%s %s %s %s %s %s %s %s %s %s',
			"convert -pointsize 30 -font " + home + "lihei.ttf ",
			"-stroke white -fill white",
			"-annotate 0x0+110+260", temp, 
			"-annotate 0x0+135+30", day,
			"-annotate 0x0+135+80", num,
			home + "weather"+index+".png",
			home + "wea"+index+".png" )
	Process p = cmd.execute()
}

//--------------------------------------------------------------------
//
// 函数功能  ：去掉html的标记，返回实际内容，只解析第一个><内的内容
//
// 函数参数  ：html  : 带html标记字符串
//
// 返回值    ：实际内容
//
// 用法示例  ：
//
// 修改记录  ：
//
//--------------------------------------------------------------------
def GetString ( html ) {
	html = html[1..-1]
	return html[html.indexOf('>')+1..html.indexOf('<')-1]
}
def GetWeek( enweek ) {
	if( enweek == 'Mon' ) {
		return "星期一"
	} else if( enweek == 'Tue' ) {
		return "星期二"
	} else if( enweek == 'Wed' ) {
		return "星期三"
	} else if( enweek == 'Thu' ) {
		return "星期四"
	} else if( enweek == 'Fri' ) {
		return "星期五"
	} else if( enweek == 'Sat' ) {
		return "星期六"
	} else if( enweek == 'Sun' ) {
		return "星期日"
	}
	return "---"
}
def ReadHour3Data( line ) {
	def items = line.split( '\"...\"' )
	for ( i=0; i<items.size(); i++ ) {
		def words = items[i].split( '\",\"' )
		def low = 100
		def high = -100
		def wea1 = '-'
		def wea2 = '-'

		for ( j=0; j<words.size(); j++ ) {
			def chas = words[j].split( ',' )
			if( wea1 == '-' ) wea1 = chas[2]
			else if( wea1 != chas[2] ) wea2 = chas[2]
			def tmp = chas[3][0..-2].toInteger()
			if( low > tmp ) low = tmp
			if( high < tmp ) high = tmp
		}

		if( wea2 == '-' )
			GenPicture( GetWeek(update.format('E')), update.format('MM月dd'), high,low,wea1, i+1)
		else
			GenPicture( GetWeek(update.format('E')), update.format('MM月dd'), high,low,wea1+'转'+wea2, i+1)
		update = update.plus(1)
	}
}

//--------------------------------------------------------------------
//
// 函数功能  ：从html中解析天气数据，第一遍循环解析出更新时间，第二遍
//             解析近七天的天气信息
//
// 函数参数  ：
//
// 返回值    ：
//
// 用法示例  ：
//
// 修改记录  ：
//
//--------------------------------------------------------------------
def ReadHtml () {
	lines = new File ( home + 'weather.com.cn' ).readLines()
	for ( i=0; i<lines.size(); i++ ) {
		if ( lines[i].indexOf(updatetime)>=0 ) {
			UpdateTime ( lines[i] )
			break;
		}
	}
	for ( i=0; i<lines.size(); i++ ) {
		if ( lines[i].indexOf("hour3data")>=0 ) {
			line = lines[i];
			line = line[line.indexOf("7d")+7..-5]

			ReadHour3Data( line );
			break;
		}
	}

	/*
	for ( i=480; i<lines.size(); i++ ) {
		if ( lines[i].indexOf('li')>=0 && lines[i].indexOf('7d')>=0 ) {
			GenPicture ( GetString(lines[i+1]),
						 GetString(lines[i+2]),
						 GetString(lines[i+7]),
						 GetString(lines[i+10]),
						 GetString(lines[i+5]),
						 lines[i][lines[i].indexOf('7d')+2] )
		}
		if ( i>633 ) break;
	}
	*/
}

ReadHtml()
