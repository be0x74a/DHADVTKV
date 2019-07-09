#!/bin/bash

for i in {0..300..10}
    do
        if [ "$i" -eq "0" ]; then
            i=1
        fi
        echo $i/300

        sed -e 's/TEMPLATE_SIZE/'$(($i+8))'/' < saturation_files/configs/config-2pc.template > saturation_files/configs/config.config
        java -classpath ./out/production/Sim:./jep-2.3.0.jar:./djep-1.0.0.jar peersim.Simulator saturation_files/configs/config.config >> saturation_files/results/ee0a251b934a78f78279f4d943e31f670e571fb7/tpc_data.csv 2>/dev/null
        echo ', '$(($i)) >> saturation_files/results/ee0a251b934a78f78279f4d943e31f670e571fb7/tpc_data.csv
        rm saturation_files/configs/config.config
        for j in {0..3..1}
            do
                if [ "$j" -eq "3" ]; then
                    j=30
                fi
                if [ "$j" -eq "2" ]; then
                    j=15
                fi
                if [ "$j" -eq "1" ]; then
                    j=5
                fi
                if [ "$j" -eq "0" ]; then
                    j=1
                fi

                sed -e 's/TEMPLATE_SIZE/'$(($i+9))'/' -e 's/BATCH_SIZE/'$j'/' < saturation_files/configs/config-tsb.template > saturation_files/configs/config.config
                java -classpath ./out/production/Sim:./jep-2.3.0.jar:./djep-1.0.0.jar peersim.Simulator saturation_files/configs/config.config >> saturation_files/results/ee0a251b934a78f78279f4d943e31f670e571fb7/tsb_data_$j.csv 2>/dev/null
                echo ', '$(($i)) >> saturation_files/results/ee0a251b934a78f78279f4d943e31f670e571fb7/tsb_data_$j.csv
                rm saturation_files/configs/config.config

            done

    done