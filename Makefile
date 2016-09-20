build : Source/weather.groovy
	cp Source/weather.groovy /home/laojing/.conky/weather.groovy
	cp Resource/$(CPUNAME) /home/laojing/.conkyrc
	cp Resource/*.png /home/laojing/.conky
	mkdir -p /home/laojing/.conky/weather
	cp Resource/weather/*.png /home/laojing/.conky/weather

run : build
	groovy /home/laojing/.conky/weather.groovy
