templateFill="TEMPLATE_MIN_DELAY $1\nTEMPLATE_MAX_DELAY $2\nTEMPLATE_MAX_MSG_COUNT $3\n"
gnuplotScript="
set title 'Min Delay: $1, Max delay: $2, Max messages/10 cycles: $3'\n
set xlabel 'Clients'\n
set ylabel 'Tx'\n
set xrange [80:280]\n
set yrange [1000:6000]\n
set xtics 20;
plot 'tests/$1_$2_$3.dat' with linespoints linestyle 1\n
pause -1 'Hit any key to continue'\n"

echo $gnuplotScript > temp.gnuscript
cat /dev/null > tests/$1_$2_$3.dat
for i in `seq 88 20 288`;
do
	echo "Testing with `expr $i - 8` of 280 clients"
	templateFillSize="TEMPLATE_SIZE $i\n"
	echo "$templateFill$templateFillSize" | cat - configs/config-2pc-template.txt > temp.config
	java -classpath ./out/production/Sim:./jep-2.3.0.jar:./djep-1.0.0.jar peersim.Simulator temp.config > temp.out 2>/dev/null
	echo "`expr $i - 8` $(tail -1 temp.out)" >> tests/$1_$2_$3.dat
	rm temp.config
	rm temp.out
done

gnuplot temp.gnuscript
rm temp.gnuscript